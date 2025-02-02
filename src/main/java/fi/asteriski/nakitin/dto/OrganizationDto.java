/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record OrganizationDto(UUID id, String name) {}
