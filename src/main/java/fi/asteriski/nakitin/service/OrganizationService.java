/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.OrganizationDao;
import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OrganizationService {
    private final OrganizationDao organizationDao;

    public List<OrganizationDto> fetchAllOrganization() {
        return organizationDao.fetchAll();
    }

    public void deleteOrganizations(List<OrganizationEntity> organizationsToDelete) {
        organizationDao.deleteOrganizations(organizationsToDelete);
    }

    public OrganizationEntity fetchOrganizationById(String organization) {
        return organizationDao.fetchOrganizationById(UUID.fromString(organization));
    }

    public void save(OrganizationEntity organization) {
        organizationDao.save(organization);
    }

    public List<OrganizationEntity> fetchOrganizationsByIds(List<UUID> organizationIds) {
        return organizationDao.fetchOrganizationsByIds(organizationIds);
    }
}
