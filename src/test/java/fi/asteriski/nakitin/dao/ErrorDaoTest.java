/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fi.asteriski.nakitin.exceptions.NotFoundException;
import fi.asteriski.nakitin.repo.ErrorRepository;
import fi.asteriski.nakitin.test.factory.TestDataFactory;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ErrorDaoTest extends TestFixtures {

    @Autowired
    private ErrorRepository errorRepository;

    private ErrorDao errorDao;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        errorDao = new ErrorDao(errorRepository);
        testDataFactory =
                TestDataFactory.builder().errorRepository(errorRepository).build();
    }

    @AfterEach
    void tearDown() {
        testDataFactory.cleanUp();
    }

    @Test
    void logError_shouldPersistErrorAndReturnId() {
        var testException = new RuntimeException("Test error message");

        var savedId = errorDao.logError(testException);

        var savedError = errorRepository.findById(savedId).orElseThrow();
        assertThat(savedError.getErrorClass()).isEqualTo(RuntimeException.class.getName());
        assertThat(savedError.getErrorMessage()).isEqualTo("Test error message");
        assertThat(savedError.getStackTrace()).isNotBlank();
    }

    @Test
    void logError_withNullMessage_shouldPersistErrorWithNullMessage() {
        var testException = new RuntimeException();

        var savedId = errorDao.logError(testException);

        var savedError = errorRepository.findById(savedId).orElseThrow();
        assertThat(savedError.getErrorMessage()).isNull();
        assertThat(savedError.getErrorClass()).isEqualTo(RuntimeException.class.getName());
    }

    @Test
    void logError_withCustomException_shouldPersistCorrectErrorClass() {
        var testException = new NotFoundException("Custom error");

        var savedId = errorDao.logError(testException);

        var savedError = errorRepository.findById(savedId).orElseThrow();
        assertThat(savedError.getErrorClass()).isEqualTo(NotFoundException.class.getName());
    }

    @Test
    void fetchAllErrorsForAdmin_withNoErrors_shouldReturnEmptyPage() {
        var result = errorDao.fetchAllErrorsForAdmin(0);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchAllErrorsForAdmin_withMultipleErrors_shouldReturnPagedResults() {
        testDataFactory.persistErrors(createMultipleErrors(25));

        var firstPage = errorDao.fetchAllErrorsForAdmin(0);
        var secondPage = errorDao.fetchAllErrorsForAdmin(1);

        assertThat(firstPage.getContent()).hasSize(20);
        assertThat(secondPage.getContent()).hasSize(5);
        // Verify descending order by timestamp
        assertThat(firstPage.getContent().get(0).getTimestamp())
                .isAfterOrEqualTo(firstPage.getContent().get(1).getTimestamp());
    }

    @Test
    void fetchError_whenErrorExists_shouldReturnError() {
        var savedError = testDataFactory.persistError(createError("Test error"));

        var result = errorDao.fetchError(savedError.getId());

        assertThat(result.getId()).isEqualTo(savedError.getId());
        assertThat(result.getErrorMessage()).isEqualTo("Test error");
    }

    @Test
    void fetchError_whenErrorDoesNotExist_shouldThrowNotFoundException() {
        var nonexistentId = UUID.randomUUID();

        assertThatThrownBy(() -> errorDao.fetchError(nonexistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(nonexistentId.toString());
    }
}
