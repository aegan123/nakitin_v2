/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.entity.EventEntity;
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
        LocalDate date,
        OrganizationDto organizer,
        List<EventTaskDto> tasks,
        ZonedDateTime createdAt) {
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
                .organizer(organizer.toEntity())
                .createdAt(createdAt)
                .build();
    }

    public EventDto copy(EventForm eventForm, OrganizationDto org) {
        return EventDto.builder()
                .id(eventForm.getEventId())
                .name(eventForm.getName())
                .venue(eventForm.getVenue())
                .description(eventForm.getDescription())
                .date(eventForm.getDate())
                .organizer(org)
                .createdAt(createdAt)
                .build();
    }
}
