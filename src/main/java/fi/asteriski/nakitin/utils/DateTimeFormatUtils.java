/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import lombok.experimental.UtilityClass;
import org.springframework.context.i18n.LocaleContextHolder;

@UtilityClass
public final class DateTimeFormatUtils {

    public static String formatLocalDate(LocalDate date) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(LocaleContextHolder.getLocale())
                .format(date);
    }

    public static String formatLocalTime(LocalTime time) {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(LocaleContextHolder.getLocale())
                .format(time);
    }
}
