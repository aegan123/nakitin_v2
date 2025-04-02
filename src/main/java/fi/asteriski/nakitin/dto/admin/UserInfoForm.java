/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto.admin;

import static fi.asteriski.nakitin.utils.Constants.EMAIL_IN_USE_BY_ANOTHER_USER;

import fi.asteriski.nakitin.validation.EmailNotInUseByAnotherUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@EmailNotInUseByAnotherUser(message = EMAIL_IN_USE_BY_ANOTHER_USER)
public class UserInfoForm {
    private UUID id;

    @NotBlank(message = "Etunimi on pakollinen tieto")
    private String firstName;

    @NotBlank(message = "Sukunimi on pakollinen tieto")
    private String lastName;

    @Email(message = "Virheellinen sähköpostiosoite")
    @NotBlank(message = "Sähköpostiosoite on pakollinen tieto")
    private String email;

    private UUID newOrganization;
    private UUID currentOrganization;
}
