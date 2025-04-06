/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import static fi.asteriski.nakitin.utils.Constants.PASSWORDS_MUST_MATCH;

import fi.asteriski.nakitin.validation.EmailNotInUse;
import fi.asteriski.nakitin.validation.IsNotAllTheSameCharacter;
import fi.asteriski.nakitin.validation.PasswordMatch;
import fi.asteriski.nakitin.validation.UsernameNotInUse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@PasswordMatch(message = PASSWORDS_MUST_MATCH)
public class SignupForm extends FormWithPassword {
    @NotBlank(message = "Käyttäjänimi on pakollinen tieto")
    @UsernameNotInUse(message = "Käyttäjätunnus on jo varattu")
    String username;

    @NotBlank(message = "Etunimi on pakollinen tieto")
    String firstName;

    @NotBlank(message = "Sukunimi on pakollinen tieto")
    String lastName;

    @Email(message = "Virheellinen sähköpostiosoite")
    @NotBlank(message = "Sähköpostiosoite on pakollinen tieto")
    @EmailNotInUse(message = "Sähköpostiosoitteella on jo rekisteröity")
    String email;

    @NotBlank(message = "Salasana on pakollinen tieto")
    @Size(min = 8, message = "Salasanan tulee olla vähintään kahdeksan merkkiä pitkä")
    @IsNotAllTheSameCharacter(message = "Salasana ei saa koostua pelkästään yhdestä ja samasta merkistä")
    String password;

    @NotBlank(message = "Salasana on pakollinen tieto")
    @Size(min = 8, message = "Salasanan tulee olla vähintään kahdeksan merkkiä pitkä")
    @IsNotAllTheSameCharacter(message = "Salasana ei saa koostua pelkästään yhdestä ja samasta merkistä")
    String confirmPassword;
}
