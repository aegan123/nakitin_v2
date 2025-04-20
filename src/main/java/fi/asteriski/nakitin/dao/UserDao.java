/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static fi.asteriski.nakitin.entity.UserRole.*;
import static fi.asteriski.nakitin.utils.Constants.*;

import fi.asteriski.nakitin.dto.IdFirstLastNameDto;
import fi.asteriski.nakitin.dto.UserDto;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.repo.projection.EmailProjection;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserDao {
    private final UserRepository userRepository;

    public void saveNewUser(UserEntity newUserEntity) {
        userRepository.save(newUserEntity);
    }

    public UserEntity findById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User '%s' not found.", id)));
    }

    public UserEntity findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User '%s' not found.", username)));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUserName(String username) {
        return userRepository.existsByUsername(username);
    }

    public void editUser(
            UserInfoForm userInfoForm,
            OrganizationEntity currentOrganization,
            Optional<OrganizationEntity> newOrganization) {
        var user = findById(userInfoForm.getId());
        user.setFirstName(userInfoForm.getFirstName());
        user.setLastName(userInfoForm.getLastName());
        user.setEmail(userInfoForm.getEmail());
        if (!Objects.equals(userInfoForm.getCurrentOrganization(), userInfoForm.getNewOrganization())) {
            newOrganization.ifPresentOrElse(
                    newOrg -> {
                        if (currentOrganization != null) {
                            currentOrganization.removeUser(user);
                        }
                        newOrg.addUser(user);
                        user.setUserRole(ROLE_ORG_ADMIN);
                    },
                    () -> {
                        currentOrganization.removeUser(user);
                        user.setUserRole(ROLE_USER);
                    });
        }

        userRepository.save(user);
    }

    public void editUser(UserEntity user) {
        userRepository.save(user);
    }

    public Page<UserEntity> fetchAllUsersForAdmin(int page) {
        return userRepository.findAll(PageRequest.of(page, MAX_PAGE_SIZE, SORT_BY_LASTNAME_ASC));
    }

    public boolean emailNotInUseByAnotherUser(String email, UUID id) {
        return userRepository.emailInUseByAnotherUser(email, id) == null;
    }

    public void updatePasswordForUser(UserDto userDto) {
        var user = findById(userDto.getId());
        user.setPassword(userDto.getPassword());
        userRepository.save(user);
    }

    public boolean userIsTheOnlyAdmin(UUID id) {
        return userRepository.countAllByUserRoleAndIdNot(ROLE_ADMIN, id) == 0;
    }

    public void deleteUser(UserEntity user) {
        userRepository.delete(user);
    }

    public List<UserEntity> fetchUsersByIds(List<UUID> userIds) {
        return userRepository.findAllById(userIds);
    }

    public void save(UserEntity user) {
        userRepository.save(user);
    }

    public void save(List<UserEntity> users) {
        userRepository.saveAll(users);
    }

    public Set<IdFirstLastNameDto> fetchUsers() {
        return userRepository.fetchAllUsers().stream()
                .map(user -> new IdFirstLastNameDto(user.getId(), user.getFirstName(), user.getLastName()))
                .collect(Collectors.toSet());
    }

    public UserEntity fetchUserReferencesById(UUID userId) {
        return userRepository.getReferenceById(userId);
    }

    public UserDto fetchUserDetails(UUID id) {
        return userRepository
                .findUserEntityById(id)
                .map(UserDto::fromUserDetailsProjection)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User '%s' not found.", id)));
    }

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUsersById(List<UUID> toDelete) {
        userRepository.deleteUsersById(toDelete);
    }

    public Optional<UserEntity> findByVerificationToken(String token) {
        return userRepository.findByVerificationToken_Token(token);
    }

    public List<String> fetchUsersWithExpiringPassword(LocalDate localDate) {
        return userRepository.readAllByExpirationDateIs(localDate).stream()
                .map(EmailProjection::getEmail)
                .toList();
    }
}
