/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.exceptions.OrganizationNotFoundException;
import fi.asteriski.nakitin.repo.OrganizationRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrganizationDao {
    private final OrganizationRepository organizationRepository;

    public List<OrganizationDto> fetchAll() {
        return organizationRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(OrganizationEntity::toAdminDto)
                .toList();
    }

    public OrganizationDto fetchOrganizationByName(String organizer) {
        return organizationRepository
                .findByName(organizer)
                .map(OrganizationEntity::toDto)
                .orElseThrow(() ->
                        new OrganizationNotFoundException(String.format("Organization %s not found.", organizer)));
    }

    public void deleteOrganizations(List<OrganizationEntity> organizationsToDelete) {
        organizationRepository.deleteAll(organizationsToDelete);
    }

    public OrganizationEntity fetchOrganizationById(UUID id) {
        return organizationRepository
                .findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
    }

    public void save(OrganizationEntity organization) {
        organizationRepository.save(organization);
    }

    public List<OrganizationEntity> fetchOrganizationsByIds(List<UUID> organizationIds) {
        return organizationRepository.findAllById(organizationIds);
    }

    public void createNewOrganization(OrganizationDto dto) {
        var organization = dto.toEntity();
        dto.users().forEach(organization::addUser);
        save(organization);
    }

    public void editOrganization(OrganizationDto dto, UserEntity oldAdmin) {
        var organization = fetchOrganizationById(dto.id());
        organization.setName(dto.name());
        if (!dto.users().isEmpty()) {
            organization.removeUser(oldAdmin);
            dto.users().forEach(organization::addUser);
        }
        save(organization);
    }

    public List<OrganizationDto> fetchOrganizationsByName(String name) {
        return organizationRepository.readAllByName(name).stream()
                .map(org -> OrganizationDto.builder()
                        .id(org.getId())
                        .name(org.getName())
                        .build())
                .toList();
    }
}
