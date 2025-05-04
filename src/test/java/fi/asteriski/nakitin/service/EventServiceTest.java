/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
*/
package fi.asteriski.nakitin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fi.asteriski.nakitin.dao.EventDao;
import fi.asteriski.nakitin.dao.OrganizationDao;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest extends TestFixtures {

    @Mock
    private EventDao eventDao;

    @Mock
    private OrganizationDao organizationDao;

    @Mock
    private UserService userService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventDao, organizationDao, userService);
    }

    @Test
    void deleteEventById_whenEventHasNoTasksOrVolunteers_shouldDeleteEvent() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, user);
        organization.addEvent(event);
        when(eventDao.fetchEventEntityById(event.getId())).thenReturn(event);

        eventService.deleteEventById(event.getId());

        verify(eventDao).deleteEvent(event.getId());
        assertThat(organization.getEvents()).isEmpty();
    }

    @Test
    void deleteEventById_whenEventHasTasksWithVolunteers_shouldRemoveAllAssociations() {
        var organization = createDefaultOrganization();
        var creator = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, creator);
        organization.addEvent(event);
        creator.addEvent(event);

        var volunteer1 = createDefaultUser();
        var volunteer2 = createDefaultUser();

        var task1 = createEventTask("Task 1", LocalDate.now(), 2);
        var task2 = createEventTask("Task 2", LocalDate.now(), 3);
        volunteer1.addEventTask(task1);
        volunteer2.addEventTask(task2);
        event.addTask(task1);
        event.addTask(task2);

        when(eventDao.fetchEventEntityById(event.getId())).thenReturn(event);
        when(userService.fetchUsersByIds(any())).thenReturn(List.of(volunteer1, volunteer2));

        eventService.deleteEventById(event.getId());

        verify(eventDao).deleteEvent(event.getId());
        assertThat(organization.getEvents()).isEmpty();
        assertThat(volunteer1.getEventTasks()).isEmpty();
        assertThat(volunteer2.getEventTasks()).isEmpty();
        assertThat(volunteer1.getEvents()).isEmpty();
        assertThat(volunteer2.getEvents()).isEmpty();
    }

    @Test
    void deleteEventById_whenEventDoesNotExist_shouldThrowException() {
        var eventId = UUID.randomUUID();
        when(eventDao.fetchEventEntityById(eventId)).thenThrow(new EventNotFoundException("Event not found"));

        assertThatThrownBy(() -> eventService.deleteEventById(eventId))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event not found");

        verify(eventDao, never()).deleteEvent(any());
    }

    @Test
    void deleteEventById_whenEventHasMultipleTasks_shouldRemoveAllTasksAndAssociations() {
        var organization = createDefaultOrganization();
        var creator = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, creator);
        organization.addEvent(event);

        var volunteers = List.of(createDefaultUser(), createDefaultUser(), createDefaultUser());
        var tasks = new LinkedHashSet<>(Set.of(
                createEventTask("Task 1", LocalDate.now(), 2),
                createEventTask("Task 2", LocalDate.now(), 3),
                createEventTask("Task 3", LocalDate.now(), 1)));

        tasks.forEach(
                task -> task.setVolunteers(new LinkedHashSet<>(Set.of(volunteers.getFirst(), volunteers.get(1)))));
        event.setTasks(tasks);

        when(eventDao.fetchEventEntityById(event.getId())).thenReturn(event);
        when(userService.fetchUsersByIds(any())).thenReturn(volunteers);

        eventService.deleteEventById(event.getId());

        verify(eventDao).deleteEvent(event.getId());
        volunteers.forEach(volunteer -> {
            assertThat(volunteer.getEventTasks()).isEmpty();
            assertThat(volunteer.getEvents()).isEmpty();
        });
        assertThat(organization.getEvents()).isEmpty();
    }

    @Test
    void deleteEventById_shouldCallUserServiceWithCorrectIds() {
        var organization = createDefaultOrganization();
        var creator = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, creator);
        organization.addEvent(event);

        var volunteer1 = createDefaultUser();
        var volunteer2 = createDefaultUser();

        var task = createEventTask("Task 1", LocalDate.now(), 2);
        task.setVolunteers(new HashSet<>(Set.of(volunteer1, volunteer2)));
        event.setTasks(new HashSet<>(Set.of(task)));

        when(eventDao.fetchEventEntityById(event.getId())).thenReturn(event);
        when(userService.fetchUsersByIds(any())).thenReturn(List.of(volunteer1, volunteer2));

        var userIdsCaptor = ArgumentCaptor.forClass(List.class);

        eventService.deleteEventById(event.getId());

        verify(userService).fetchUsersByIds(userIdsCaptor.capture());
        List<UUID> capturedIds = userIdsCaptor.getValue();
        assertThat(capturedIds).containsExactlyInAnyOrder(volunteer1.getId(), volunteer2.getId());
    }
}
