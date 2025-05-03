/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.test.fixtures;

import fi.asteriski.nakitin.entity.*;
import fi.asteriski.nakitin.test.builders.TestUserBuilder;
import fi.asteriski.nakitin.test.builders.TestVerificationTokenBuilder;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import fi.asteriski.nakitin.test.factory.TestDataFactory;
import org.junit.jupiter.api.TestInstance;

import static fi.asteriski.nakitin.entity.UserRole.ROLE_ADMIN;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestFixtures {
    protected UserEntity createDefaultUser() {
        return TestUserBuilder.builder().withDefaults().build();
    }

    protected UserEntity createDefaultAdminUser() {
        return TestUserBuilder.builder().withDefaults().withUserRole(ROLE_ADMIN).build();
    }

    protected UserEntity[] createMultipleDefaultUsers(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> TestUserBuilder.builder().withDefaults().build())
            .toList()
            .toArray(UserEntity[]::new);
    }

    protected VerificationTokenEntity createToken(UserEntity user, LocalDateTime expiryDate) {
        return TestVerificationTokenBuilder.builder()
                .withUser(user)
                .expiringAt(expiryDate)
                .build();
    }

    protected VerificationTokenEntity createToken(UserEntity user) {
        return createToken(user, LocalDateTime.now().plusDays(1));
    }

    protected PasswordResetToken createPasswordResetToken(UserEntity user) {
        return createPasswordResetToken(user, LocalDateTime.now().plusDays(1));
    }

    protected PasswordResetToken createPasswordResetToken(UserEntity user, LocalDateTime expiryDate) {
        return PasswordResetToken.builder()
            .token(TestDataFactory.generateRandomString(30))
            .user(user)
            .expiryDate(expiryDate)
            .build();
    }

    protected OrganizationEntity createDefaultOrganization() {
        return OrganizationEntity.builder().name("Test Organization").build();
    }
}
