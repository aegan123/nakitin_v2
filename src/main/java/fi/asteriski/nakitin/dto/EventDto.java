/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.entity.EventEntity;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import lombok.Builder;
import org.springframework.context.i18n.LocaleContextHolder;

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
        ZonedDateTime createdAt) {
    public EventDto(UUID id, String name, String venue, String description, java.sql.Date date) {
        this(id, name, venue, description, "", date.toLocalDate(), null, null, null);
    }

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

    public String localeFormattedDate() {
        var formatter =
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LocaleContextHolder.getLocale());

        return date.format(formatter);
    }
}
