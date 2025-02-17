/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record EventTaskDto(
    UUID id,
    String taskName,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    Integer personCount,
    Set<UserDto> volunteers) {

    public boolean isFull() {
        return volunteers.size() == personCount;
    }
    public boolean isNotFull() {
        return !isFull();
    }
}
