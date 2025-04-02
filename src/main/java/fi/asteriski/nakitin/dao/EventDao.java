/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.repo.EventRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventDao {
    private final EventRepository eventRepository;

    public UUID saveEvent(final EventDto dto, final OrganizationDto org) {
        var orgEntity = org.toEntity();
        var eventEntity = dto.toEntity();
        orgEntity.addEvent(eventEntity);
        return eventRepository.save(eventEntity).getId();
    }

    public EventDto fetchEventById(UUID eventId) {
        return eventRepository
                .findById(eventId)
                .map(EventEntity::toDto)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event not found with id '%s'.", eventId)));
    }

    public List<EventEntity> fetchEventsByIds(List<UUID> eventIds) {
        return eventRepository.readAllByIdIn(eventIds);
    }
}
