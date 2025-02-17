/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.dto.EventTaskForm;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.exceptions.EventTaskNotFoundException;
import fi.asteriski.nakitin.repo.EventRepository;
import fi.asteriski.nakitin.repo.EventTaskRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@AllArgsConstructor
public class NakitinDao {
    private final EventRepository eventRepository;
    private final EventTaskRepository eventTaskRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public List<EventDto> fetchUpcomingEvents() {
        var now = LocalDate.now();
        return eventRepository
            .findAllByDateBetween(now, now.plusYears(1L), Limit.of(20), Sort.by(Sort.Direction.ASC, "date"))
            .stream()
            .map(EventEntity::toDto)
            .toList();
    }

    public EventDto fetchEvent(UUID eventId) {
        return eventRepository
            .findById(eventId)
            .map(EventEntity::toDto)
            .orElseThrow(() -> new EventNotFoundException(
                messageSource.getMessage("error.event.not-found.label", null, Locale.getDefault()) + " " + eventId + "."));
    }

    public EventTaskEntity getEventTaskById(UUID taskId) {
        return eventTaskRepository.findById(taskId)
            .orElseThrow(() -> new EventTaskNotFoundException(
                messageSource.getMessage("error.event-task.not-found.label", null, Locale.getDefault()) + " " + taskId + ".")
            );
    }

    public void saveUser(UserEntity loggedInUser) {
        userRepository.save(loggedInUser);
    }

    public void createNewEventTask(EventTaskForm eventTaskDto, UUID eventId) {
        var event = eventRepository.getReferenceById(eventId);
        var entity = eventTaskDto.toEntity();
        event.addTask(entity);
        eventTaskRepository.save(entity);
    }

    public void editEventTask(EventTaskForm eventTaskDto) {
        var entity = eventTaskRepository.findById(eventTaskDto.getId())
            .orElseThrow(() -> new EventTaskNotFoundException(messageSource.getMessage("error.event-task.not-found.label", null, Locale.getDefault()) + " " + eventTaskDto.getId() + "."));
        entity.setDate(eventTaskDto.getDate());
        entity.setTaskName(eventTaskDto.getName());
        entity.setStartTime(eventTaskDto.getStartTime());
        entity.setEndTime(eventTaskDto.getEndTime());
        entity.setPersonCount(eventTaskDto.getPersonCount());
        eventTaskRepository.save(entity);
    }

    public EventDto getEventById(UUID eventId) {
        return eventRepository.findById(eventId)
            .map(EventEntity::toDto)
            .orElseThrow(() -> new EventNotFoundException(
                messageSource.getMessage("error.event.not-found.label", null, Locale.getDefault()) + " " + eventId + "."));
    }
}
