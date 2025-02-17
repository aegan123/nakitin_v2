/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserDto(
        UUID id,
        @NotBlank(message = "Käyttäjätunnus ei saa olla tyhjä.") String username,
        @NotBlank(message = "Salasana ei saa olla tyhjä.") String password,
        String firstName,
        String lastName,
        @Email(message = "Virheellinen sähköpostiosoite.") @NotBlank(message = "Etunimi ei saa olla tyhjä.")
                String email) {
}
