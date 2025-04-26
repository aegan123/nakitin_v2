/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.utils.LocaleDateFormattable;
import fi.asteriski.nakitin.utils.LocaleTimeFormattable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
public record EventTaskDto(
        UUID id,
        String taskName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Integer personCount,
        Set<UserDto> volunteers)
        implements LocaleDateFormattable, LocaleTimeFormattable {

    public boolean isFull() {
        return volunteers.size() == personCount;
    }

    public boolean isNotFull() {
        return !isFull();
    }

    public boolean userHasVolunteered(UserEntity user) {
        if (user == null) {
            return false;
        }
        return volunteers.contains(user.toDto());
    }

    public boolean userHasNotVolunteered(UserEntity user) {
        return !userHasVolunteered(user);
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalTime getEndTime() {
        return endTime;
    }
}
