/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.test.builders;

import static fi.asteriski.nakitin.test.factory.TestDataFactory.generateRandomString;

import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import java.time.LocalDate;
import java.util.UUID;

public class TestEventBuilder {
    private String name = generateRandomString(10);
    private String venue = generateRandomString(10);
    private String description = generateRandomString(10);
    private LocalDate date = LocalDate.now();
    private OrganizationEntity organizer = null;
    private UserEntity createdBy = null;
    private UUID id = null;

    public static TestEventBuilder builder() {
        return new TestEventBuilder();
    }

    public TestEventBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TestEventBuilder withVenue(String venue) {
        this.venue = venue;
        return this;
    }

    public TestEventBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public TestEventBuilder withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public TestEventBuilder withDefaults() {
        return this;
    }

    public TestEventBuilder withOrganizer(OrganizationEntity organizer) {
        this.organizer = organizer;
        return this;
    }

    public TestEventBuilder withCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public TestEventBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public EventEntity build() {
        return EventEntity.builder()
                .id(id)
                .name(name)
                .venue(venue)
                .description(description)
                .date(date)
                .organizer(organizer)
                .createdBy(createdBy)
                .build();
    }
}
