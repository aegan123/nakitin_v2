/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.exceptions.CsvExportException;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.service.ErrorService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@AllArgsConstructor
class NakitinControllerAdvice {

    private final ErrorService errorService;
    private final MessageSource messageSource;

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String eventNotFoundHandler(EventNotFoundException ex, Model model) {
        errorService.logError(ex);
        model.addAttribute(
                MODEL_LABEL_ERROR_TITLE,
                messageSource.getMessage("error.event.not-found.title", null, LocaleContextHolder.getLocale()));
        model.addAttribute(MODEL_LABEL_CONTACT_ADMIN, false);

        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String methodArgumentTypeMismatchHandler(MethodArgumentTypeMismatchException ex, Model model) {
        errorService.logError(ex);
        model.addAttribute(
                MODEL_LABEL_ERROR_TITLE,
                messageSource.getMessage("error.page.not-found.title", null, LocaleContextHolder.getLocale()));
        model.addAttribute(MODEL_LABEL_ERROR_TEXT, "");
        model.addAttribute(MODEL_LABEL_CONTACT_ADMIN, false);

        return "error";
    }

    @ExceptionHandler(CsvExportException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    String csvExportHandler(CsvExportException ex) {
        errorService.logError(ex);
        return ex.getMessage();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String genericErrorHandler(RuntimeException ex, Model model) {
        var id = errorService.logError(ex);
        model.addAttribute(
                MODEL_LABEL_ERROR_TITLE,
                messageSource.getMessage("error.generic.title", null, LocaleContextHolder.getLocale()));
        model.addAttribute(
                MODEL_LABEL_ERROR_TEXT,
                messageSource.getMessage("error.generic.text", null, LocaleContextHolder.getLocale()));
        model.addAttribute(MODEL_LABEL_CONTACT_ADMIN, true);
        model.addAttribute(
                "contactAdminText",
                String.format(
                        messageSource.getMessage("error.contact.admin", null, LocaleContextHolder.getLocale()), id));

        return "error";
    }
}
