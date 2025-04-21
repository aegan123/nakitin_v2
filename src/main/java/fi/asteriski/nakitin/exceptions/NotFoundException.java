/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message, null, true, false);
    }
}
