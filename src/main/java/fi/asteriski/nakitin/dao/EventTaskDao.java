/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.exceptions.EventTaskNotFoundException;
import fi.asteriski.nakitin.repo.EventTaskRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class EventTaskDao {
    private final EventTaskRepository eventTaskRepository;

    public List<EventTaskEntity> fetchEventTasksByIds(List<UUID> taskIds) {
        return eventTaskRepository.readEventTaskEntitiesByIdIn(taskIds);
    }

    public List<EventTaskEntity> fetchUsersEventTasks(UserEntity id) {
        return eventTaskRepository.findAllByVolunteersContaining(Set.of(id));
    }

    public EventTaskEntity fetchTask(UUID id) {
        return eventTaskRepository
                .findById(id)
                .orElseThrow(() -> new EventTaskNotFoundException("Task with id " + id + " not found"));
    }

    public void deleteTask(EventTaskEntity task) {
        eventTaskRepository.delete(task);
    }
}
