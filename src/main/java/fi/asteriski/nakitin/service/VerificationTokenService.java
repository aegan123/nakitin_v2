/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class VerificationTokenService {
    @Value("${security.verificationToken.expiryHours}")
    private Long tokenExpiryHours;

    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusHours(tokenExpiryHours);
    }

    public boolean isTokenExpired(LocalDateTime expiryDate) {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
}
