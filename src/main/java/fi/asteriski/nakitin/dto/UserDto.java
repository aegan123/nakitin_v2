/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class UserDto {
    private UUID id;
    private String username;
    private String password;

    @NotBlank(message = "Etunimi on pakollinen tieto")
    private String firstName;

    @NotBlank(message = "Sukunimi on pakollinen tieto")
    private String lastName;

    @Email(message = "Virheellinen sähköpostiosoite")
    @NotBlank(message = "Sähköpostiosoite on pakollinen tieto")
    private String email;
}
