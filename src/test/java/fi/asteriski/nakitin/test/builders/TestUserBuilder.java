/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.test.builders;

import static fi.asteriski.nakitin.entity.UserRole.USER;
import static fi.asteriski.nakitin.test.factory.TestDataFactory.generateRandomString;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.UserRole;
import java.time.LocalDate;
import java.util.Random;

public class TestUserBuilder {
    private String username = generateRandomString(10);
    private String password = generateRandomString(10);
    private String firstName = generateRandomString(10);
    private String lastName = generateRandomString(10);
    private String email = generateRandomString(10) + "@example.com";
    private boolean emailVerified = true;
    private LocalDate expirationDate = LocalDate.now().plusDays(1);
    private UserRole userRole = USER;

    public static TestUserBuilder builder() {
        return new TestUserBuilder();
    }

    public TestUserBuilder withDefaults() {
        var rnd = new Random();
        withUsername(generateRandomString(10));
        withPassword(generateRandomString(10));
        withFirstName("Test");
        withLastName("User");
        withEmail(rnd.nextBoolean() ? "user@example.com" : "user@example.org");
        withExpirationDate(LocalDate.now().plusDays(1));
        withUserRole(USER);
        withEmailVerified(true);
        return this;
    }

    public TestUserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public TestUserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public TestUserBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public TestUserBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public TestUserBuilder withExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public TestUserBuilder withUserRole(UserRole role) {
        this.userRole = role;
        return this;
    }

    public TestUserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public TestUserBuilder withEmailVerified(boolean verified) {
        this.emailVerified = verified;
        return this;
    }

    public UserEntity build() {
        return UserEntity.builder()
                .username(username)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .emailVerified(emailVerified)
                .expirationDate(expirationDate)
                .userRole(userRole)
                .build();
    }
}
