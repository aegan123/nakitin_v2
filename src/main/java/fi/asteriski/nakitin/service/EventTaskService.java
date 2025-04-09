/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.EventTaskDao;
import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Transactional(readOnly = true)
@Service
public class EventTaskService {
    private final EventTaskDao eventTaskDao;

    public List<EventTaskEntity> fetchEventTasksById(List<UUID> taskIds) {
        return eventTaskDao.fetchEventTasksByIds(taskIds);
    }

    public List<EventTaskEntity> fetchUsersEventTasks(UserEntity id) {
        var tasks = eventTaskDao.fetchUsersEventTasks(id);
        // Work-around for lazy loading problem.
        tasks.forEach(EventTaskEntity::toString);
        return tasks;
    }
}
