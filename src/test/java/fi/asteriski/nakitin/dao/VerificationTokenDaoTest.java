/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static org.assertj.core.api.Assertions.assertThat;

import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.repo.VerificationTokenRepository;
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
class VerificationTokenDaoTest extends TestFixtures {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private VerificationTokenDao verificationTokenDao;

    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        verificationTokenDao = new VerificationTokenDao(verificationTokenRepository);
        testDataFactory = TestDataFactory.builder()
                .userRepository(userRepository)
                .verificationTokenRepository(verificationTokenRepository)
                .build();
    }

    @AfterEach
    void tearDown() {
        testDataFactory.cleanUp();
    }

    @Test
    void deleteByUser_givenTokenExists_shouldDeleteTokenForGivenUser() {
        var user1 = createDefaultUser();
        var user2 = createDefaultUser();
        var token1 = createToken(user1);
        var token2 = createToken(user2);
        testDataFactory.persistUsers(user1, user2);
        testDataFactory.persistVerificationTokens(token1, token2);
        var countBefore = verificationTokenRepository.count();

        verificationTokenDao.deleteByUser(user1);

        var countAfter = verificationTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - 1);
    }

    @Test
    void deleteByUser_givenTokenDoesNotExists_shouldNotDeleteAnything() {
        var user1 = createDefaultUser();
        var user2 = createDefaultUser();
        var token = createToken(user1);
        testDataFactory.persistUsers(user1, user2);
        testDataFactory.persistVerificationToken(token);
        var countBefore = verificationTokenRepository.count();

        verificationTokenDao.deleteByUser(user2);

        var countAfter = verificationTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Test
    void findUnverifiedUsersWithExpiredTokens_expiredTokensExist_returnsExpiredTokens() {
        var now = LocalDateTime.now();
        var user1 = createDefaultUser();
        var user2 = createDefaultUser();

        var expiredToken = createToken(user1, now.minusDays(1));
        var validToken = createToken(user2, now.plusDays(1));

        testDataFactory.persistUsers(user1, user2);
        testDataFactory.persistVerificationTokens(expiredToken, validToken);

        var expiredTokens = verificationTokenDao.findUnverifiedUsersWithExpiredTokens();

        assertThat(expiredTokens).hasSize(1);
        assertThat(expiredTokens.getFirst().getExpiryDate()).isBefore(now);
    }

    @Test
    void findUnverifiedUsersWithExpiredTokens_noExpiredTokens_returnsEmptyList() {
        var now = LocalDateTime.now();
        var user1 = createDefaultUser();
        var user2 = createDefaultUser();

        var token1 = createToken(user1, now.plusDays(1));
        var token2 = createToken(user2, now.plusDays(1));

        testDataFactory.persistUsers(user1, user2);
        testDataFactory.persistVerificationTokens(token1, token2);

        var expiredTokens = verificationTokenDao.findUnverifiedUsersWithExpiredTokens();

        assertThat(expiredTokens).isEmpty();
    }

    @Test
    void deleteByUsers_givenTokensExist_tokensExistsForTheUsersAndOtherUsers_shouldDeleteTokensForGivenUsers() {
        var user1 = createDefaultUser();
        var user2 = createDefaultUser();
        var user3 = createDefaultUser();
        var token1 = createToken(user1);
        var token2 = createToken(user2);
        var token3 = createToken(user3);
        testDataFactory.persistUsers(user1, user2, user3);
        testDataFactory.persistVerificationTokens(token1, token2, token3);
        var countBefore = verificationTokenRepository.count();

        verificationTokenDao.deleteByUsers(List.of(user1, user3));

        var countAfter = verificationTokenRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - 2);
    }
}
