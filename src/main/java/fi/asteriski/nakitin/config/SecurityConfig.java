/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.config;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@SuppressWarnings("unused")
public class SecurityConfig {
    @Profile({"dev", "special"})
    public static class DevSecurityConfig {
        @Bean
        public SecurityFilterChain configureDev(@NonNull HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(
                    authorizeHttpRequests -> authorizeHttpRequests.anyRequest().permitAll());
            http.cors(Customizer.withDefaults());

            return http.build();
        }
    }
}
