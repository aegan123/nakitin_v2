/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fi.asteriski.nakitin.dto.EventTaskForm;
import fi.asteriski.nakitin.dto.NameAndDateDto;
import fi.asteriski.nakitin.dto.UpcomingEventDto;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.exceptions.EventTaskNotFoundException;
import fi.asteriski.nakitin.repo.EventRepository;
import fi.asteriski.nakitin.repo.EventTaskRepository;
import fi.asteriski.nakitin.repo.OrganizationRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.test.factory.TestDataFactory;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("test")
class NakitinDaoTest extends TestFixtures {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTaskRepository eventTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @MockitoBean
    private MessageSource messageSource;

    private NakitinDao nakitinDao;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        nakitinDao = new NakitinDao(eventRepository, eventTaskRepository, userRepository, messageSource);
        testDataFactory = TestDataFactory.builder()
                .eventRepository(eventRepository)
                .eventTaskRepository(eventTaskRepository)
                .userRepository(userRepository)
                .organizationRepository(organizationRepository)
                .build();

        when(messageSource.getMessage(eq("error.event.not-found.label"), any(), any()))
                .thenReturn("Event not found");
        when(messageSource.getMessage(eq("error.event-task.not-found.label"), any(), any()))
                .thenReturn("Event task not found");
    }

    @AfterEach
    void tearDown() {
        testDataFactory.cleanUp();
    }

    @Test
    void fetchUpcomingEvents_futureEventsExists_shouldReturnEventsInTheFuture() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var today = LocalDate.now();
        var futureEvent1 = createEvent("Event1", today.plusDays(1), organization, user);
        var futureEvent2 = createEvent("Event2", today.plusMonths(6), organization, user);
        var pastEvent = createEvent("Event3", today.minusDays(6), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvents(futureEvent1, futureEvent2, pastEvent);

        var result = nakitinDao.fetchUpcomingEvents();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(UpcomingEventDto::name)
                .containsExactlyInAnyOrder(futureEvent1.getName(), futureEvent2.getName());
    }

    @Test
    void fetchUpcomingEvents_noFutureEventsExist_shouldReturnEmptyList() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var today = LocalDate.now();
        var pastEvent1 = createEvent("Event1", today.minusDays(1), organization, user);
        var pastEvent2 = createEvent("Event2", today.minusDays(6), organization, user);
        var pastEvent3 = createEvent("Event3", today.minusDays(6), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvents(pastEvent1, pastEvent2, pastEvent3);

        var result = nakitinDao.fetchUpcomingEvents();

        assertThat(result).isEmpty();
    }

    @Test
    void fetchEventForEventPage_whenEventExists_shouldReturnEvent() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("TestEvent", LocalDate.now().plusDays(1), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvent(event);

        var result = nakitinDao.fetchEventForEventPage(event.getId());

        assertThat(result.name()).isEqualTo("TestEvent");
    }

    @Test
    void fetchEventForEventPage_whenEventDoesNotExist_shouldThrowException() {
        var randomId = UUID.randomUUID();

        assertThatThrownBy(() -> nakitinDao.fetchEventForEventPage(randomId))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void fetchEventForEventPage_whenEventIsInThePast_shouldThrowException() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("TestEvent", LocalDate.now().minusDays(2), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        var finalEvent = testDataFactory.persistEvent(event);

        assertThatThrownBy(() -> nakitinDao.fetchEventForEventPage(finalEvent.getId()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void fetchReference_shouldReturnTaskReference() {
        var task = createEventTask("TestTask", LocalDate.now(), 2);
        testDataFactory.persistEventTask(task);

        var result = nakitinDao.fetchReference(task.getId());

        assertThat(result.getId()).isEqualTo(task.getId());
    }

    @Test
    void saveUser_shouldPersistUser() {
        var user = createDefaultUser();

        nakitinDao.saveUser(user);

        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void createNewEventTask_shouldCreateAndAssociateTask() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("TestEvent", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvent(event);

        var taskForm = createEventTaskForm(event);

        nakitinDao.createNewEventTask(taskForm);

        var savedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(savedEvent.getTasks()).hasSize(1);
        assertThat(savedEvent.getTasks().stream().findFirst().get().getTaskName())
                .isEqualTo("TestTask");
    }

    @Test
    void editEventTask_whenTaskExists_shouldUpdateTask() {
        var task = createEventTask("OldName", LocalDate.now(), 2);
        testDataFactory.persistEventTask(task);

        var taskForm = EventTaskForm.builder()
                .id(task.getId())
                .taskName("NewName")
                .date(LocalDate.now())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .personCount(3)
                .build();

        nakitinDao.editEventTask(taskForm);

        var updatedTask = eventTaskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getTaskName()).isEqualTo("NewName");
        assertThat(updatedTask.getPersonCount()).isEqualTo(3);
    }

    @Test
    void editEventTask_whenTaskDoesNotExist_shouldThrowException() {
        var taskForm = EventTaskForm.builder()
                .id(UUID.randomUUID())
                .taskName("NewName")
                .date(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .personCount(3)
                .build();

        assertThatThrownBy(() -> nakitinDao.editEventTask(taskForm)).isInstanceOf(EventTaskNotFoundException.class);
    }

    @Test
    void getEventById_whenEventExists_shouldReturnEvent() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("TestEvent", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvent(event);

        var result = nakitinDao.getEventById(event.getId());

        assertThat(result.name()).isEqualTo("TestEvent");
    }

    @Test
    void getEventById_whenEventDoesNotExist_shouldThrowException() {
        var randomId = UUID.randomUUID();

        assertThatThrownBy(() -> nakitinDao.getEventById(randomId)).isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void fetchUser_whenUserExists_shouldReturnUser() {
        var user = testDataFactory.persistUser(createDefaultUser());

        var result = nakitinDao.fetchUser(user.getUsername());

        assertThat(result.getId()).isEqualTo(user.getId());
    }

    @Test
    void fetchUser_whenUserDoesNotExist_shouldThrowException() {
        assertThatThrownBy(() -> nakitinDao.fetchUser("nonexistent")).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void fetchEventDate_whenEventExists_shouldReturnDate() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("TestEvent", LocalDate.now(), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvent(event);

        var result = nakitinDao.fetchEventDate(event.getId());

        assertThat(result).isEqualTo(event.getDate());
    }

    @Test
    void fetchEventDate_whenEventDoesNotExist_shouldThrowException() {
        var randomId = UUID.randomUUID();

        assertThatThrownBy(() -> nakitinDao.fetchEventDate(randomId)).isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void fetchUpcomingEvents_shouldReturnUpcomingEventsForOrganization() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var futureEvent1 = createEvent("Event1", LocalDate.now().plusDays(1), organization, user);
        var futureEvent2 = createEvent("Event2", LocalDate.now().plusDays(2), organization, user);
        var pastEvent = createEvent("Event2", LocalDate.now().minusDays(2), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvents(futureEvent1, futureEvent2, pastEvent);

        var result = nakitinDao.fetchUpcomingEvents(organization.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(NameAndDateDto::name)
                .containsExactlyInAnyOrder(futureEvent1.getName(), futureEvent2.getName());
    }

    @Test
    void fetchPastEvents_shouldReturnPastEventsForOrganization() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var pastEvent1 = createEvent("Event1", LocalDate.now().minusDays(1), organization, user);
        var pastEvent2 = createEvent("Event2", LocalDate.now().minusDays(2), organization, user);
        var futureEvent = createEvent("Event2", LocalDate.now().plusDays(2), organization, user);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUser(user);
        testDataFactory.persistEvents(pastEvent1, pastEvent2, futureEvent);

        var result = nakitinDao.fetchPastEvents(organization.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(NameAndDateDto::name)
                .containsExactlyInAnyOrder(pastEvent1.getName(), pastEvent2.getName());
    }
}
