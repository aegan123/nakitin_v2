/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later..
 */
package fi.asteriski.nakitin.config;

import static fi.asteriski.nakitin.entity.UserRole.ROLE_ADMIN;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
@ConditionalOnProperty(name = "app.admin.initialization.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AdminUserInitializer implements DisposableBean {

    @NonNull
    private final UserRepository userRepository;

    @NonNull
    private final BCryptPasswordEncoder passwordEncoder;

    @NonNull
    private final AdminProperties adminProperties;

    @NonNull
    private final ConfigurableApplicationContext applicationContext;

    @Value("${fi.asteriski.config.maxPasswordAgeInDays}")
    private Long maxPasswordAgeInDays;

    @PostConstruct
    void initializeAdminUser() {
        String adminUsername = adminProperties.getUsername();
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        var admin = UserEntity.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminProperties.getPassword()))
                .email(adminProperties.getEmail())
                .firstName(adminProperties.getFirstName())
                .lastName(adminProperties.getLastName())
                .userRole(ROLE_ADMIN)
                .expirationDate(LocalDate.now().plusDays(maxPasswordAgeInDays))
                .emailVerified(true)
                .build();

        userRepository.save(admin);

        // Remove this bean from the application context
        applicationContext.getBeanFactory().destroyBean("adminUserInitializer");
    }

    @Override
    public void destroy() {
        // This method will be called when the bean is destroyed
        // You can add any cleanup code here if needed
    }
}
