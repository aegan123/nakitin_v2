/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.test.fixtures;

import static fi.asteriski.nakitin.entity.UserRole.ROLE_ADMIN;

import fi.asteriski.nakitin.dto.EventTaskForm;
import fi.asteriski.nakitin.entity.*;
import fi.asteriski.nakitin.test.builders.TestErrorBuilder;
import fi.asteriski.nakitin.test.builders.TestEventBuilder;
import fi.asteriski.nakitin.test.builders.TestUserBuilder;
import fi.asteriski.nakitin.test.builders.TestVerificationTokenBuilder;
import fi.asteriski.nakitin.test.factory.TestDataFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestFixtures {
    protected UserEntity createDefaultUser() {
        return TestUserBuilder.builder().withDefaults().build();
    }

    protected UserEntity createDefaultAdminUser() {
        return TestUserBuilder.builder().withDefaults().withUserRole(ROLE_ADMIN).build();
    }

    protected UserEntity[] createMultipleDefaultUsers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> TestUserBuilder.builder().withDefaults().build())
                .toList()
                .toArray(UserEntity[]::new);
    }

    protected UserEntity createExpiringUsers(LocalDate expiryDate) {
        return TestUserBuilder.builder()
                .withDefaults()
                .withExpirationDate(expiryDate)
                .build();
    }

    protected UserEntity createUserWithEmail(String email) {
        return TestUserBuilder.builder().withDefaults().withEmail(email).build();
    }

    protected VerificationTokenEntity createToken(UserEntity user, LocalDateTime expiryDate) {
        return TestVerificationTokenBuilder.builder()
                .withUser(user)
                .expiringAt(expiryDate)
                .build();
    }

    protected VerificationTokenEntity createToken(UserEntity user) {
        return createToken(user, LocalDateTime.now().plusDays(1));
    }

    protected PasswordResetToken createPasswordResetToken(UserEntity user) {
        return createPasswordResetToken(user, LocalDateTime.now().plusDays(1));
    }

    protected PasswordResetToken createPasswordResetToken(UserEntity user, LocalDateTime expiryDate) {
        return PasswordResetToken.builder()
                .token(TestDataFactory.generateRandomString(30))
                .user(user)
                .expiryDate(expiryDate)
                .build();
    }

    protected OrganizationEntity createOrganization(String name) {
        return OrganizationEntity.builder()
                .name(name)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    protected OrganizationEntity[] createMultipleOrganizations(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createOrganization("Org" + i))
                .toList()
                .toArray(OrganizationEntity[]::new);
    }

    protected OrganizationEntity createDefaultOrganization() {
        return createOrganization("Default Org");
    }

    protected EventEntity createEvent(String name, LocalDate date, OrganizationEntity organizer, UserEntity createdBy) {
        return TestEventBuilder.builder()
                .withDefaults()
                .withDate(date)
                .withName(name)
                .withOrganizer(organizer)
                .withCreatedBy(createdBy)
                .build();
    }

    protected EventTaskEntity createEventTask(String name, LocalDate date, int personCount) {
        var rnd = new Random();
        var startTime = LocalTime.of(rnd.nextInt(9, 16), 0);
        var endTime = startTime.plusHours(rnd.nextInt(3, 5));
        return EventTaskEntity.builder()
                .taskName(name)
                .date(date)
                .personCount(personCount)
                .startTime(startTime)
                .endTime(endTime)
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    protected EventTaskForm createEventTaskForm(EventEntity event) {
        var rnd = new Random();
        var startTime = LocalTime.of(rnd.nextInt(9, 16), 0);
        var endTime = startTime.plusHours(rnd.nextInt(3, 5));
        return EventTaskForm.builder()
                .eventId(event.getId())
                .taskName("TestTask")
                .date(LocalDate.now())
                .startTime(startTime)
                .endTime(endTime)
                .personCount(2)
                .build();
    }

    protected ErrorEntity createError(String errorMessage) {
        var rnd = new Random();
        return TestErrorBuilder.builder()
                .withErrorMessage(errorMessage)
                .withTimestamp(ZonedDateTime.now().plusHours(rnd.nextInt(-3, 3)))
                .build();
    }

    protected ErrorEntity[] createMultipleErrors(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createError("Error " + i))
                .toList()
                .toArray(ErrorEntity[]::new);
    }
}
