/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto.admin;

import fi.asteriski.nakitin.validation.EmailNotInUseByAnotherUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@EmailNotInUseByAnotherUser(message = "{validation.user.email.inUseByAnother}")
public class UserInfoForm {
    private UUID id;

    @NotBlank(message = "{validation.user.firstname.notBlank}")
    private String firstName;

    @NotBlank(message = "{validation.user.lastname.notBlank}")
    private String lastName;

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.notBlank=}")
    private String email;

    private UUID newOrganization;
    private UUID currentOrganization;
}
