/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto.admin;

import fi.asteriski.nakitin.dto.IdFirstLastNameDto;
import fi.asteriski.nakitin.validation.OrganizationDoesNotExist;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@OrganizationDoesNotExist(message = "{validation.organization.existByName}")
public class AddEditOrganizationForm {
    private UUID organizationId;

    @NotBlank(message = "{validation.name.notBlank}")
    private String name;

    private UUID newAdmin;
    private Set<IdFirstLastNameDto> users;
    private UUID currentAdmin;
}
