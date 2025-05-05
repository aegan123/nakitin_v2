/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import com.github.benmanes.caffeine.cache.Cache;
import fi.asteriski.nakitin.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class RateLimitService {
    private final Cache<String, Bucket> loginAttemptsCache;
    private final Cache<String, Bucket> passwordResetCache;
    private final RateLimitProperties properties;

    public boolean tryConsumeLimitByEmail(String email) {
        Bucket bucket = passwordResetCache.get(email, key -> createEmailBucket());
        return bucket.tryConsume(1);
    }

    public boolean tryConsumeLimitByIp(String ip) {
        Bucket bucket = loginAttemptsCache.get(ip, key -> createLoginBucket());
        return bucket.tryConsume(1);
    }

    private Bucket createEmailBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.getPasswordReset().getMaxAttempts())
                        .refillIntervally(
                                properties.getPasswordReset().getRefillAmount(),
                                Duration.ofHours(properties.getPasswordReset().getRefillHours()))
                        .build())
                .build();
    }

    private Bucket createLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.getLogin().getMaxAttempts())
                        .refillIntervally(
                                properties.getLogin().getRefillAmount(),
                                Duration.ofMinutes(properties.getLogin().getRefillMinutes()))
                        .build())
                .build();
    }
}
