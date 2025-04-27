/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

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
@PasswordMatch(message = "{validation.passwords.mustMatch}")
public class PasswordForm extends FormWithPassword {
    private UUID userId;

    @NotBlank(message = "{validation.password.notBlank}")
    @Size(min = 8, message = "{validation.password.size}")
    @IsNotAllTheSameCharacter(message = "{validation.password.notSameChar}")
    private String password;

    @NotBlank(message = "{validation.password.notBlank}")
    @Size(min = 8, message = "{validation.password.size}")
    @IsNotAllTheSameCharacter(message = "{validation.password.notSameChar}")
    private String confirmPassword;

    public UserDto toUserDto() {
        return UserDto.builder().id(userId).password(password).build();
    }
}
