/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.entity.OrganizationEntity;
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
                .map(OrganizationEntity::toDto)
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
}
