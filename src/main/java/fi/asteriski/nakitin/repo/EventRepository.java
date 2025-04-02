/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.repo;

import fi.asteriski.nakitin.entity.EventEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    List<EventEntity> findAllByDateBetween(LocalDate dateAfter, LocalDate dateBefore, Limit limit, Sort sort);

    List<EventEntity> findAllByOrganizer_IdAndDateBefore(
            UUID organizerId, LocalDate dateBefore, Sort sort, Limit limit);

    List<EventEntity> findAllByOrganizer_IdAndDateAfter(UUID organizerId, LocalDate dateAfter, Sort sort);

    List<EventEntity> readAllByIdIn(List<UUID> ids);
}
