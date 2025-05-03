/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
*/
package fi.asteriski.nakitin.dao;

import static org.assertj.core.api.Assertions.assertThat;

import fi.asteriski.nakitin.repo.PasswordResetTokenRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.test.factory.TestDataFactory;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class PasswordResetTokenDaoTest extends TestFixtures {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private PasswordResetTokenDao passwordResetTokenDao;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        passwordResetTokenDao = new PasswordResetTokenDao(passwordResetTokenRepository);
        testDataFactory = TestDataFactory.builder()
                .userRepository(userRepository)
                .passwordResetTokenRepository(passwordResetTokenRepository)
                .build();
    }

    @AfterEach
    void tearDown() {
        testDataFactory.cleanUp();
    }

    @Test
    void findByToken_whenTokenExists_shouldReturnToken() {
        var user = createDefaultUser();
        var token = createPasswordResetToken(user);
        testDataFactory.persistUser(user);
        testDataFactory.persistToken(token);

        var result = passwordResetTokenDao.findByToken(token.getToken());

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(token.getToken());
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    void findByToken_whenTokenDoesNotExist_shouldReturnNull() {
        var result = passwordResetTokenDao.findByToken("nonexistent-token");

        assertThat(result).isNull();
    }

    @Test
    void save_shouldPersistToken() {
        var user = createDefaultUser();
        var token = createPasswordResetToken(user);
        testDataFactory.persistUser(user);
        var countBefore = passwordResetTokenRepository.count();

        passwordResetTokenDao.save(token);

        var countAfter = passwordResetTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore + 1);
    }

    @Test
    void delete_whenTokenExists_shouldDeleteToken() {
        var user = createDefaultUser();
        var token = createPasswordResetToken(user);
        testDataFactory.persistUser(user);
        testDataFactory.persistToken(token);
        var countBefore = passwordResetTokenRepository.count();

        passwordResetTokenDao.delete(token);

        var countAfter = passwordResetTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - 1);
    }

    @Test
    void findExpiredTokens_shouldReturnOnlyExpiredTokens() {
        var now = LocalDateTime.now();
        var user1 = createDefaultUser();
        var user2 = createDefaultUser();

        var expiredToken = createPasswordResetToken(user1, now.minusDays(1));
        var validToken = createPasswordResetToken(user2, now.plusDays(1));

        testDataFactory.persistUsers(user1, user2);
        testDataFactory.persistTokens(expiredToken, validToken);

        var expiredTokens = passwordResetTokenDao.findExpiredTokens();

        assertThat(expiredTokens).hasSize(1);
        assertThat(expiredTokens.getFirst().getExpiryDate()).isBefore(now);
    }

    @Test
    void deleteAll_shouldDeleteAllGivenTokens() {
        var user1 = createDefaultUser();
        var user2 = createDefaultUser();
        var user3 = createDefaultUser();

        var token1 = createPasswordResetToken(user1);
        var token2 = createPasswordResetToken(user2);
        var token3 = createPasswordResetToken(user3);

        testDataFactory.persistUsers(user1, user2, user3);
        testDataFactory.persistTokens(token1, token2, token3);
        var countBefore = passwordResetTokenRepository.count();

        passwordResetTokenDao.deleteAll(List.of(token1, token2));

        var countAfter = passwordResetTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - 2);
        assertThat(passwordResetTokenRepository.findByToken(token3.getToken())).isPresent();
    }
}
