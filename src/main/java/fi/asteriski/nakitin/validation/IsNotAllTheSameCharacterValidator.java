/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsNotAllTheSameCharacterValidator implements ConstraintValidator<IsNotAllTheSameCharacter, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.codePoints().distinct().count() > 1;
    }
}
