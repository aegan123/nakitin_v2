/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.validation;

import fi.asteriski.nakitin.dto.FormWithPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, FormWithPassword> {

    @Override
    public boolean isValid(FormWithPassword value, ConstraintValidatorContext context) {
        if (value.getPassword() == null || value.getConfirmPassword() == null) {
            return false;
        }
        return value.getPassword().equals(value.getConfirmPassword());
    }
}
