/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.config;

import fi.asteriski.nakitin.service.UserService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
@SuppressWarnings("unused")
public class SecurityConfig {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Profile({"dev", "special"})
    public static class DevSecurityConfig {
        @Bean
        public SecurityFilterChain configureDev(@NonNull HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authorizeHttpRequests ->
                            authorizeHttpRequests.anyRequest().permitAll())
                    .formLogin(Customizer.withDefaults())
                    .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
            http.cors(Customizer.withDefaults());

            return http.build();
        }
    }

    /**
     * Configures to use custom service for user management and password encrypting method.
     *
     * @param auth Builtin AuthenticationManagerBuilder entity.
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    }
}
