/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
*/
package fi.asteriski.nakitin.service;

import static fi.asteriski.nakitin.entity.UserRole.ROLE_ADMIN;
import static fi.asteriski.nakitin.entity.UserRole.ROLE_ORG_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fi.asteriski.nakitin.dao.PasswordResetTokenDao;
import fi.asteriski.nakitin.dao.UserDao;
import fi.asteriski.nakitin.dao.VerificationTokenDao;
import fi.asteriski.nakitin.dto.admin.AddUserForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest extends TestFixtures {

    @Mock
    private UserDao userDao;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private VerificationTokenDao verificationTokenDao;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private PasswordResetTokenDao passwordResetTokenDao;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private EventTaskService eventTaskService;

    @Mock
    private HttpServletRequest request;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                verificationTokenDao,
                passwordEncoder,
                userDao,
                organizationService,
                passwordResetTokenDao,
                verificationTokenService,
                emailService,
                eventTaskService);

        ReflectionTestUtils.setField(userService, "maxPasswordAgeInDays", 90L);
        ReflectionTestUtils.setField(userService, "serverPort", "8080");
        ReflectionTestUtils.setField(userService, "contextPath", "");
    }

    @Test
    void createNewUser_withNewAdminUser_shouldCreateUserWithAdminRole() {
        var addUserForm = new AddUserForm();
        addUserForm.setUsername("adminuser");
        addUserForm.setPassword("password123");
        addUserForm.setEmail("admin@example.com");
        addUserForm.setFirstName("Admin");
        addUserForm.setLastName("User");
        addUserForm.setIsAdmin(true);
        addUserForm.setOrganization("");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(verificationTokenService.generateVerificationToken()).thenReturn("token123");
        when(verificationTokenService.calculateExpiryDate())
                .thenReturn(LocalDateTime.now().plusDays(1));
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");

        var userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        userService.createNewUser(addUserForm, request);

        verify(userDao).saveNewUser(userCaptor.capture());
        var savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("adminuser");
        assertThat(savedUser.getUserRole()).isEqualTo(ROLE_ADMIN);
        verify(emailService).sendEmailVerificationForAdmin(eq("admin@example.com"), eq("adminuser"), anyString());
    }

    @Test
    void createNewUser_withOrganization_shouldCreateOrgAdminUser() {
        var addUserForm = new AddUserForm();
        addUserForm.setUsername("orgadmin");
        addUserForm.setFirstName("Org");
        addUserForm.setLastName("Admin");
        addUserForm.setPassword("password123");
        addUserForm.setEmail("orgadmin@example.com");
        addUserForm.setOrganization("org123");

        var organization = createDefaultOrganization();
        when(organizationService.fetchOrganizationByIdForAdmin("org123")).thenReturn(organization);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(verificationTokenService.generateVerificationToken()).thenReturn("token123");
        when(verificationTokenService.calculateExpiryDate())
                .thenReturn(LocalDateTime.now().plusDays(1));
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");

        var userCaptor = ArgumentCaptor.forClass(UserEntity.class);

        userService.createNewUser(addUserForm, request);

        verify(userDao).saveNewUser(userCaptor.capture());
        var savedUser = userCaptor.getValue();
        assertThat(savedUser.getUserRole()).isEqualTo(ROLE_ORG_ADMIN);
        assertThat(savedUser.getOrganizations()).contains(organization);
        verify(organizationService).save(organization);
    }

    @Test
    void deleteUser_withNoAssociations_shouldDeleteUser() {
        var user = createUserWithId(UUID.randomUUID());
        when(userDao.fetchUsersByIds(List.of(user.getId()))).thenReturn(List.of(user));

        userService.deleteUser(user.getId());

        verify(userDao).deleteUser(user);
        verify(eventTaskService, never()).fetchEventTasksById(any());
    }

    @Test
    void deleteUser_withEventTasks_shouldRemoveTaskAssociations() {
        var user = createUserWithId(UUID.randomUUID());
        var task1 = createEventTaskWithId("Task 1", LocalDate.now(), 2, UUID.randomUUID());
        var task2 = createEventTaskWithId("Task 2", LocalDate.now(), 3, UUID.randomUUID());
        user.setEventTasks(new LinkedHashSet<>(List.of(task1, task2)));

        when(userDao.fetchUsersByIds(List.of(user.getId()))).thenReturn(List.of(user));
        when(eventTaskService.fetchEventTasksById(List.of(task1.getId(), task2.getId())))
                .thenReturn(List.of(task1, task2));

        userService.deleteUser(user.getId());

        verify(userDao).deleteUser(user);
        assertThat(user.getEventTasks()).isEmpty();
        verify(eventTaskService).fetchEventTasksById(anyList());
    }

    @Test
    void deleteUser_whenUserNotFound_shouldHandleGracefully() {
        var userId = UUID.randomUUID();
        when(userDao.fetchUsersByIds(List.of(userId))).thenReturn(List.of());

        assertThatThrownBy(() -> userService.deleteUser(userId)).isInstanceOf(NoSuchElementException.class);
        verify(userDao, never()).deleteUser(any());
    }

    @Test
    void deleteUser_withComplexAssociations_shouldCleanupAllAssociations() {
        var user = createUserWithId(UUID.randomUUID());
        var task1 = createEventTaskWithId("Task 1", LocalDate.now(), 2, UUID.randomUUID());
        var task2 = createEventTaskWithId("Task 2", LocalDate.now(), 3, UUID.randomUUID());
        user.setEventTasks(new LinkedHashSet<>(List.of(task1, task2)));

        when(userDao.fetchUsersByIds(List.of(user.getId()))).thenReturn(List.of(user));
        when(eventTaskService.fetchEventTasksById(List.of(task1.getId(), task2.getId())))
                .thenReturn(List.of(task1, task2));

        userService.deleteUser(user.getId());

        verify(userDao).deleteUser(user);
        assertThat(user.getEventTasks()).isEmpty();
    }
}
