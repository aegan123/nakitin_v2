/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.controller;

import fi.asteriski.nakitin.exceptions.CsvExportException;
import fi.asteriski.nakitin.exceptions.EventNotFoundException;
import fi.asteriski.nakitin.service.ErrorService;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
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
        //        var errorId = errorService.logError(ex);
        model.addAttribute(
                "errorTitle", messageSource.getMessage("error.event.not-found.title", null, Locale.getDefault()));
        model.addAttribute("errorText", ex.getMessage());
        model.addAttribute("contactAdmin", false);
        //        model.addAttribute(
        //            "contactAdminText",
        //            String.format(messageSource.getMessage("error.contact.admin", null, Locale.getDefault()),
        // errorId));

        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String methodArgumentTypeMismatchHandler(MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute(
                "errorTitle", messageSource.getMessage("error.page.not-found.title", null, Locale.getDefault()));
        model.addAttribute("errorText", "");
        model.addAttribute("contactAdmin", false);

        return "error";
    }

    @ExceptionHandler(CsvExportException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    String csvExportHandler(CsvExportException ex) {
        return ex.getMessage();
    }
}
