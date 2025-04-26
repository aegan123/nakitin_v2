/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.utils.LocaleDateFormattable;
import java.time.LocalDate;
import java.util.UUID;

public record UpcomingEventDto(UUID id, String name, String organizer, LocalDate date)
        implements LocaleDateFormattable {
    @Override
    public LocalDate getDate() {
        return date;
    }
}
