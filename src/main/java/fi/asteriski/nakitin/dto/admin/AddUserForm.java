/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto.admin;

import static fi.asteriski.nakitin.utils.Constants.CANNOT_BE_ADMIN_AND_ORG_ADMIN;
import static fi.asteriski.nakitin.utils.Constants.PASSWORDS_MUST_MATCH;

import fi.asteriski.nakitin.dto.FormWithPassword;
import fi.asteriski.nakitin.validation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@PasswordMatch(message = PASSWORDS_MUST_MATCH)
@CannotBeAdminAndOrgAdmin(message = CANNOT_BE_ADMIN_AND_ORG_ADMIN)
public class AddUserForm extends FormWithPassword {
    @NotBlank(message = "Käyttäjänimi on pakollinen tieto")
    @UsernameNotInUse(message = "Käyttäjätunnus on jo varattu")
    private String username;

    @NotBlank(message = "Etunimi on pakollinen tieto")
    private String firstName;

    @NotBlank(message = "Sukunimi on pakollinen tieto")
    private String lastName;

    @Email(message = "Virheellinen sähköpostiosoite")
    @NotBlank(message = "Sähköpostiosoite on pakollinen tieto")
    @EmailNotInUse(message = "Sähköpostiosoitteella on jo rekisteröity")
    private String email;

    @NotBlank(message = "Salasana on pakollinen tieto")
    @Size(min = 8, message = "Salasanan tulee olla vähintään kahdeksan merkkiä pitkä")
    @IsNotAllTheSameCharacter(message = "Salasana ei saa koostua pelkästään yhdestä ja samasta merkistä")
    private String password;

    @NotBlank(message = "Salasana on pakollinen tieto")
    @Size(min = 8, message = "Salasanan tulee olla vähintään kahdeksan merkkiä pitkä")
    @IsNotAllTheSameCharacter(message = "Salasana ei saa koostua pelkästään yhdestä ja samasta merkistä")
    private String confirmPassword;

    private Boolean isAdmin;
    private String organization;
}
