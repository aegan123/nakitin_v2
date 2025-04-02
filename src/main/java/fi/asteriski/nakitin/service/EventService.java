/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.EventDao;
import fi.asteriski.nakitin.dao.OrganizationDao;
import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.dto.EventForm;
import fi.asteriski.nakitin.entity.EventEntity;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventDao eventDao;
    private final OrganizationDao organizationDao;

    @Transactional
    public UUID createNewEvent(EventForm eventForm) {
        var org = organizationDao.fetchOrganizationByName(eventForm.getOrganizer());
        return eventDao.saveEvent(eventForm.toDto(), org);
    }

    @Transactional
    public UUID editEvent(EventForm eventForm) {
        var org = organizationDao.fetchOrganizationByName(eventForm.getOrganizer());
        var event = fetchEventById(eventForm.getEventId()).copy(eventForm, org);

        return eventDao.saveEvent(event, org);
    }

    public EventForm generateEventFormForEvent(UUID eventId) {
        var event = fetchEventById(eventId);
        return EventForm.builder()
                .eventId(eventId)
                .name(event.name())
                .venue(event.venue())
                .description(event.description())
                .date(event.date())
                .build();
    }

    private EventDto fetchEventById(UUID eventId) {
        return eventDao.fetchEventById(eventId);
    }

    public List<EventEntity> fetchEventsById(List<UUID> eventIds) {
        return eventDao.fetchEventsByIds(eventIds);
    }
}
