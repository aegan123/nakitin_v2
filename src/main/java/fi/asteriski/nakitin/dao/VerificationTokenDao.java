/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.VerificationTokenEntity;
import fi.asteriski.nakitin.repo.VerificationTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class VerificationTokenDao {
    private final VerificationTokenRepository verificationTokenRepository;

    public void deleteByUser(UserEntity user) {
        verificationTokenRepository.deleteByUser(user);
    }

    public void save(VerificationTokenEntity verificationToken) {
        verificationTokenRepository.save(verificationToken);
    }

    public Optional<VerificationTokenEntity> findByToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    public List<VerificationTokenEntity> findUnverifiedUsersWithExpiredTokens() {
        return verificationTokenRepository.findAllByExpiryDateIsBefore(LocalDateTime.now());
    }

    public void deleteByUsers(List<UserEntity> users) {
        verificationTokenRepository.deleteAllByUserIn(users);
    }
}
