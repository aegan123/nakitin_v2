/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.entity.UserEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import org.springframework.context.i18n.LocaleContextHolder;

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

    public boolean userHasVolunteered(UserEntity user) {
        if (user == null) {
            return false;
        }
        return volunteers.contains(user.toDto());
    }

    public boolean userHasNotVolunteered(UserEntity user) {
        return !userHasVolunteered(user);
    }

    public String localeFormattedDate() {
        var formatter =
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LocaleContextHolder.getLocale());

        return date.format(formatter);
    }

    public String localeFormattedStartTime() {
        var formatter =
                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(LocaleContextHolder.getLocale());

        return startTime.format(formatter);
    }

    public String localeFormattedEndTime() {
        var formatter =
                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(LocaleContextHolder.getLocale());

        return endTime.format(formatter);
    }
}
