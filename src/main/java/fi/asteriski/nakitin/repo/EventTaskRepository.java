/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.repo;

import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTaskRepository extends JpaRepository<EventTaskEntity, UUID> {
    List<EventTaskEntity> readEventTaskEntitiesByIdIn(List<UUID> ids);

    List<EventTaskEntity> findAllByVolunteersContaining(Set<UserEntity> volunteers);
}
