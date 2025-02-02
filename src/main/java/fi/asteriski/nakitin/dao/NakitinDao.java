/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.repo.EventRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NakitinDao {
    private final EventRepository eventRepository;

    public List<EventDto> fetchUpcomingEvents() {
        var now = LocalDate.now();
        return eventRepository
                .findAllByDateBetween(now, now.plusYears(1L), Limit.of(20), Sort.by(Sort.Direction.ASC, "date"))
                .stream()
                .map(EventEntity::toDto)
                .toList();
    }
}
