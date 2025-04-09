/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static fi.asteriski.nakitin.utils.Constants.MAX_PAGE_SIZE;
import static fi.asteriski.nakitin.utils.Constants.SORT_BY_ID_ASC;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.repo.EventRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventDao {
    private final EventRepository eventRepository;

    public UUID saveEvent(final EventDto dto, final OrganizationDto org, UserEntity user) {
        var orgEntity = org.toEntity();
        var eventEntity = dto.toEntity();
        orgEntity.addEvent(eventEntity);
        eventEntity.setCreatedBy(user);
        return eventRepository.save(eventEntity).getId();
    }

    public EventDto fetchEventById(UUID eventId) {
        return fetchById(eventId).toEventPageDto();
    }

    public List<EventEntity> fetchEventsByIds(List<UUID> eventIds) {
        return eventRepository.readAllByIdIn(eventIds);
    }

    public Page<EventEntity> fetchAllEventsForAdmin(int page) {
        return eventRepository.findAll(PageRequest.of(page, MAX_PAGE_SIZE, SORT_BY_ID_ASC));
    }

    public EventEntity fetchEventEntityById(UUID id) {
        return fetchById(id);
    }

    private EventEntity fetchById(UUID id) {
        return eventRepository
                .findById(id)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event not found with id '%s'.", id)));
    }

    public void deleteEvent(UUID event) {
        eventRepository.deleteById(event);
    }

    public EventDto fetchEventDetailsById(UUID eventId) {
        return eventRepository.fetchEventDetails(eventId);
    }
}
