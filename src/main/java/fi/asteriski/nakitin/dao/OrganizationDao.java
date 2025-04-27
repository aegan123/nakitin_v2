/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.IdAndNameDto;
import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.exceptions.OrganizationNotFoundException;
import fi.asteriski.nakitin.repo.OrganizationRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrganizationDao {
    private final OrganizationRepository organizationRepository;

    public List<IdAndNameDto> fetchAll() {
        return organizationRepository.readAll().stream()
                .map(o -> new IdAndNameDto(o.getId(), o.getName()))
                .toList();
    }

    public Page<OrganizationEntity> fetchAllForAdmin(int page) {
        return organizationRepository.findAll(PageRequest.of(page, MAX_PAGE_SIZE, SORT_BY_NAME_ASC));
    }

    public OrganizationEntity fetchOrganizationByName(String organizer) {
        return organizationRepository
                .findByName(organizer)
                .orElseThrow(
                        () -> new OrganizationNotFoundException("Organization %s not found.".formatted(organizer)));
    }

    public void deleteOrganizations(List<OrganizationEntity> organizationsToDelete) {
        organizationRepository.deleteAll(organizationsToDelete);
    }

    public OrganizationEntity fetchOrganizationById(UUID id) {
        return organizationRepository
                .findByIdWithUsers(id)
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

    public List<IdAndNameDto> fetchUsersOrganizations(UUID userId) {
        return organizationRepository.fetchUsersOrganizations(userId).stream()
                .map(p -> new IdAndNameDto(p.getId(), p.getName()))
                .toList();
    }
}
