/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.admin;

import static fi.asteriski.nakitin.entity.UserRole.ROLE_ORG_ADMIN;
import static fi.asteriski.nakitin.entity.UserRole.ROLE_USER;

import fi.asteriski.nakitin.dto.IdFirstLastNameDto;
import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.dto.admin.AddEditOrganizationForm;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.EventService;
import fi.asteriski.nakitin.service.EventTaskService;
import fi.asteriski.nakitin.service.OrganizationService;
import fi.asteriski.nakitin.service.UserService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AdminOrganizationService {
    private final OrganizationService organizationService;
    private final UserService userService;
    private final EventService eventService;
    private final EventTaskService eventTaskService;

    public Page<OrganizationEntity> fetchAllOrganizations(int page) {
        return organizationService.fetchAllOrganizationForAdmin(page);
    }

    @Transactional
    public void createNewOrganization(final AddEditOrganizationForm addOrganizationForm) {
        UserEntity user;
        var users = new LinkedHashSet<UserEntity>();
        if (addOrganizationForm.getNewAdmin() != null) {
            user = userService.fetchUserReferencesById(addOrganizationForm.getNewAdmin());
            user.setUserRole(ROLE_ORG_ADMIN);
            users.add(user);
        }
        organizationService.createNewOrganization(OrganizationDto.builder()
                .id(addOrganizationForm.getOrganizationId())
                .name(addOrganizationForm.getName())
                .users(users)
                .build());
    }

    @Transactional
    public void editOrganization(final AddEditOrganizationForm editOrganizationForm) {
        var users = new LinkedHashSet<UserEntity>();
        UserEntity oldAdmin = null;
        if (!editOrganizationForm.getNewAdmin().equals(editOrganizationForm.getCurrentAdmin())) {
            var user = userService.fetchUserReferencesById(editOrganizationForm.getNewAdmin());
            user.setUserRole(ROLE_ORG_ADMIN);
            users.add(user);
            oldAdmin = userService.fetchUserReferencesById(editOrganizationForm.getCurrentAdmin());
            oldAdmin.setUserRole(ROLE_USER);
        }
        var dto = OrganizationDto.builder()
                .id(editOrganizationForm.getOrganizationId())
                .name(editOrganizationForm.getName())
                .users(users)
                .build();
        organizationService.editOrganization(dto, oldAdmin);
        if (oldAdmin != null) {
            userService.save(List.of(oldAdmin));
        }
    }

    public AddEditOrganizationForm fetchOrganizationAddForm() {
        return AddEditOrganizationForm.builder().users(fetchUsers()).build();
    }

    public AddEditOrganizationForm fetchOrganizationEditForm(final UUID id) {
        var org = organizationService.fetchOrganizationByIdForAdmin(id);
        var adminId = org.users().stream().findFirst().orElseThrow().getId();
        return AddEditOrganizationForm.builder()
                .organizationId(id)
                .name(org.name())
                .users(fetchUsers())
                .currentAdmin(adminId)
                .newAdmin(adminId)
                .build();
    }

    public OrganizationDto fetchOrganization(final UUID id) {
        return organizationService.fetchOrganizationByIdForAdmin(id);
    }

    @Transactional
    public void deleteOrganization(final UUID id) {
        final var organization = organizationService.fetchOrganizationByIdForAdmin(id.toString());

        final var userIds =
                organization.getUsers().stream().map(UserEntity::getId).toList();
        final var users = userService.fetchUsersByIds(userIds);
        final var eventIds =
                organization.getEvents().stream().map(EventEntity::getId).toList();
        if (!eventIds.isEmpty()) {
            var events = eventService.fetchEventsById(eventIds);
            var eventTaskIds = events.stream()
                    .map(event -> event.getTasks().stream()
                            .map(EventTaskEntity::getId)
                            .toList())
                    .flatMap(List::stream)
                    .toList();
            if (!eventTaskIds.isEmpty()) {
                var tasks = eventTaskService.fetchEventTasksById(eventTaskIds);
                var volunteerIds = tasks.stream()
                        .map(task -> task.getVolunteers().stream()
                                .map(UserEntity::getId)
                                .toList())
                        .flatMap(List::stream)
                        .toList();
                if (!volunteerIds.isEmpty()) {
                    var volunteers = userService.fetchUsersByIds(volunteerIds);
                    volunteers.forEach(volunteer -> tasks.forEach(volunteer::removeEventTask));
                }
            }
            users.forEach(user -> events.forEach(user::removeEvent));
            events.forEach(organization::removeEvent);
        }
        users.forEach(UserEntity::removeOrganizationAdminRights);

        users.forEach(organization::removeUser);

        userService.save(users);
        organizationService.deleteOrganizations(List.of(organization));
    }

    public Set<IdFirstLastNameDto> fetchUsers() {
        return userService.fetchUsers();
    }

    public void addUsersToAddEditOrganizationForm(AddEditOrganizationForm editOrganizationForm) {
        editOrganizationForm.setUsers(fetchUsers());
    }
}
