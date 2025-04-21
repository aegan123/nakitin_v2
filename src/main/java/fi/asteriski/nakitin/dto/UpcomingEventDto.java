/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;
import org.springframework.context.i18n.LocaleContextHolder;

public record UpcomingEventDto(UUID id, String name, String organizer, LocalDate date) {
    public String localeFormattedDate() {
        var formatter =
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LocaleContextHolder.getLocale());

        return date.format(formatter);
    }
}
