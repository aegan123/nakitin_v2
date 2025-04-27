/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto.admin;

import fi.asteriski.nakitin.dto.FormWithPassword;
import fi.asteriski.nakitin.validation.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@PasswordMatch(message = "{validation.passwords.mustMatch}")
@CannotBeAdminAndOrgAdmin(message = "{validation.user.notOrgAdminAndAdmin}")
public class AddUserForm extends FormWithPassword {
    @NotBlank(message = "{validation.user.username.notBlank}")
    @UsernameNotInUse(message = "{validation.user.username.inuse}")
    private String username;

    @NotBlank(message = "{validation.user.firstname.notBlank}")
    private String firstName;

    @NotBlank(message = "{validation.user.lastname.notBlank}")
    private String lastName;

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.notBlank}")
    @EmailNotInUse(message = "{validation.user.email.inUse}")
    private String email;

    @NotBlank(message = "{validation.password.notBlank}")
    @Size(min = 8, message = "{validation.password.size}")
    @IsNotAllTheSameCharacter(message = "{validation.password.notSameChar}")
    private String password;

    @NotBlank(message = "{validation.password.notBlank}")
    @Size(min = 8, message = "{validation.password.size}")
    @IsNotAllTheSameCharacter(message = "{validation.password.notSameChar}")
    private String confirmPassword;

    private Boolean isAdmin;
    private String organization;
}
