/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.entity.EventTaskEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StringFormatter {
    private final MessageSource messageSource;

    public String formatDeleteInfo(EventTaskEntity task) {
        return messageSource.getMessage(
                "event.task.delete.info",
                new Object[] {
                    task.getTaskName(),
                    task.localeFormattedDate(),
                    task.localeFormattedStartTime(),
                    task.localeFormattedEndTime(),
                    task.getEvent().toString()
                },
                LocaleContextHolder.getLocale());
    }
}
