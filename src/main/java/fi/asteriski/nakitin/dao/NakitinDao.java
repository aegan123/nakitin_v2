/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.dto.EventTaskForm;
import fi.asteriski.nakitin.dto.NameAndDateDto;
import fi.asteriski.nakitin.dto.UpcomingEventDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.exceptions.EventTaskNotFoundException;
import fi.asteriski.nakitin.repo.EventRepository;
import fi.asteriski.nakitin.repo.EventTaskRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.repo.projection.DateProjection;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Limit;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NakitinDao {
    private final EventRepository eventRepository;
    private final EventTaskRepository eventTaskRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Cacheable(cacheNames = CACHE_NAME_EVENTS)
    public List<UpcomingEventDto> fetchUpcomingEvents() {
        var now = LocalDate.now();
        return eventRepository.findAllByDateBetween(now, now.plusYears(1L), Limit.of(20), SORT_BY_DATE_ASC).stream()
                .map(EventEntity::toUpcomingEventDto)
                .toList();
    }

    @Cacheable(cacheNames = CACHE_NAME_EVENTS, key = "#p0")
    public EventDto fetchEventForEventPage(UUID eventId) {
        return eventRepository
                .findById(eventId)
                .map(EventEntity::toEventPageDto)
                .orElseThrow(() -> new EventNotFoundException(
                        messageSource.getMessage("error.event.not-found.label", null, Locale.getDefault()) + " "
                                + eventId + "."));
    }

    public EventTaskEntity fetchReference(UUID taskId) {
        return eventTaskRepository.getReferenceById(taskId);
    }

    public EventTaskEntity getEventTaskById(UUID taskId) {
        return eventTaskRepository
                .findById(taskId)
                .orElseThrow(() -> new EventTaskNotFoundException(
                        messageSource.getMessage("error.event-task.not-found.label", null, Locale.getDefault()) + " "
                                + taskId + "."));
    }

    @CacheEvict(cacheNames = CACHE_NAME_USERS)
    public void saveUser(UserEntity loggedInUser) {
        userRepository.save(loggedInUser);
    }

    public void createNewEventTask(EventTaskForm eventTaskDto) {
        var event = eventRepository.getReferenceById(eventTaskDto.getEventId());
        var entity = eventTaskDto.toEntity();
        event.addTask(entity);
        eventTaskRepository.save(entity);
    }

    @CacheEvict(cacheNames = CACHE_NAME_TASKS, key = "#p0.id")
    public void editEventTask(EventTaskForm eventTaskDto) {
        var entity = eventTaskRepository
                .findById(eventTaskDto.getId())
                .orElseThrow(() -> new EventTaskNotFoundException(
                        messageSource.getMessage("error.event-task.not-found.label", null, Locale.getDefault()) + " "
                                + eventTaskDto.getId() + "."));
        entity.setDate(eventTaskDto.getDate());
        entity.setTaskName(eventTaskDto.getTaskName());
        entity.setStartTime(eventTaskDto.getStartTime());
        entity.setEndTime(eventTaskDto.getEndTime());
        entity.setPersonCount(eventTaskDto.getPersonCount());
        eventTaskRepository.save(entity);
    }

    @Cacheable(cacheNames = CACHE_NAME_EVENTS)
    public EventDto getEventById(UUID eventId) {
        return eventRepository
                .findById(eventId)
                .map(EventEntity::toEventPageDto)
                .orElseThrow(() -> new EventNotFoundException(
                        messageSource.getMessage("error.event.not-found.label", null, Locale.getDefault()) + " "
                                + eventId + "."));
    }

    @Cacheable(cacheNames = CACHE_NAME_USERS)
    public UserEntity fetchUser(@NonNull String userName) {
        return userRepository
                .findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    @Cacheable(cacheNames = CACHE_NAME_EVENTS)
    public LocalDate fetchEventDate(@NotNull UUID eventId) {
        return eventRepository
                .findDateById(eventId)
                .map(DateProjection::getDate)
                .orElseThrow(() -> new EventNotFoundException(
                        messageSource.getMessage("error.event.not-found.label", null, Locale.getDefault()) + " "
                                + eventId + "."));
    }

    @Cacheable(cacheNames = CACHE_NAME_EVENTS)
    public List<NameAndDateDto> fetchUpcomingEvents(UUID organizationId) {
        return eventRepository
                .findAllByOrganizer_IdAndDateAfter(organizationId, LocalDate.now(), SORT_BY_DATE_ASC)
                .stream()
                .map(e -> new NameAndDateDto(e.getName(), e.getDate()))
                .toList();
    }

    @Cacheable(cacheNames = CACHE_NAME_EVENTS)
    public List<NameAndDateDto> fetchPastEvents(UUID organizationId) {
        return eventRepository
                .findAllByOrganizer_IdAndDateBefore(organizationId, LocalDate.now(), SORT_BY_DATE_ASC, Limit.of(50))
                .stream()
                .map(e -> new NameAndDateDto(e.getName(), e.getDate()))
                .toList();
    }
}
