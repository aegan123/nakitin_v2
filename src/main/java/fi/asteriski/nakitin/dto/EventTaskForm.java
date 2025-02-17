/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.entity.EventTaskEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class EventTaskForm {
    private UUID id;
    @NotBlank(message = "Nimi on pakollinen tieto")
    @NonNull
    private String name;
    @NotNull(message = "Päivämäärä on pakollinen tieto")
    @NonNull
    private LocalDate date;
    @NotNull(message = "Aloitusaika on pakollinen tieto")
    @NonNull
    private LocalTime startTime;
    @NotNull(message = "Lopetusaika on pakollinen tieto")
    @NonNull
    private LocalTime endTime;
    @NonNull
    @NotNull(message = "Henkilömäärä on pakollinen tieto")
    @Min(1)
    private Integer personCount;
    private UUID eventId;

    public EventTaskEntity toEntity() {
        return EventTaskEntity.builder()
            .id(id)
            .taskName(name)
            .date(date)
            .startTime(startTime)
            .endTime(endTime)
            .personCount(personCount)
            .build();
    }
}
