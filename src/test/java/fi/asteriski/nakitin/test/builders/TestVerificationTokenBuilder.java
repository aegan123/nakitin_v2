/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.test.builders;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.VerificationTokenEntity;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestVerificationTokenBuilder {
    private final VerificationTokenEntity token = new VerificationTokenEntity();

    public static TestVerificationTokenBuilder builder() {
        return new TestVerificationTokenBuilder();
    }

    public TestVerificationTokenBuilder withUser(UserEntity user) {
        token.setUser(user);
        return this;
    }

    public TestVerificationTokenBuilder expiringAt(LocalDateTime expiryDate) {
        token.setExpiryDate(expiryDate);
        return this;
    }

    public TestVerificationTokenBuilder withDefaultExpiration() {
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        return this;
    }

    public VerificationTokenEntity build() {
        token.setToken(UUID.randomUUID().toString());
        return token;
    }
}
