/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.repo.EventRepository;
import fi.asteriski.nakitin.repo.OrganizationRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.test.factory.TestDataFactory;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class EventDaoTest extends TestFixtures {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    private EventDao eventDao;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        eventDao = new EventDao(eventRepository);
        testDataFactory = TestDataFactory.builder()
                .eventRepository(eventRepository)
                .organizationRepository(organizationRepository)
                .userRepository(userRepository)
                .build();
    }

    @AfterEach
    void tearDown() {
        testDataFactory.cleanUp();
    }

    @Test
    void saveEvent_shouldPersistEventWithOrganizationAndUser() {
        var organization = testDataFactory.persistOrganization(createDefaultOrganization());
        var user = testDataFactory.persistUser(createDefaultUser());
        var eventDto = EventDto.builder()
                .name("Test Event")
                .description("Test Description")
                .venue("Test Venue")
                .date(LocalDate.now())
                .build();

        var savedEventId = eventDao.saveEvent(eventDto, organization, user);

        var savedEvent = eventRepository.findById(savedEventId).orElseThrow();
        assertThat(savedEvent.getName()).isEqualTo("Test Event");
        assertThat(savedEvent.getOrganizer()).isEqualTo(organization);
        assertThat(savedEvent.getCreatedBy()).isEqualTo(user);
    }

    @Test
    void fetchEventById_whenEventExists_shouldReturnEventDto() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvent(event);

        var result = eventDao.fetchEventById(event.getId());

        assertThat(result.name()).isEqualTo("Test Event");
        assertThat(result.date()).isEqualTo(event.getDate());
    }

    @Test
    void fetchEventById_whenEventDoesNotExist_shouldThrowException() {
        var nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> eventDao.fetchEventById(nonExistentId))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    void fetchEventsByIds_whenEventsExist_shouldReturnEvents() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event1 = createEvent("Event 1", LocalDate.now(), organization, user);
        var event2 = createEvent("Event 2", LocalDate.now(), organization, user);
        var event3 = createEvent("Event 3", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvents(event1, event2, event3);

        var result = eventDao.fetchEventsByIds(List.of(event1.getId(), event2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(EventEntity::getName)
                .containsExactlyInAnyOrder(event1.getName(), event2.getName());
    }

    @Test
    void fetchEventsByIds_whenNoEventsExist_shouldReturnEmptyList() {
        var nonExistentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        var result = eventDao.fetchEventsByIds(nonExistentIds);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchAllEventsForAdmin_shouldReturnPagedResults() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var events = new EventEntity[25];
        for (int i = 0; i < 25; i++) {
            events[i] = createEvent("Event " + i, LocalDate.now(), organization, user);
        }
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvents(events);

        var firstPage = eventDao.fetchAllEventsForAdmin(0);
        var secondPage = eventDao.fetchAllEventsForAdmin(1);

        assertThat(firstPage.getContent()).hasSize(20);
        assertThat(secondPage.getContent()).hasSize(5);
    }

    @Test
    void fetchEventEntityById_whenEventExists_shouldReturnEvent() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvent(event);

        var result = eventDao.fetchEventEntityById(event.getId());

        assertThat(result.getId()).isEqualTo(event.getId());
        assertThat(result.getName()).isEqualTo("Test Event");
    }

    @Test
    void fetchEventEntityById_whenEventDoesNotExist_shouldThrowException() {
        var nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> eventDao.fetchEventEntityById(nonExistentId))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    void deleteEvent_shouldRemoveEventFromDatabase() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvent(event);

        eventDao.deleteEvent(event.getId());

        assertThat(eventRepository.findById(event.getId())).isEmpty();
    }

    @Test
    void fetchEventDetailsById_whenEventExists_shouldReturnEventDto() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvent(event);

        var result = eventDao.fetchEventDetailsById(event.getId());

        assertThat(result.name()).isEqualTo("Test Event");
    }

    @Test
    void save_shouldPersistEventAndReturnId() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);

        var savedId = eventDao.save(event);

        assertThat(eventRepository.findById(savedId))
                .isPresent()
                .get()
                .extracting(EventEntity::getName)
                .isEqualTo("Test Event");
    }
}
