/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.utils.LocaleDateFormattable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.Builder;

@Builder
public record EventDto(
        UUID id,
        String name,
        String venue,
        String description,
        String abbreviatedDescription,
        LocalDate date,
        OrganizationDto organizer,
        List<EventTaskDto> tasks,
        ZonedDateTime createdAt)
        implements LocaleDateFormattable {

    public EventDto sorted() {
        return EventDto.builder()
                .id(id)
                .name(name)
                .venue(venue)
                .description(description)
                .date(date)
                .organizer(organizer)
                .tasks(tasks.stream()
                        .sorted(Comparator.comparing(EventTaskDto::date).thenComparing(EventTaskDto::startTime))
                        .toList())
                .build();
    }

    public EventEntity toEntity() {
        return EventEntity.builder()
                .id(id)
                .name(name)
                .venue(venue)
                .description(description)
                .date(date)
                .organizer(organizer != null ? organizer.toEntity() : null)
                .createdAt(createdAt)
                .build();
    }

    @Override
    public LocalDate getDate() {
        return date;
    }
}
