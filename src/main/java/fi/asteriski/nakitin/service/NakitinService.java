/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.NakitinDao;
import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.dto.EventTaskDto;
import fi.asteriski.nakitin.dto.EventTaskForm;
import fi.asteriski.nakitin.dto.ExportDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Log4j2
public class NakitinService {
    private final NakitinDao nakitinDao;

    public List<EventDto> fetchUpcomingEvents() {
        return nakitinDao.fetchUpcomingEvents();
    }

    public EventDto fetchEvent(UUID eventId) {
        return nakitinDao.fetchEvent(eventId).sorted();
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
        user.addEventTask(nakitinDao.getEventTaskById(taskId));
        nakitinDao.saveUser(user);
    }

    @Transactional
    public void cancelVolunteeringToTask(UUID taskId, UserEntity loggedInUser) {
        var user = nakitinDao.fetchUser(loggedInUser.getUsername());
        user.removeEventTask(nakitinDao.getEventTaskById(taskId));
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

    private String generateCsv(Set<EventTaskDto> tasks) {
        var sw = new StringWriter();

        var csvFormat = CSVFormat.EXCEL
                .builder()
                .setHeader("Nakki", "Pvm", "Klo", "Etunimi", "Sukunimi", "Sähköposti")
                .get();

        try (final var printer = new CSVPrinter(sw, csvFormat)) {
            tasks.forEach(dto -> dto.volunteers().forEach(volunteer -> {
                try {
                    printer.printRecord(
                            dto.taskName(),
                            dto.date(),
                            String.format("%s - %s", dto.startTime(), dto.endTime()),
                            volunteer.firstName(),
                            volunteer.lastName(),
                            volunteer.email());
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
}
