/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
*/
package fi.asteriski.nakitin.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fi.asteriski.nakitin.dto.IdAndNameDto;
import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.exceptions.OrganizationNotFoundException;
import fi.asteriski.nakitin.repo.OrganizationRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.test.factory.TestDataFactory;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationDaoTest extends TestFixtures {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    private OrganizationDao organizationDao;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        organizationDao = new OrganizationDao(organizationRepository);
        testDataFactory = TestDataFactory.builder()
                .organizationRepository(organizationRepository)
                .userRepository(userRepository)
                .build();
    }

    @AfterEach
    void tearDown() {
        testDataFactory.cleanUp();
    }

    @Test
    void fetchAll_organizationsExists_shouldReturnAllOrganizations() {
        var org1 = createOrganization("Org1");
        var org2 = createOrganization("Org2");
        testDataFactory.persistOrganizations(org1, org2);

        List<IdAndNameDto> result = organizationDao.fetchAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(IdAndNameDto::name).containsExactlyInAnyOrder("Org1", "Org2");
    }

    @Test
    void fetchAll_noOrganizations_shouldReturnEmptyList() {
        List<IdAndNameDto> result = organizationDao.fetchAll();

        assertThat(result).isEmpty();
    }

    @Test
    void fetchAllForAdmin_shouldReturnPagedResults() {
        var organizations = createMultipleOrganizations(25);
        testDataFactory.persistOrganizations(organizations);

        var firstPage = organizationDao.fetchAllForAdmin(0);
        var secondPage = organizationDao.fetchAllForAdmin(1);

        assertThat(firstPage.getContent()).hasSize(20);
        assertThat(secondPage.getContent()).hasSize(5);
    }

    @Test
    void fetchOrganizationByName_whenExists_shouldReturnOrganization() {
        var organization = createOrganization("TestOrg");
        testDataFactory.persistOrganization(organization);

        var result = organizationDao.fetchOrganizationByName("TestOrg");

        assertThat(result.getName()).isEqualTo("TestOrg");
    }

    @Test
    void fetchOrganizationByName_whenNotExists_shouldThrowException() {
        assertThatThrownBy(() -> organizationDao.fetchOrganizationByName("NonExistent"))
                .isInstanceOf(OrganizationNotFoundException.class)
                .hasMessageContaining("NonExistent");
    }

    @Test
    void deleteOrganizations_shouldRemoveSpecifiedOrganizations() {
        var org1 = createOrganization("Org1");
        var org2 = createOrganization("Org2");
        testDataFactory.persistOrganizations(org1, org2);
        var countBefore = organizationRepository.count();

        organizationDao.deleteOrganizations(List.of(org2));

        var countAfter = organizationRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - 1);
    }

    @Test
    void fetchOrganizationById_whenExists_shouldReturnOrganization() {
        var organization = createOrganization("TestOrg");
        testDataFactory.persistOrganization(organization);

        var result = organizationDao.fetchOrganizationById(organization.getId());

        assertThat(result.getId()).isEqualTo(organization.getId());
        assertThat(result.getName()).isEqualTo(organization.getName());
    }

    @Test
    void fetchOrganizationById_whenNotExists_shouldThrowException() {
        var randomId = UUID.randomUUID();

        assertThatThrownBy(() -> organizationDao.fetchOrganizationById(randomId))
                .isInstanceOf(OrganizationNotFoundException.class);
    }

    @Test
    void save_shouldPersistOrganization() {
        var organization = createOrganization("TestOrg");

        organizationDao.save(organization);

        assertThat(organizationRepository.findById(organization.getId()))
                .isPresent()
                .get()
                .extracting(OrganizationEntity::getName)
                .isEqualTo("TestOrg");
    }

    @Test
    void fetchOrganizationsByIds_shouldReturnMatchingOrganizations() {
        var org1 = createOrganization("Org1");
        var org2 = createOrganization("Org2");
        testDataFactory.persistOrganizations(org1, org2);

        var result = organizationDao.fetchOrganizationsByIds(List.of(org1.getId(), org2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrganizationEntity::getName).containsExactlyInAnyOrder("Org1", "Org2");
    }

    @Test
    void createNewOrganization_shouldCreateOrganizationWithUsers() {
        var user = testDataFactory.persistUser(createDefaultUser());
        var users = new HashSet<>(List.of(user));
        var dto = new OrganizationDto(null, "NewOrg", users, null);

        organizationDao.createNewOrganization(dto);

        var savedOrg = organizationRepository.findByName("NewOrg").orElseThrow();
        assertThat(savedOrg.getName()).isEqualTo("NewOrg");
        assertThat(savedOrg.getUsers()).contains(user);
    }

    @Test
    void editOrganization_shouldUpdateNameAndUsers() {
        var organization = createOrganization("OldName");
        var oldAdmin = createDefaultUser();
        var newAdmin = createDefaultUser();
        organization.addUser(oldAdmin);
        testDataFactory.persistOrganization(organization);
        testDataFactory.persistUsers(oldAdmin, newAdmin);

        var dto = new OrganizationDto(organization.getId(), "NewName", Set.of(newAdmin), null);

        organizationDao.editOrganization(dto, oldAdmin);

        var updatedOrg = organizationRepository.findById(organization.getId()).orElseThrow();
        assertThat(updatedOrg.getName()).isEqualTo("NewName");
        assertThat(updatedOrg.getUsers()).contains(newAdmin);
        assertThat(updatedOrg.getUsers()).doesNotContain(oldAdmin);
    }

    @Test
    void fetchOrganizationsByName_shouldReturnMatchingOrganizations() {
        var org1 = createOrganization("TestOrg");
        var org2 = createOrganization("TestOrg2");
        testDataFactory.persistOrganizations(org1, org2);

        var result = organizationDao.fetchOrganizationsByName("TestOrg");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("TestOrg");
    }

    @Test
    void fetchUsersOrganizations_shouldReturnOrganizationsForUser() {
        var user = createDefaultUser();
        var org1 = createOrganization("Org1");
        var org2 = createOrganization("Org2");
        org1.addUser(user);
        org2.addUser(user);
        testDataFactory.persistUser(user);
        testDataFactory.persistOrganizations(org1, org2);

        var result = organizationDao.fetchUsersOrganizations(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(IdAndNameDto::name).containsExactlyInAnyOrder("Org1", "Org2");
    }
}
