/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.service.EventService;
import fi.asteriski.nakitin.service.EventTaskService;
import fi.asteriski.nakitin.service.OrganizationService;
import fi.asteriski.nakitin.service.UserService;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest extends TestFixtures {

    @Mock
    private UserService userService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private EventTaskService eventTaskService;

    @Mock
    private EventService eventService;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userService, organizationService, eventTaskService, eventService);
    }

    @Test
    void editUser_whenNoOrganizationChange_shouldUpdateUserWithoutOrganization() {
        var userInfoForm = UserInfoForm.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        adminUserService.editUser(userInfoForm);

        verify(userService).updateUser(userInfoForm, null, Optional.empty());
    }

    @Test
    void editUser_whenChangingOrganization_shouldUpdateUserWithNewOrganization() {
        var currentOrgId = UUID.randomUUID();
        var newOrgId = UUID.randomUUID();
        var userInfoForm = UserInfoForm.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .currentOrganization(currentOrgId)
                .newOrganization(newOrgId)
                .build();

        var currentOrg = createDefaultOrganizationWithId(currentOrgId);
        var newOrg = createDefaultOrganizationWithId(newOrgId);
        when(organizationService.fetchOrganizationsByIds(anyList())).thenReturn(List.of(currentOrg, newOrg));

        adminUserService.editUser(userInfoForm);

        verify(organizationService).fetchOrganizationsByIds(List.of(currentOrgId, newOrgId));
        verify(userService).updateUser(userInfoForm, currentOrg, Optional.of(newOrg));
    }

    @Test
    void editUser_whenRemovingOrganization_shouldUpdateUserWithoutNewOrganization() {
        var currentOrgId = UUID.randomUUID();
        var userInfoForm = UserInfoForm.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .currentOrganization(currentOrgId)
                .newOrganization(null)
                .build();

        var currentOrg = createDefaultOrganizationWithId(currentOrgId);
        when(organizationService.fetchOrganizationsByIds(List.of(currentOrgId))).thenReturn(List.of(currentOrg));

        adminUserService.editUser(userInfoForm);

        verify(organizationService).fetchOrganizationsByIds(List.of(currentOrgId));
        verify(userService).updateUser(userInfoForm, currentOrg, Optional.empty());
    }

    @Test
    void deleteUser_withNoAssociations_shouldDeleteUserDirectly() {
        var userId = UUID.randomUUID();
        var user = createUserWithId(userId);
        when(userService.fetchUserById(userId)).thenReturn(user);

        adminUserService.deleteUser(userId);

        verify(userService).deleteUser(user);
        verify(eventTaskService, never()).fetchEventTasksById(anyList());
        verify(organizationService, never()).fetchOrganizationsByIds(anyList());
    }

    @Test
    void deleteUser_withEventTasks_shouldRemoveTaskAssociationsBeforeDelete() {
        var userId = UUID.randomUUID();
        var user = createUserWithId(userId);
        var task = createEventTaskWithId("Test Task", LocalDate.now(), 2, UUID.randomUUID());
        user.addEventTask(task);

        when(userService.fetchUserById(userId)).thenReturn(user);
        when(eventTaskService.fetchEventTasksById(List.of(task.getId()))).thenReturn(List.of(task));

        adminUserService.deleteUser(userId);

        assertThat(user.getEventTasks()).isEmpty();
        verify(eventTaskService).fetchEventTasksById(List.of(task.getId()));
        verify(userService).deleteUser(user);
    }

    @Test
    void deleteUser_withEventsAndVolunteers_shouldCleanupAllAssociations() {
        var userId = UUID.randomUUID();
        var user = createUserWithId(UUID.randomUUID());
        var event =
                createEventWithId("Test Event", LocalDate.now(), createDefaultOrganization(), user, UUID.randomUUID());
        var task = createEventTaskWithId("Test Task", LocalDate.now(), 2, UUID.randomUUID());
        var volunteer = createUserWithId(UUID.randomUUID());

        event.addTask(task);
        volunteer.addEventTask(task);
        user.addEvent(event);

        when(userService.fetchUserById(userId)).thenReturn(user);
        when(eventService.fetchEventsById(List.of(event.getId()))).thenReturn(List.of(event));
        when(eventTaskService.fetchEventTasksById(List.of(task.getId()))).thenReturn(List.of(task));
        when(userService.fetchUsersByIds(List.of(volunteer.getId()))).thenReturn(List.of(volunteer));

        adminUserService.deleteUser(userId);

        assertThat(volunteer.getEventTasks()).isEmpty();
        verify(eventService).fetchEventsById(List.of(event.getId()));
        verify(eventTaskService).fetchEventTasksById(List.of(task.getId()));
        verify(userService).fetchUsersByIds(List.of(volunteer.getId()));
        verify(userService).deleteUser(user);
    }

    @Test
    void deleteUser_withOrganizations_shouldRemoveOrganizationAssociations() {
        var userId = UUID.randomUUID();
        var user = createUserWithId(userId);
        var organization = createDefaultOrganizationWithId(UUID.randomUUID());
        organization.addUser(user);

        when(userService.fetchUserById(userId)).thenReturn(user);
        when(organizationService.fetchOrganizationsByIds(List.of(organization.getId())))
                .thenReturn(List.of(organization));

        adminUserService.deleteUser(userId);

        assertThat(organization.getUsers()).isEmpty();
        verify(organizationService).fetchOrganizationsByIds(List.of(organization.getId()));
        verify(userService).deleteUser(user);
    }
}
