/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.repo.EventRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventDao {
    private final EventRepository eventRepository;

    public UUID saveEvent(EventDto dto) {
        return eventRepository.save(dto.toEntity()).getId();
    }

    public EventDto fetchEventById(UUID eventId) {
        return eventRepository
                .findById(eventId)
                .map(EventEntity::toDto)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event not found with id '%s'.", eventId)));
    }
}
