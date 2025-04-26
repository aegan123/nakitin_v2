/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.admin;

import static fi.asteriski.nakitin.utils.Constants.DUMMY_PASSWORD;
import static fi.asteriski.nakitin.utils.Utils.*;

import fi.asteriski.nakitin.dto.IdAndNameDto;
import fi.asteriski.nakitin.dto.PasswordForm;
import fi.asteriski.nakitin.dto.admin.AddUserForm;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.EventEntity;
import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.EventService;
import fi.asteriski.nakitin.service.EventTaskService;
import fi.asteriski.nakitin.service.OrganizationService;
import fi.asteriski.nakitin.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {
    private final UserService userService;
    private final OrganizationService organizationService;
    private final EventTaskService eventTaskService;
    private final EventService eventService;

    public UserInfoForm fetchUserForm(final UUID id) {
        final var user = userService.fetchUserById(id);
        var orgId = user.getOrganizations().stream()
                .findFirst()
                .map(OrganizationEntity::getId)
                .orElse(null);
        return UserInfoForm.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .currentOrganization(orgId)
                .newOrganization(orgId)
                .build();
    }

    public Page<UserEntity> fetchAllUsersForAdmin(int page) {
        return userService.fetchAllUsersForAdmin(page);
    }

    public PasswordForm fetchPasswordForm(final UUID id) {
        final var user = userService.fetchUserById(id);

        return PasswordForm.builder()
                .userId(user.getId())
                .password(DUMMY_PASSWORD)
                .confirmPassword(DUMMY_PASSWORD)
                .build();
    }

    @Transactional
    @SuppressWarnings({"unchecked"})
    public void editUser(final UserInfoForm userInfoForm) {
        var organizations =
                fetchOrganizations(userInfoForm.getCurrentOrganization(), userInfoForm.getNewOrganization());
        var partitions =
                partition(organizations, o -> Objects.equals(o.getId(), userInfoForm.getCurrentOrganization()));
        var currentOrganization =
                partitions.getFirst().isEmpty() ? null : partitions.getFirst().getFirst();
        var newOrganization = partitions.get(1).isEmpty()
                ? Optional.empty()
                : Optional.of(partitions.get(1).getFirst());

        userService.updateUser(userInfoForm, currentOrganization, (Optional<OrganizationEntity>) newOrganization);
    }

    @Transactional
    public void changePasswordForUser(final PasswordForm passwordForm) {
        userService.updatePasswordForUser(passwordForm.toUserDto());
    }

    @Transactional
    public void createNewUser(final AddUserForm addUserForm, HttpServletRequest request) {
        userService.createNewUser(addUserForm, request);
    }

    public UserEntity fetchUser(final UUID id) {
        return userService.fetchUserById(id);
    }

    public boolean userIsTheOnlyAdmin(final UUID id) {
        return userService.userIsTheOnlyAdmin(id);
    }

    @Transactional
    public void deleteUser(final UUID userId) {
        final var user = fetchUser(userId);

        var taskIds = user.getEventTasks().stream().map(EventTaskEntity::getId).toList();
        if (!taskIds.isEmpty()) {
            var tasks = eventTaskService.fetchEventTasksById(taskIds);
            tasks.forEach(user::removeEventTask);
        }

        eventService
                .fetchEventsById(
                        user.getEvents().stream().map(EventEntity::getId).toList())
                .stream()
                .map(event ->
                        event.getTasks().stream().map(EventTaskEntity::getId).toList())
                .map(eventTaskService::fetchEventTasksById)
                .flatMap(List::stream)
                .forEach(task -> userService
                        .fetchUsersByIds(task.getVolunteers().stream()
                                .map(UserEntity::getId)
                                .toList())
                        .forEach(user1 -> user1.removeEventTask(task)));

        if (!user.getOrganizations().isEmpty()) {
            var organizationIds = user.getOrganizations().stream()
                    .map(OrganizationEntity::getId)
                    .toList();
            if (!organizationIds.isEmpty()) {
                var organizations = organizationService.fetchOrganizationsByIds(organizationIds);
                organizations.forEach(organization -> organization.removeUser(user));
            }
        }

        userService.deleteUser(user);
    }

    public List<IdAndNameDto> fetchAllOrganizations() {
        return organizationService.fetchAllOrganization();
    }

    private List<OrganizationEntity> fetchOrganizations(UUID... organizations) {
        return organizationService.fetchOrganizationsByIds(
                Arrays.stream(organizations).filter(Objects::nonNull).toList());
    }

    @Transactional
    public void enableUser(UUID id) {
        userService.enableUser(id);
    }

    @Transactional
    public void disableUser(UUID id) {
        userService.disableUser(id);
    }
}
