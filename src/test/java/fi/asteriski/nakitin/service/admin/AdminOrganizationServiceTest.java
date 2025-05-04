/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.admin;

import static fi.asteriski.nakitin.entity.UserRole.ROLE_ORG_ADMIN;
import static fi.asteriski.nakitin.entity.UserRole.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.dto.admin.AddEditOrganizationForm;
import fi.asteriski.nakitin.service.EventService;
import fi.asteriski.nakitin.service.EventTaskService;
import fi.asteriski.nakitin.service.OrganizationService;
import fi.asteriski.nakitin.service.UserService;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class AdminOrganizationServiceTest extends TestFixtures {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private EventTaskService eventTaskService;

    private AdminOrganizationService adminOrganizationService;

    @BeforeEach
    void setUp() {
        adminOrganizationService =
                new AdminOrganizationService(organizationService, userService, eventService, eventTaskService);
    }

    @Test
    void fetchAllOrganizations_shouldReturnPageOfOrganizations() {
        int page = 0;
        var organizations = Arrays.asList(createMultipleOrganizations(5));
        var expectedPage = new PageImpl<>(organizations);
        when(organizationService.fetchAllOrganizationForAdmin(page)).thenReturn(expectedPage);

        var result = adminOrganizationService.fetchAllOrganizations(page);

        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getContent()).isEqualTo(organizations);
        verify(organizationService).fetchAllOrganizationForAdmin(page);
    }

    @Test
    void createNewOrganization_withNewAdmin_shouldCreateOrganizationAndSetAdmin() {
        var id = UUID.randomUUID();
        var form =
                AddEditOrganizationForm.builder().name("Test Org").newAdmin(id).build();

        var admin = createUserWithId(id);
        when(userService.fetchUserReferencesById(form.getNewAdmin())).thenReturn(admin);

        adminOrganizationService.createNewOrganization(form);

        verify(userService).fetchUserReferencesById(form.getNewAdmin());
        assertThat(admin.getUserRole()).isEqualTo(ROLE_ORG_ADMIN);

        var orgCaptor = ArgumentCaptor.forClass(OrganizationDto.class);
        verify(organizationService).createNewOrganization(orgCaptor.capture());
        assertThat(orgCaptor.getValue().name()).isEqualTo("Test Org");
        assertThat(orgCaptor.getValue().users()).hasSize(1);
    }

    @Test
    void editOrganization_withAdminChange_shouldUpdateOrganizationAndRoles() {
        var currentAdminId = UUID.randomUUID();
        var newAdminId = UUID.randomUUID();
        var form = AddEditOrganizationForm.builder()
                .organizationId(UUID.randomUUID())
                .name("Updated Org")
                .currentAdmin(currentAdminId)
                .newAdmin(newAdminId)
                .build();

        var newAdmin = createUserWithId(newAdminId);
        var oldAdmin = createUserWithId(currentAdminId);
        when(userService.fetchUserReferencesById(form.getNewAdmin())).thenReturn(newAdmin);
        when(userService.fetchUserReferencesById(form.getCurrentAdmin())).thenReturn(oldAdmin);

        adminOrganizationService.editOrganization(form);

        assertThat(newAdmin.getUserRole()).isEqualTo(ROLE_ORG_ADMIN);
        assertThat(oldAdmin.getUserRole()).isEqualTo(ROLE_USER);
        verify(organizationService).editOrganization(any(OrganizationDto.class), eq(oldAdmin));
        verify(userService).save(List.of(oldAdmin));
    }

    @Test
    void deleteOrganization_withComplexAssociations_shouldCleanupAllAssociations() {
        var orgId = UUID.randomUUID();
        var organization = createDefaultOrganizationWithId(orgId);
        var user = createDefaultOrgAdminUserWithId(UUID.randomUUID());
        var event = createEventWithId("Test Event", LocalDate.now(), organization, user, UUID.randomUUID());
        var task = createEventTaskWithId("Test Task", LocalDate.now(), 2, UUID.randomUUID());
        var volunteer = createUserWithId(UUID.randomUUID());

        organization.addEvent(event);
        event.addTask(task);
        volunteer.addEventTask(task);
        organization.addUser(user);

        when(organizationService.fetchOrganizationByIdForAdmin(orgId.toString()))
                .thenReturn(organization);
        when(userService.fetchUsersByIds(List.of(user.getId()))).thenReturn(List.of(user));
        when(eventService.fetchEventsById(List.of(event.getId()))).thenReturn(List.of(event));
        when(eventTaskService.fetchEventTasksById(List.of(task.getId()))).thenReturn(List.of(task));
        when(userService.fetchUsersByIds(List.of(volunteer.getId()))).thenReturn(List.of(volunteer));

        adminOrganizationService.deleteOrganization(orgId);

        verify(organizationService).deleteOrganizations(List.of(organization));
        assertThat(user.getUserRole()).isEqualTo(ROLE_USER);
        assertThat(organization.getUsers()).isEmpty();
        assertThat(organization.getEvents()).isEmpty();
        verify(userService).save(anyList());
    }
}
