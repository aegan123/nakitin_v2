/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto.admin;

import static fi.asteriski.nakitin.utils.Constants.PASSWORDS_MUST_MATCH;

import fi.asteriski.nakitin.dto.FormWithPassword;
import fi.asteriski.nakitin.dto.UserDto;
import fi.asteriski.nakitin.validation.IsNotAllTheSameCharacter;
import fi.asteriski.nakitin.validation.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@PasswordMatch(message = PASSWORDS_MUST_MATCH)
public class PasswordForm extends FormWithPassword {
    private UUID userId;

    @NotBlank(message = "Salasana on pakollinen tieto")
    @Size(min = 8, message = "Salasanan tulee olla vähintään kahdeksan merkkiä pitkä")
    @IsNotAllTheSameCharacter(message = "Salasana ei saa koostua pelkästään yhdestä ja samasta merkistä")
    private String password;

    @NotBlank(message = "Salasana on pakollinen tieto")
    @Size(min = 8, message = "Salasanan tulee olla vähintään kahdeksan merkkiä pitkä")
    @IsNotAllTheSameCharacter(message = "Salasana ei saa koostua pelkästään yhdestä ja samasta merkistä")
    private String confirmPassword;

    public UserDto toUserDto() {
        return UserDto.builder().id(userId).password(password).build();
    }
}
