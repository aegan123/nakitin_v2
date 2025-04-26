/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.utils;

import static fi.asteriski.nakitin.utils.DateTimeFormatUtils.formatLocalDate;

import java.time.LocalDate;

public interface LocaleDateFormattable {
    LocalDate getDate();

    default String localeFormattedDate() {
        return formatLocalDate(getDate());
    }
}
