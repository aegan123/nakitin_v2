/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.exceptions;

public class CsvExportException extends RuntimeException {
    public CsvExportException(String message, Throwable cause) {
        super(message, cause, true, false);
    }
}
