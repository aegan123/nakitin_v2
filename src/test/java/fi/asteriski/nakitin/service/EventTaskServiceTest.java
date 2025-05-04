/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
*/
package fi.asteriski.nakitin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fi.asteriski.nakitin.dao.EventTaskDao;
import fi.asteriski.nakitin.exceptions.EventTaskNotFoundException;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventTaskServiceTest extends TestFixtures {

    @Mock
    private EventTaskDao eventTaskDao;

    private EventTaskService eventTaskService;

    @BeforeEach
    void setUp() {
        eventTaskService = new EventTaskService(eventTaskDao);
    }

    @Test
    void deleteTask_whenTaskExists_shouldDeleteTaskAndRemoveAllAssociations() {
        var organization = createDefaultOrganization();
        var user = createDefaultUser();
        var event = createEvent("Test Event", LocalDate.now(), organization, user);
        var task = createEventTask("Test Task", LocalDate.now(), 2);
        var volunteer1 = createDefaultUser();
        var volunteer2 = createDefaultUser();

        task.setVolunteers(new LinkedHashSet<>(Set.of(volunteer1, volunteer2)));
        event.addTask(task);

        when(eventTaskDao.fetchTask(task.getId())).thenReturn(task);

        eventTaskService.deleteTask(task.getId());

        verify(eventTaskDao).deleteTask(task);
        assertThat(event.getTasks()).isEmpty();
        assertThat(volunteer1.getEventTasks()).isEmpty();
        assertThat(volunteer2.getEventTasks()).isEmpty();
    }

    @Test
    void deleteTask_whenTaskDoesNotExist_shouldThrowException() {
        var taskId = UUID.randomUUID();
        when(eventTaskDao.fetchTask(taskId)).thenThrow(new EventTaskNotFoundException("Task not found"));

        assertThatThrownBy(() -> eventTaskService.deleteTask(taskId))
                .isInstanceOf(EventTaskNotFoundException.class)
                .hasMessage("Task not found");

        verify(eventTaskDao, never()).deleteTask(any());
    }

    @Test
    void deleteTask_withMultipleVolunteers_shouldRemoveAllVolunteerAssociations() {
        var organization = createDefaultOrganization();
        var event = createEvent("Test Event", LocalDate.now(), organization, createDefaultUser());
        var task = createEventTask("Test Task", LocalDate.now(), 5);
        var volunteers = new LinkedHashSet<>(Set.of(createDefaultUser(), createDefaultUser(), createDefaultUser()));

        task.setEvent(event);
        task.setVolunteers(volunteers);
        event.getTasks().add(task);

        when(eventTaskDao.fetchTask(task.getId())).thenReturn(task);

        eventTaskService.deleteTask(task.getId());

        verify(eventTaskDao).deleteTask(task);
        assertThat(event.getTasks()).isEmpty();
        volunteers.forEach(volunteer -> assertThat(volunteer.getEventTasks()).isEmpty());
    }

    @Test
    void deleteTask_withNoVolunteers_shouldOnlyRemoveEventAssociation() {
        var organization = createDefaultOrganization();
        var event = createEvent("Test Event", LocalDate.now(), organization, createDefaultUser());
        var task = createEventTask("Test Task", LocalDate.now(), 2);

        task.setEvent(event);
        event.getTasks().add(task);

        when(eventTaskDao.fetchTask(task.getId())).thenReturn(task);

        eventTaskService.deleteTask(task.getId());

        verify(eventTaskDao).deleteTask(task);
        assertThat(event.getTasks()).isEmpty();
    }
}
