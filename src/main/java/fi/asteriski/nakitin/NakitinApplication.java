/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later..
 */
package fi.asteriski.nakitin;

import fi.asteriski.nakitin.entity.OrganizationEntity;
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
        // FIXME remove before prod?
        if (!userRepository.getClass().getSimpleName().contains("Mockito")) {
            var adminUsername = System.getenv("ADMIN_USERNAME") != null ? System.getenv("ADMIN_USERNAME") : "admin";
            var adminPassword = System.getenv("ADMIN_PASSWORD") != null ? System.getenv("ADMIN_PASSWORD") : "admin";
            var orgName = System.getenv("ORG_NAME") != null ? System.getenv("ORG_NAME") : "Asteriski RY";
            var firstName = System.getenv("FIRST_NAME") != null ? System.getenv("FIRST_NAME") : "Asteriski";
            var lastName = System.getenv("LAST_NAME") != null ? System.getenv("LAST_NAME") : "Admin";
            var email = System.getenv("EMAIL") != null ? System.getenv("EMAIL") : "admin@localhost";
            var org = OrganizationEntity.builder().name(orgName).build();
            var admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(bCryptPasswordEncoder.encode(adminPassword))
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .userRole(UserRole.ROLE_ORG_ADMIN)
                    .expirationDate(LocalDate.now().plusDays(5))
                    .build();
            if (!userRepository.existsByUsername(adminUsername)) {
                admin.getOrganizations().add(org);
                userRepository.save(admin);
            }
        }
    }
}
