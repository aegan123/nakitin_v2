/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.admin;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.service.EventService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminEventService {
    private final EventService eventService;

    public Page<EventEntity> fetchAllEvents(int page) {
        return eventService.fetchAllEventsForAdmin(page);
    }

    public EventDto fetchEvent(UUID id) {
        return eventService.fetchEventById(id);
    }

    @Transactional
    public void deleteEvent(UUID id) {
        eventService.deleteEventById(id);
    }
}
