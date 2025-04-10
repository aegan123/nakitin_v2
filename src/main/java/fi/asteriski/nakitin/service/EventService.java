/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    private final EventDao eventDao;
    private final OrganizationDao organizationDao;
    private final UserService userService;

    @Transactional
    public UUID createNewEvent(EventForm eventForm, UserEntity user) {
        var org = organizationDao.fetchOrganizationByName(eventForm.getOrganizer());
        return eventDao.saveEvent(eventForm.toDto(), org, user);
    }

    @Transactional
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
    public void deleteEventById(UUID id) {
        var event = fetchEventEntityById(id);
        var users = event.getTasks().stream()
                .map(task ->
                        task.getVolunteers().stream().map(UserEntity::getId).toList())
                .map(userService::fetchUsersByIds)
                .flatMap(List::stream)
                .toList();
        event.getTasks()
                .forEach(task -> users.forEach(user -> {
                    user.removeEventTask(task);
                    user.removeEvent(event);
                }));
        event.getOrganizer().removeEvent(event);

        eventDao.deleteEvent(id);
    }

    public EventEntity fetchEventEntityById(UUID id) {
        return eventDao.fetchEventEntityById(id);
    }

    public List<IdAndNameDto> fetchUsersOrganizations(UUID userId) {
        return organizationDao.fetchUsersOrganizations(userId);
    }
}
