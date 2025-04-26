/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.utils;

import static fi.asteriski.nakitin.utils.DateTimeFormatUtils.formatLocalTime;

import java.time.LocalTime;

public interface LocaleTimeFormattable {
    LocalTime getStartTime();

    LocalTime getEndTime();

    default String localeFormattedStartTime() {
        return formatLocalTime(getStartTime());
    }

    default String localeFormattedEndTime() {
        return formatLocalTime(getEndTime());
    }
}
