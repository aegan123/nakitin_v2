/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.NakitinDao;
import fi.asteriski.nakitin.dto.*;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.exceptions.CsvExportException;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Log4j2
public class NakitinService {
    private final NakitinDao nakitinDao;
    private final MessageSource messageSource;

    public List<UpcomingEventDto> fetchUpcomingEvents() {
        return nakitinDao.fetchUpcomingEvents();
    }

    public EventDto fetchEvent(UUID eventId) {
        return nakitinDao.fetchEventForEventPage(eventId).sorted();
    }

    public EventDto fetchEventForEventPage(UUID eventId) {
        return nakitinDao.fetchEventForEventPage(eventId).sorted();
    }

    @Transactional
    public void addEventTask(@Valid EventTaskForm eventTaskDto) {
        if (eventTaskDto.getDate() == null) {
            eventTaskDto.setDate(nakitinDao.fetchEventDate(eventTaskDto.getEventId()));
        }
        nakitinDao.createNewEventTask(eventTaskDto);
    }

    @Transactional
    public void editEventTask(@Valid EventTaskForm eventTaskDto) {
        nakitinDao.editEventTask(eventTaskDto);
    }

    @Transactional
    public void volunteerToTask(UUID taskId, UserEntity loggedInUser) {
        var user = nakitinDao.fetchUser(loggedInUser.getUsername());
        user.addEventTask(nakitinDao.fetchReference(taskId));
        nakitinDao.saveUser(user);
    }

    @Transactional
    public void cancelVolunteeringToTask(UUID taskId, UserEntity loggedInUser) {
        var user = nakitinDao.fetchUser(loggedInUser.getUsername());
        user.removeEventTask(nakitinDao.fetchReference(taskId));
        nakitinDao.saveUser(user);
    }

    public ExportDto exportVolunteers(UUID eventId) {
        EventDto event;
        try {
            event = nakitinDao.getEventById(eventId);
        } catch (EventNotFoundException e) {
            throw new CsvExportException("Could not find event with id: " + eventId, e);
        }
        return ExportDto.builder()
                .eventName(event.name())
                .csv(generateCsv(event.tasks()))
                .build();
    }

    private String generateCsv(List<EventTaskDto> tasks) {
        var sw = new StringWriter();

        var csvFormat = CSVFormat.EXCEL
                .builder()
                .setHeader(
                        getHeaderName(ExportHeader.TASK),
                        getHeaderName(ExportHeader.DATE),
                        getHeaderName(ExportHeader.TIME),
                        getHeaderName(ExportHeader.FIRST_NAME),
                        getHeaderName(ExportHeader.LAST_NAME),
                        getHeaderName(ExportHeader.EMAIL))
                .get();

        try (final var printer = new CSVPrinter(sw, csvFormat)) {
            tasks.forEach(dto -> dto.volunteers().forEach(volunteer -> {
                try {
                    printer.printRecord(
                            dto.taskName(),
                            dto.localeFormattedDate(),
                            "%s - %s".formatted(dto.localeFormattedStartTime(), dto.localeFormattedEndTime()),
                            volunteer.getFirstName(),
                            volunteer.getLastName(),
                            volunteer.getEmail());
                } catch (IOException | IllegalArgumentException e) {
                    log.debug("Error printing record", e);
                }
            }));
        } catch (IOException e) {
            log.error("Error generating CSV", e);
            throw new CsvExportException("Csv export failed.", e);
        }

        return sw.toString().trim();
    }

    public List<NameAndDateDto> fetchUpcomingEvents(UUID organizationId) {
        return nakitinDao.fetchUpcomingEvents(organizationId);
    }

    public List<NameAndDateDto> fetchPastEvents(UUID organizationId) {
        return nakitinDao.fetchPastEvents(organizationId);
    }

    private String getHeaderName(ExportHeader header) {
        return switch (header) {
            case TASK -> messageSource.getMessage("export.header.task", null, LocaleContextHolder.getLocale());
            case DATE -> messageSource.getMessage("export.header.date", null, LocaleContextHolder.getLocale());
            case TIME -> messageSource.getMessage("export.header.time", null, LocaleContextHolder.getLocale());
            case FIRST_NAME -> messageSource.getMessage(
                    "export.header.firstName", null, LocaleContextHolder.getLocale());
            case LAST_NAME -> messageSource.getMessage("export.header.lastName", null, LocaleContextHolder.getLocale());
            case EMAIL -> messageSource.getMessage("export.header.email", null, LocaleContextHolder.getLocale());
        };
    }

    private enum ExportHeader {
        TASK,
        DATE,
        TIME,
        FIRST_NAME,
        LAST_NAME,
        EMAIL
    }
}
