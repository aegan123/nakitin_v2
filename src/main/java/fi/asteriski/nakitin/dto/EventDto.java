/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record EventDto(
        UUID id,
        String name,
        String venue,
        String description,
        LocalDate date,
        OrganizationDto organizer,
        Set<EventTaskDto> tasks) {
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
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build();
    }
}
