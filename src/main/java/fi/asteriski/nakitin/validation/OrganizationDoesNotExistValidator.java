/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.validation;

import fi.asteriski.nakitin.dto.admin.AddEditOrganizationForm;
import fi.asteriski.nakitin.service.OrganizationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationDoesNotExistValidator
        implements ConstraintValidator<OrganizationDoesNotExist, AddEditOrganizationForm> {

    private final OrganizationService organizationService;

    @Override
    public boolean isValid(AddEditOrganizationForm value, ConstraintValidatorContext context) {
        var orgs = organizationService.fetchByName(value.getName());
        return orgs.isEmpty()
                || orgs.stream()
                        .anyMatch(org -> Objects.equals(value.getOrganizationId(), org.id())
                                && Objects.equals(value.getName(), org.name()));
    }
}
