/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public Cache<String, Bucket> loginAttemptsCache(@Value("${cache.login.expiryHours:1}") int loginCacheExpiryHours) {
        return Caffeine.newBuilder()
                .expireAfterWrite(loginCacheExpiryHours, TimeUnit.HOURS)
                .build();
    }

    @Bean
    public Cache<String, Bucket> passwordResetCache(
            @Value("${cache.passwordReset.expiryHours:24}") int passwordResetCacheExpiryHours) {
        return Caffeine.newBuilder()
                .expireAfterWrite(passwordResetCacheExpiryHours, TimeUnit.HOURS)
                .build();
    }
}
