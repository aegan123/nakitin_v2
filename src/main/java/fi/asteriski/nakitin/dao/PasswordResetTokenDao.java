/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.entity.PasswordResetToken;
import fi.asteriski.nakitin.repo.PasswordResetTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PasswordResetTokenDao {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token).orElse(null);
    }

    public void save(PasswordResetToken passwordResetToken) {
        passwordResetTokenRepository.save(passwordResetToken);
    }

    public void delete(PasswordResetToken passwordResetToken) {
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    public List<PasswordResetToken> findExpiredTokens() {
        return passwordResetTokenRepository.findAllByExpiryDateIsBefore(LocalDateTime.now());
    }

    public void deleteAll(List<PasswordResetToken> toDelete) {
        passwordResetTokenRepository.deleteAll(toDelete);
    }
}
