/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.repo;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.repo.projection.DateProjection;
import fi.asteriski.nakitin.repo.projection.NameAndDateProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    List<EventEntity> findAllByDateBetween(LocalDate dateAfter, LocalDate dateBefore, Limit limit, Sort sort);

    List<NameAndDateProjection> findAllByOrganizer_IdAndDateBefore(
            UUID organizerId, LocalDate dateBefore, Sort sort, Limit limit);

    List<NameAndDateProjection> findAllByOrganizer_IdAndDateAfter(UUID organizerId, LocalDate dateAfter, Sort sort);

    List<EventEntity> readAllByIdIn(List<UUID> ids);

    @NativeQuery(
            value = """
        select id, name, venue, description, date from events where id = :eventId
        """)
    EventDto fetchEventDetails(@Param("eventId") UUID eventId);

    Optional<DateProjection> findDateById(UUID id);

    @EntityGraph(value = "graph_event_full")
    Page<EventEntity> findAll(Pageable pageable);
}
