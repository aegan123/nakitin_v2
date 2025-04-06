/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.validation;

import fi.asteriski.nakitin.dto.admin.AddUserForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CannotBeAdminAndOrgAdminValidator implements ConstraintValidator<CannotBeAdminAndOrgAdmin, AddUserForm> {
    @Override
    public boolean isValid(AddUserForm value, ConstraintValidatorContext context) {
        return !(Boolean.TRUE.equals(value.getIsAdmin()) && !"".equals(value.getOrganization()));
    }
}
