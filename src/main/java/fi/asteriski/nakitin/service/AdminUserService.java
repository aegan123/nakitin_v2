/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import static fi.asteriski.nakitin.utils.Constants.DUMMY_PASSWORD;

import fi.asteriski.nakitin.dto.OrganizationDto;
import fi.asteriski.nakitin.dto.admin.AddUserForm;
import fi.asteriski.nakitin.dto.admin.PasswordForm;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
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

        return UserInfoForm.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public List<UserEntity> fetchAllUsers() {
        return userService.fetchAllUsers();
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
    public void editUser(final UserInfoForm userInfoForm) {
        userService.updateUser(userInfoForm.toUserDto());
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

    public List<OrganizationDto> fetchAllOrganizations() {
        return organizationService.fetchAllOrganization();
    }
}
