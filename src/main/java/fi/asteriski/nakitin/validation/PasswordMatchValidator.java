/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.validation;

import fi.asteriski.nakitin.dto.SignupForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, SignupForm> {

    @Override
    public boolean isValid(SignupForm value, ConstraintValidatorContext context) {
        if (value.getPassword() == null || value.getConfirmPassword() == null) {
            return false;
        }
        return value.getPassword().equals(value.getConfirmPassword());
    }
}
