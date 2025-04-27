/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import static fi.asteriski.nakitin.utils.Constants.TOKEN_EXPIRY_HOURS;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class VerificationTokenService {

    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);
    }

    public boolean isTokenExpired(LocalDateTime expiryDate) {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
}
