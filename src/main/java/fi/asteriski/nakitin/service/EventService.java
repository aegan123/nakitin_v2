/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import static fi.asteriski.nakitin.utils.Constants.CACHE_NAME_EVENTS;

import fi.asteriski.nakitin.dao.EventDao;
import fi.asteriski.nakitin.dao.OrganizationDao;
import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.dto.EventForm;
import fi.asteriski.nakitin.dto.IdAndNameDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventDao eventDao;
    private final OrganizationDao organizationDao;

    @Transactional
    public UUID createNewEvent(EventForm eventForm, UserEntity user) {
        var org = organizationDao.fetchOrganizationByName(eventForm.getOrganizer());
        return eventDao.saveEvent(eventForm.toDto(), org, user);
    }

    @Transactional
    @CacheEvict(cacheNames = CACHE_NAME_EVENTS, key = "#p0.eventId")
    public UUID editEvent(EventForm eventForm, UserEntity user) {
        var org = organizationDao.fetchOrganizationByName(eventForm.getOrganizer());
        var event = fetchEventById(eventForm.getEventId()).copy(eventForm, org);

        return eventDao.saveEvent(event, org, user);
    }

    public EventForm generateEventFormForEvent(UUID eventId) {
        var event = eventDao.fetchEventDetailsById(eventId);
        return EventForm.builder()
                .eventId(eventId)
                .name(event.name())
                .venue(event.venue())
                .description(event.description())
                .date(event.date())
                .build();
    }

    public EventDto fetchEventById(UUID eventId) {
        return eventDao.fetchEventById(eventId);
    }

    public List<EventEntity> fetchEventsById(List<UUID> eventIds) {
        return eventDao.fetchEventsByIds(eventIds);
    }

    public Page<EventEntity> fetchAllEventsForAdmin(int page) {
        return eventDao.fetchAllEventsForAdmin(page);
    }

    @Transactional
    public void deleteEventById(UUID event) {
        eventDao.deleteEvent(event);
    }

    public EventEntity fetchEventEntityById(UUID id) {
        return eventDao.fetchEventEntityById(id);
    }

    public List<IdAndNameDto> fetchUsersOrganizations(UUID userId) {
        return organizationDao.fetchUsersOrganizations(userId);
    }
}
