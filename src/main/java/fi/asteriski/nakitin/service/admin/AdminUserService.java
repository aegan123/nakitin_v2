/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service.admin;

import static fi.asteriski.nakitin.utils.Constants.DUMMY_PASSWORD;
import static fi.asteriski.nakitin.utils.Utils.*;

import fi.asteriski.nakitin.dto.IdAndNameDto;
import fi.asteriski.nakitin.dto.admin.AddUserForm;
import fi.asteriski.nakitin.dto.admin.PasswordForm;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.service.EventTaskService;
import fi.asteriski.nakitin.service.OrganizationService;
import fi.asteriski.nakitin.service.UserService;
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
    public void createNewUser(final AddUserForm addUserForm) {
        userService.createNewUser(addUserForm);
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

        if (!user.getOrganizations().isEmpty()) {
            var organizationIds = user.getOrganizations().stream()
                    .map(OrganizationEntity::getId)
                    .toList();
            if (!organizationIds.isEmpty()) {
                var organizations = organizationService.fetchOrganizationsByIds(organizationIds);
                organizations.forEach(organization -> organization.removeUser(user));
                var organizationsToRemove = organizations.stream()
                        .filter(OrganizationEntity::hasNoUsers)
                        .toList();
                if (!organizationsToRemove.isEmpty()) {
                    organizationService.deleteOrganizations(organizationsToRemove);
                }
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
}
