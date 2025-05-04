/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
*/
package fi.asteriski.nakitin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fi.asteriski.nakitin.dao.NakitinDao;
import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.exceptions.CsvExportException;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.test.builders.TestEventBuilder;
import fi.asteriski.nakitin.test.builders.TestUserBuilder;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
class NakitinServiceTest extends TestFixtures {

    @Mock
    private NakitinDao nakitinDao;

    @Mock
    private MessageSource messageSource;

    private NakitinService nakitinService;

    @BeforeEach
    void setUp() {
        nakitinService = new NakitinService(nakitinDao, messageSource);
    }

    @Test
    void exportVolunteers_whenEventHasTasksWithVolunteers_shouldGenerateCorrectCsv() {
        mockCsvHeaders();
        var eventId = UUID.randomUUID();
        var volunteer1 = TestUserBuilder.builder()
                .withFirstName("John")
                .withLastName("Doe")
                .withEmail("john@example.com")
                .build();
        var volunteer2 = TestUserBuilder.builder()
                .withFirstName("Jane")
                .withLastName("Smith")
                .withEmail("jane@example.com")
                .build();

        var event = TestEventBuilder.builder()
                .withName("Test Event")
                .withDate(LocalDate.parse("2024-03-15"))
                .build();

        var task1 = createEventTask("Task 1", LocalDate.parse("2024-03-15"), 2);
        var task2 = createEventTask("Task 2", LocalDate.parse("2024-03-15"), 2);
        task1.setVolunteers(Set.of(volunteer1));
        task2.setVolunteers(Set.of(volunteer1, volunteer2));

        var eventDto = EventDto.builder()
                .id(eventId)
                .name(event.getName())
                .date(event.getDate())
                .tasks(List.of(task1.toDto(), task2.toDto()))
                .build();

        when(nakitinDao.getEventById(eventId)).thenReturn(eventDto);

        var result = nakitinService.exportVolunteers(eventId);

        assertThat(result.eventName()).isEqualTo("Test Event");
        assertThat(result.csv())
                .contains("\"Task\",\"Date\",\"Time\",\"First Name\",\"Last Name\",\"Email\"")
                .contains("\"Task 1\"")
                .contains("\"Task 2\"")
                .contains("\"John\",\"Doe\",\"john@example.com\"")
                .contains("\"Jane\",\"Smith\",\"jane@example.com\"");
    }

    @Test
    void exportVolunteers_whenEventHasNoTasks_shouldGenerateOnlyHeaders() {
        mockCsvHeaders();
        var eventId = UUID.randomUUID();
        var event = TestEventBuilder.builder()
                .withName("Empty Event")
                .withDate(LocalDate.now())
                .build();

        var eventDto = EventDto.builder()
                .id(eventId)
                .name(event.getName())
                .date(event.getDate())
                .tasks(List.of())
                .build();

        when(nakitinDao.getEventById(eventId)).thenReturn(eventDto);

        var result = nakitinService.exportVolunteers(eventId);

        assertThat(result.eventName()).isEqualTo("Empty Event");
        assertThat(result.csv())
                .contains("\"Task\",\"Date\",\"Time\",\"First Name\",\"Last Name\",\"Email\"")
                .doesNotContain("\"Task 1\"")
                .doesNotContain("\"John\"");
    }

    @Test
    void exportVolunteers_whenEventHasTasksWithNoVolunteers_shouldGenerateOnlyHeaders() {
        mockCsvHeaders();
        var eventId = UUID.randomUUID();
        var event = TestEventBuilder.builder()
                .withName("Event With Empty Tasks")
                .withDate(LocalDate.now())
                .build();

        var task = createEventTask("Empty Task", LocalDate.now(), 2);

        var eventDto = EventDto.builder()
                .id(eventId)
                .name(event.getName())
                .date(event.getDate())
                .tasks(List.of(task.toDto()))
                .build();

        when(nakitinDao.getEventById(eventId)).thenReturn(eventDto);

        var result = nakitinService.exportVolunteers(eventId);

        assertThat(result.eventName()).isEqualTo("Event With Empty Tasks");
        assertThat(result.csv())
                .contains("\"Task\",\"Date\",\"Time\",\"First Name\",\"Last Name\",\"Email\"")
                .doesNotContain("\"Empty Task\"");
    }

    @Test
    void exportVolunteers_whenEventNotFound_shouldThrowException() {
        var eventId = UUID.randomUUID();
        when(nakitinDao.getEventById(eventId)).thenThrow(new EventNotFoundException("Event not found"));

        assertThatThrownBy(() -> nakitinService.exportVolunteers(eventId))
                .isInstanceOf(CsvExportException.class)
                .hasMessageContaining("Could not find event with id: " + eventId);
    }

    @Test
    void exportVolunteers_withSpecialCharacters_shouldEscapeCorrectly() {
        mockCsvHeaders();
        var eventId = UUID.randomUUID();
        var volunteer = TestUserBuilder.builder()
                .withFirstName("John \"Quote\"")
                .withLastName("O'Connor")
                .withEmail("john,comma@example.com")
                .build();

        var event = TestEventBuilder.builder()
                .withName("Test Event")
                .withDate(LocalDate.now())
                .build();

        var task = createEventTask("Task \"Special\"", LocalDate.now(), 2);
        task.setVolunteers(Set.of(volunteer));

        var eventDto = EventDto.builder()
                .id(eventId)
                .name(event.getName())
                .date(event.getDate())
                .tasks(List.of(task.toDto()))
                .build();

        when(nakitinDao.getEventById(eventId)).thenReturn(eventDto);

        var result = nakitinService.exportVolunteers(eventId);

        assertThat(result.csv())
                .contains("Task \\\"Special\\\"")
                .contains("John \\\"Quote\\\"")
                .contains("O'Connor")
                .contains("john,comma@example.com");
    }

    @Test
    void exportVolunteers_withMultipleVolunteersPerTask_shouldGenerateCorrectRows() {
        mockCsvHeaders();
        var eventId = UUID.randomUUID();
        var volunteers = List.of(
                TestUserBuilder.builder()
                        .withFirstName("John")
                        .withLastName("Doe")
                        .withEmail("john@example.com")
                        .build(),
                TestUserBuilder.builder()
                        .withFirstName("Jane")
                        .withLastName("Smith")
                        .withEmail("jane@example.com")
                        .build(),
                TestUserBuilder.builder()
                        .withFirstName("Bob")
                        .withLastName("Wilson")
                        .withEmail("bob@example.com")
                        .build());

        var event = TestEventBuilder.builder()
                .withName("Multi-Volunteer Event")
                .withDate(LocalDate.parse("2024-03-15"))
                .build();

        var task = createEventTask("Popular Task", LocalDate.parse("2024-03-15"), 3);
        task.setVolunteers(Set.copyOf(volunteers));

        var eventDto = EventDto.builder()
                .id(eventId)
                .name(event.getName())
                .date(event.getDate())
                .tasks(List.of(task.toDto()))
                .build();

        when(nakitinDao.getEventById(eventId)).thenReturn(eventDto);

        var result = nakitinService.exportVolunteers(eventId);

        assertThat(result.csv().split("\r\n"))
                .hasSize(4) // Header + 3 volunteers
                .allMatch(line -> line.startsWith("\"") && line.endsWith("\""))
                .contains(
                        "\"Task\",\"Date\",\"Time\",\"First Name\",\"Last Name\",\"Email\"",
                        "\"Popular Task\",\"15.3.2024\",\"%s - %s\",\"John\",\"Doe\",\"john@example.com\""
                                .formatted(task.localeFormattedStartTime(), task.localeFormattedEndTime()),
                        "\"Popular Task\",\"15.3.2024\",\"%s - %s\",\"Jane\",\"Smith\",\"jane@example.com\""
                                .formatted(task.localeFormattedStartTime(), task.localeFormattedEndTime()),
                        "\"Popular Task\",\"15.3.2024\",\"%s - %s\",\"Bob\",\"Wilson\",\"bob@example.com\""
                                .formatted(task.localeFormattedStartTime(), task.localeFormattedEndTime()));
    }

    private void mockCsvHeaders() {
        when(messageSource.getMessage("export.header.task", null, LocaleContextHolder.getLocale()))
                .thenReturn("Task");
        when(messageSource.getMessage("export.header.date", null, LocaleContextHolder.getLocale()))
                .thenReturn("Date");
        when(messageSource.getMessage("export.header.time", null, LocaleContextHolder.getLocale()))
                .thenReturn("Time");
        when(messageSource.getMessage("export.header.firstName", null, LocaleContextHolder.getLocale()))
                .thenReturn("First Name");
        when(messageSource.getMessage("export.header.lastName", null, LocaleContextHolder.getLocale()))
                .thenReturn("Last Name");
        when(messageSource.getMessage("export.header.email", null, LocaleContextHolder.getLocale()))
                .thenReturn("Email");
    }
}
