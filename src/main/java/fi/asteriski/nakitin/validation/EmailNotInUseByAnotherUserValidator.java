/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.validation;

import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmailNotInUseByAnotherUserValidator
        implements ConstraintValidator<EmailNotInUseByAnotherUser, UserInfoForm> {

    private final UserService userService;

    @Override
    public boolean isValid(UserInfoForm value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return userService.emailNotInUseByAnotherUser(value.getEmail(), value.getId());
    }
}
