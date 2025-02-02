package fi.asteriski.nakitin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(
        @NotBlank(message = "Käyttäjätunnus ei saa olla tyhjä.") String username,
        @NotBlank(message = "Salasana ei saa olla tyhjä.") String password,
        String firstName,
        String lastName,
        @Email(message = "Virheellinen sähköpostiosoite.") @NotBlank(message = "Etunimi ei saa olla tyhjä.")
                String email) {}
