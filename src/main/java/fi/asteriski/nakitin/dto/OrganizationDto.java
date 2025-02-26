/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dto;

import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrganizationDto(UUID id, String name, Set<UserEntity> users, List<EventDto> events) {
    public OrganizationEntity toEntity() {
        return OrganizationEntity.builder().id(id).name(name).users(users).build();
    }
}
