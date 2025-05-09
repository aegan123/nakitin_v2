/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ratelimit")
@Validated
@Data
public class RateLimitProperties {
    @NotNull
    private LoginLimit login = new LoginLimit();

    @NotNull
    private PasswordResetLimit passwordReset = new PasswordResetLimit();

    @Data
    public static class LoginLimit {
        @Min(1)
        private long maxAttempts = 5;

        @Min(1)
        private long refillAmount = 5;

        @Min(1)
        private long refillMinutes = 15;
    }

    @Data
    public static class PasswordResetLimit {
        @Min(1)
        private long maxAttempts = 3;

        @Min(1)
        private long refillAmount = 3;

        @Min(1)
        private long refillHours = 1;
    }
}
