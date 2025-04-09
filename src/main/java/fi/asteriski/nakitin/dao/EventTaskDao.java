/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static fi.asteriski.nakitin.utils.Constants.CACHE_NAME_TASKS;

import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.repo.EventTaskRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class EventTaskDao {
    private final EventTaskRepository eventTaskRepository;

    public List<EventTaskEntity> fetchEventTasksByIds(List<UUID> taskIds) {
        return eventTaskRepository.readEventTaskEntitiesByIdIn(taskIds);
    }

    @Cacheable(cacheNames = CACHE_NAME_TASKS)
    public List<EventTaskEntity> fetchUsersEventTasks(UserEntity id) {
        return eventTaskRepository.findAllByVolunteersContaining(Set.of(id));
    }
}
