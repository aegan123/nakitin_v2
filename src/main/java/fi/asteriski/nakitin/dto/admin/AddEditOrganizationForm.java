/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto.admin;

import static fi.asteriski.nakitin.utils.Constants.ORGANIZATION_ALREADY_EXISTS_BY_THIS_NAME;

import fi.asteriski.nakitin.dto.IdFirstLastNameDto;
import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.validation.OrganizationDoesNotExist;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@OrganizationDoesNotExist(message = ORGANIZATION_ALREADY_EXISTS_BY_THIS_NAME)
public class AddEditOrganizationForm {
    private UUID organizationId;

    @NotBlank(message = "Nimi on pakollinen tieto")
    private String name;

    private UUID newAdmin;
    private Set<IdFirstLastNameDto> users;
    private UUID currentAdmin;

    public OrganizationDto toDto() {
        return OrganizationDto.builder().id(organizationId).name(name).build();
    }
}
