/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import static fi.asteriski.nakitin.entity.UserRole.*;

import fi.asteriski.nakitin.dao.PasswordResetTokenDao;
import fi.asteriski.nakitin.dao.UserDao;
import fi.asteriski.nakitin.dto.IdFirstLastNameDto;
import fi.asteriski.nakitin.dto.SignupForm;
import fi.asteriski.nakitin.dto.UserDto;
import fi.asteriski.nakitin.dto.admin.AddUserForm;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.OrganizationEntity;
import fi.asteriski.nakitin.entity.PasswordResetToken;
import fi.asteriski.nakitin.entity.UserEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    @NonNull
    private final BCryptPasswordEncoder passwordEncoder;

    @NonNull
    private final UserDao userDao;

    @NonNull
    private final OrganizationService organizationService;

    @NonNull
    private final PasswordResetTokenDao passwordResetTokenDao;

    @Value("${fi.asteriski.config.maxPasswordAgeInDays}")
    private Long maxPasswordAgeInDays;

    @Value("${fi.asteriski.config.passwordResetTokenExpirationHours:24}")
    private Integer passwordResetTokenExpirationHours;

    /**
     * Fetches a user from database on login. Called automatically by Spring.
     *
     * @param username the username identifying the user whose data is required.
     * @return The requested user.
     * @throws UsernameNotFoundException If user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDao.findByUsername(username);
    }

    public UserEntity fetchUserById(UUID id) {
        return userDao.findById(id);
    }

    @Transactional
    public void createNewUser(SignupForm signupForm) {
        userDao.saveNewUser(UserDto.builder()
                .username(signupForm.getUsername())
                .password(passwordEncoder.encode(signupForm.getPassword()))
                .email(signupForm.getEmail())
                .firstName(signupForm.getFirstName())
                .lastName(signupForm.getLastName())
                .build());
    }

    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    public boolean existsByUserName(String username) {
        return userDao.existsByUserName(username);
    }

    @Transactional
    public void updateUser(
            UserInfoForm userDto,
            OrganizationEntity currentOrganization,
            Optional<OrganizationEntity> newOrganization) {
        userDao.editUser(userDto, currentOrganization, newOrganization);
    }

    @Transactional
    public void updateUser(UserDto userDto) {
        userDao.editUser(userDto);
    }

    public Page<UserEntity> fetchAllUsersForAdmin(int page) {
        return userDao.fetchAllUsersForAdmin(page);
    }

    public boolean emailNotInUseByAnotherUser(String email, UUID id) {
        return userDao.emailNotInUseByAnotherUser(email, id);
    }

    @Transactional
    public void updatePasswordForUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userDao.updatePasswordForUser(userDto);
    }

    public boolean userIsTheOnlyAdmin(UUID id) {
        return userDao.userIsTheOnlyAdmin(id);
    }

    @Transactional
    public void deleteUser(UserEntity user) {
        userDao.deleteUser(user);
    }

    @Transactional
    public void createNewUser(AddUserForm addUserForm) {
        var user = UserEntity.builder()
                .username(addUserForm.getUsername())
                .password(passwordEncoder.encode(addUserForm.getPassword()))
                .email(addUserForm.getEmail())
                .firstName(addUserForm.getFirstName())
                .lastName(addUserForm.getLastName())
                .userRole(ROLE_USER)
                .expirationDate(LocalDate.now().plusDays(maxPasswordAgeInDays))
                .build();

        if (!"".equals(addUserForm.getOrganization())) {
            var organization = organizationService.fetchOrganizationByIdForAdmin(addUserForm.getOrganization());
            organization.addUser(user);
            user.setUserRole(ROLE_ORG_ADMIN);
            organizationService.save(organization);
        }
        if (Boolean.TRUE.equals(addUserForm.getIsAdmin())) {
            user.setUserRole(ROLE_ADMIN);
        }
        userDao.saveNewUser(user);
    }

    public List<UserEntity> fetchUsersByIds(List<UUID> userIds) {
        return userDao.fetchUsersByIds(userIds);
    }

    @Transactional
    public void save(List<UserEntity> users) {
        userDao.save(users);
    }

    public Set<IdFirstLastNameDto> fetchUsers() {
        return userDao.fetchUsers();
    }

    public UserEntity fetchUserReferencesById(UUID userIds) {
        return userDao.fetchUserReferencesById(userIds);
    }

    public UserDto fetchUserDetails(UUID id) {
        return userDao.fetchUserDetails(id);
    }

    public Optional<UserEntity> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Transactional
    public void createPasswordResetTokenForUser(UserEntity user, String token) {
        var expiryDate = LocalDateTime.now().plusHours(passwordResetTokenExpirationHours);
        var passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .build();
        passwordResetTokenDao.save(passwordResetToken);
    }

    public boolean validatePasswordResetToken(String token) {
        var passwordResetToken = passwordResetTokenDao.findByToken(token);
        if (passwordResetToken == null) {
            return false;
        }

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenDao.delete(passwordResetToken);
            return false;
        }

        return true;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        var passwordResetToken = passwordResetTokenDao.findByToken(token);
        if (passwordResetToken == null || !validatePasswordResetToken(token)) {
            return false;
        }

        var user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setExpirationDate(LocalDate.now().plusDays(maxPasswordAgeInDays));

        userDao.saveNewUser(user);
        passwordResetTokenDao.delete(passwordResetToken);

        return true;
    }
}
