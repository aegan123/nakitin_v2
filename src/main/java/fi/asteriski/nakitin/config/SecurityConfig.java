/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.config;

import static fi.asteriski.nakitin.entity.UserRole.*;
import static org.springframework.http.HttpMethod.*;

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
        private final int rememberMeValiditySeconds;

        public DevSecurityConfig(
                CustomAuthenticationFailureHandler authenticationFailureHandler,
                @Value("${security.rememberMe.validitySeconds:86400}") int rememberMeValiditySeconds) {
            this.rememberMeValiditySeconds = rememberMeValiditySeconds;
        }

        @Bean
        public SecurityFilterChain configureDev(@NonNull HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                            .requestMatchers(POST, "/logout")
                            .hasAnyRole(ROLE_USER.label, ROLE_ADMIN.label, ROLE_ORG_ADMIN.label)
                            .requestMatchers(POST, "/volunteer/**")
                            .hasAnyRole(ROLE_USER.label, ROLE_ADMIN.label, ROLE_ORG_ADMIN.label)
                            .requestMatchers(POST, "/task/add")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/task/add")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/task/edit")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(POST, "/task/edit")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/task/delete")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(POST, "/task/delete")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/export/**")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/add-event")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(POST, "/add-event")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/edit-event")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(POST, "/edit-event")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/delete-event")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(POST, "/delete-event")
                            .hasRole(ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/profile")
                            .hasAnyRole(ROLE_USER.label, ROLE_ADMIN.label, ROLE_ORG_ADMIN.label)
                            .requestMatchers(POST, "/profile")
                            .hasAnyRole(ROLE_USER.label, ROLE_ADMIN.label, ROLE_ORG_ADMIN.label)
                            .requestMatchers(GET, "/admin/**")
                            .hasRole(ROLE_ADMIN.label)
                            .requestMatchers(POST, "/admin/**")
                            .hasRole(ROLE_ADMIN.label)
                            .requestMatchers(
                                    "/",
                                    "/event/**",
                                    "/login",
                                    "/organizations",
                                    "/privacy",
                                    "/signup",
                                    "/success",
                                    "/error",
                                    "/js/**",
                                    "/favicon.ico",
                                    "/forgot-password",
                                    "/reset-password/**",
                                    "/css/**")
                            .permitAll())
                    .formLogin(form -> form.loginPage("/login")
                            .defaultSuccessUrl("/", true)
                            .failureUrl("/login?error=true")
                            .permitAll())
                    .logout(logout -> logout.logoutSuccessUrl("/")
                            .deleteCookies("JSESSIONID")
                            .permitAll())
                    .rememberMe(remember -> remember.key("uniqueAndSecretKey")
                            .tokenValiditySeconds(rememberMeValiditySeconds)
                            .rememberMeParameter("remember-me"))
                    .cors(Customizer.withDefaults());

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
