/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.entity.EventTaskEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class EventTaskForm {
    private UUID id;

    @NotBlank(message = "{validation.task.name.notBlank}")
    @NonNull
    private String taskName;

    private LocalDate date;

    @NotNull(message = "{validation.task.startTime.notBlank}")
    @NonNull
    private LocalTime startTime;

    @NotNull(message = "{validation.task.endTime.notBlank}")
    @NonNull
    private LocalTime endTime;

    @NonNull
    @NotNull(message = "{validation.task.personCount.notBlank}")
    @Min(value = 1, message = "{validation.task.personCount.min}")
    private Integer personCount;

    private UUID eventId;

    public EventTaskEntity toEntity() {
        return EventTaskEntity.builder()
                .id(id)
                .taskName(taskName)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .personCount(personCount)
                .build();
    }
}
