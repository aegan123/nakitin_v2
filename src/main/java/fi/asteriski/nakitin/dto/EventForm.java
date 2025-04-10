/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.validation.DateMustBeInTheFuture;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventForm {
    @NotBlank(message = "Nimi on pakollinen tieto")
    private String name;

    @NotBlank(message = "Venue on pakollinen tieto")
    private String venue;

    @NotBlank(message = "Kuvaus on pakollinen tieto")
    private String description;

    @NotNull(message = "Ajankohta täytyy antaa")
    @DateMustBeInTheFuture(message = "Tapahtumapäivän tulee olla tulevaisuudessa")
    private LocalDate date;

    @NotBlank(message = "Järjestäjä on pakollinen tieto")
    private String organizer;

    private UUID eventId;

    public EventDto toDto() {
        return EventDto.builder()
                .name(name)
                .venue(venue)
                .description(description)
                .date(date)
                .build();
    }
}
