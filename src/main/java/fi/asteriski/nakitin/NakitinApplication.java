/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later..
 */
package fi.asteriski.nakitin;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.UserRole;
import fi.asteriski.nakitin.repo.UserRepository;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@AllArgsConstructor
public class NakitinApplication implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(NakitinApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // FIXME remove before prod
        if (!userRepository.getClass().getSimpleName().contains("Mockito")) {
            var adminUsername = System.getenv("ADMIN_USERNAME");
            var adminPassword = System.getenv("ADMIN_PASSWORD");
            var admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(bCryptPasswordEncoder.encode(adminPassword))
                    .email("palvelinvastaavat@asteriski.fi")
                    .firstName("Asteriski")
                    .lastName("Admin")
                    .userRole(UserRole.ROLE_ADMIN)
                    .expirationDate(LocalDate.now().plusYears(100))
                    .build();
            if (!userRepository.existsByUsername(adminUsername)) {
                userRepository.save(admin);
            }
        }
    }
}
