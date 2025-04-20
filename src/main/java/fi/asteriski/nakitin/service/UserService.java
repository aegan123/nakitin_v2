/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import static fi.asteriski.nakitin.dto.VerificationStatus.*;
import static fi.asteriski.nakitin.entity.UserRole.*;

import fi.asteriski.nakitin.dao.PasswordResetTokenDao;
import fi.asteriski.nakitin.dao.UserDao;
import fi.asteriski.nakitin.dao.VerificationTokenDao;
import fi.asteriski.nakitin.dto.*;
import fi.asteriski.nakitin.dto.admin.AddUserForm;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private final VerificationTokenDao verificationTokenDao;

    @NonNull
    private final BCryptPasswordEncoder passwordEncoder;

    @NonNull
    private final UserDao userDao;

    @NonNull
    private final OrganizationService organizationService;

    @NonNull
    private final PasswordResetTokenDao passwordResetTokenDao;

    @NonNull
    private final VerificationTokenService verificationTokenService;

    @NonNull
    private final EmailService emailService;

    @Value("${fi.asteriski.config.maxPasswordAgeInDays}")
    private Long maxPasswordAgeInDays;

    @Value("${fi.asteriski.config.passwordResetTokenExpirationHours}")
    private Integer passwordResetTokenExpirationHours;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${fi.asteriski.config.user.daysBeforeExpireToRemind}")
    private Long daysBeforeExpireToRemind;

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
    public void createNewUser(SignupForm signupForm, HttpServletRequest request) {
        var user = UserEntity.builder()
                .username(signupForm.getUsername())
                .password(passwordEncoder.encode(signupForm.getPassword()))
                .email(signupForm.getEmail())
                .firstName(signupForm.getFirstName())
                .lastName(signupForm.getLastName())
                .expirationDate(LocalDate.now().plusDays(maxPasswordAgeInDays))
                .userRole(UserRole.ROLE_USER)
                .build();
        setupEmailVerification(user, request);
        userDao.saveNewUser(user);
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
    public boolean updateUser(UserDto userDto, HttpServletRequest request) {
        var user = userDao.findById(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        boolean emailChanged = !user.getEmail().equals(userDto.getEmail());
        if (emailChanged) {
            setupEmailChangeVerification(user, userDto.getEmail(), request);
        }

        userDao.save(user);
        return emailChanged;
    }

    private void setupEmailChangeVerification(UserEntity user, String newEmail, HttpServletRequest request) {
        var token = verificationTokenService.generateVerificationToken();
        var verificationToken = VerificationTokenEntity.builder()
                .token(token)
                .expiryDate(verificationTokenService.calculateExpiryDate())
                .build();

        user.setPendingEmailChange(newEmail, verificationToken);
        verificationToken.addUser(user);

        var verificationUrl = "%s/verify-email?token=%s".formatted(getBaseUrl(request), token);
        emailService.sendEmailChangeVerification(newEmail, verificationUrl);
    }

    @Transactional
    public VerificationResult verifyEmail(String token) {
        return userDao.findByVerificationToken(token)
                .map(user -> {
                    if (user.getPendingEmailChange() != null) {
                        return processEmailChangeVerification(user);
                    }
                    return processNewUserVerification(user);
                })
                .orElse(createVerificationResult(false, null, NOT_FOUND));
    }

    private VerificationResult processEmailChangeVerification(UserEntity user) {
        var newEmail = user.getPendingEmailChange().getNewEmail();
        if (verificationTokenService.isTokenExpired(
                user.getPendingEmailChange().getVerificationToken().getExpiryDate())) {
            return createVerificationResult(false, newEmail, EXPIRED);
        }

        user.getPendingEmailChange().getVerificationToken().removeUser(user);

        user.setEmail(newEmail);
        user.setEmailVerified(true);
        user.clearPendingEmailChange();
        userDao.save(user);

        return createVerificationResult(true, null, VERIFIED);
    }

    private VerificationResult processNewUserVerification(UserEntity user) {
        if (verificationTokenService.isTokenExpired(user.getVerificationToken().getExpiryDate())) {
            return createVerificationResult(false, user.getEmail(), EXPIRED);
        }

        verifyUserEmail(user);
        return createVerificationResult(true, null, VERIFIED);
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

    @Transactional
    public void setupEmailVerification(UserEntity user, HttpServletRequest request) {
        var token = verificationTokenService.generateVerificationToken();
        var verificationToken = VerificationTokenEntity.builder()
                .token(token)
                .expiryDate(verificationTokenService.calculateExpiryDate())
                .build();

        verificationToken.addUser(user);
        userDao.save(user);

        var verificationUrl = "%s/verify-email?token=%s".formatted(getBaseUrl(request), token);
        emailService.sendEmailVerification(user.getEmail(), verificationUrl);
    }

    @Transactional
    public void changePassword(PasswordForm passwordForm) {
        var user = userDao.findById(passwordForm.getUserId());
        user.setPassword(passwordEncoder.encode(passwordForm.getPassword()));
        userDao.save(user);
    }

    private String getBaseUrl(HttpServletRequest request) {
        var scheme = request.getScheme();
        var serverName = request.getServerName();
        return "%s://%s:%s%s".formatted(scheme, serverName, serverPort, contextPath);
    }

    private void verifyUserEmail(UserEntity user) {
        user.setEmailVerified(true);
        user.getVerificationToken().removeUser(user);
        verificationTokenDao.deleteByUser(user);
    }

    private VerificationResult createVerificationResult(boolean verified, String email, VerificationStatus status) {
        return VerificationResult.builder()
                .verified(verified)
                .email(email)
                .status(status)
                .build();
    }

    public void sendReminderEmailAboutExpiringPasswords() {
        userDao.fetchUsersWithExpiringPassword(LocalDate.now().plusDays(daysBeforeExpireToRemind))
                .forEach(email -> emailService.sendPasswordExpirationWarning(email, daysBeforeExpireToRemind));
    }
}
