/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.admin;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.EventService;
import fi.asteriski.nakitin.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminEventService {
    private final EventService eventService;
    private final UserService userService;

    public List<EventDto> fetchAllEvents() {
        return eventService.fetchAllEvents();
    }

    public EventDto fetchEvent(UUID id) {
        return eventService.fetchEventById(id);
    }

    @Transactional
    public void deleteEvent(UUID id) {
        var event = eventService.fetchEventEntityById(id);
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

        eventService.deleteEventById(event.getId());
    }
}
