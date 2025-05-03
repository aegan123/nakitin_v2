/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
*/
package fi.asteriski.nakitin.test.factory;

import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.PasswordResetToken;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.VerificationTokenEntity;
import fi.asteriski.nakitin.repo.OrganizationRepository;
import fi.asteriski.nakitin.repo.PasswordResetTokenRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.repo.VerificationTokenRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;

@Profile("test")
@RequiredArgsConstructor
@Builder
public class TestDataFactory {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final OrganizationRepository organizationRepository;

    public UserEntity persistUser(UserEntity user) {
        return userRepository.save(user);
    }

    public List<UserEntity> persistUsers(UserEntity... users) {
        return userRepository.saveAll(Arrays.asList(users));
    }

    public VerificationTokenEntity persistVerificationToken(VerificationTokenEntity token) {
        return verificationTokenRepository.save(token);
    }

    public List<VerificationTokenEntity> persistVerificationTokens(VerificationTokenEntity... tokens) {
        return verificationTokenRepository.saveAll(Arrays.asList(tokens));
    }

    public PasswordResetToken persistToken(PasswordResetToken token) {
        return passwordResetTokenRepository.save(token);
    }

    public List<PasswordResetToken> persistTokens(PasswordResetToken... tokens) {
        return passwordResetTokenRepository.saveAll(Arrays.asList(tokens));
    }

    public OrganizationEntity persistOrganization(OrganizationEntity organization) {
        return organizationRepository.save(organization);
    }

    public List<OrganizationEntity> persistOrganizations(OrganizationEntity... organizations) {
        return organizationRepository.saveAll(Arrays.asList(organizations));
    }

    public void cleanUp() {
        if (verificationTokenRepository != null) {
            verificationTokenRepository.deleteAll();
        }
        if (passwordResetTokenRepository != null) {
            passwordResetTokenRepository.deleteAll();
        }
        if (userRepository != null) {
            userRepository.deleteAll();
        }
    }

    public static String generateRandomString(int targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        var random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
