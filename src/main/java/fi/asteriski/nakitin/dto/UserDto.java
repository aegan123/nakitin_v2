/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.repo.projection.UserDetailsProjection;
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

    @NotBlank(message = "{validation.user.firstname.notBlank}")
    private String firstName;

    @NotBlank(message = "{validation.user.lastname.notBlank}")
    private String lastName;

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.notBlank}")
    private String email;

    public static UserDto fromUserDetailsProjection(UserDetailsProjection userDetailsProjection) {
        return builder()
                .id(userDetailsProjection.getId())
                .username(userDetailsProjection.getUsername())
                .firstName(userDetailsProjection.getFirstName())
                .lastName(userDetailsProjection.getLastName())
                .email(userDetailsProjection.getEmail())
                .build();
    }
}
