/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static fi.asteriski.nakitin.entity.UserRole.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fi.asteriski.nakitin.dto.IdFirstLastNameDto;
import fi.asteriski.nakitin.dto.UserDto;
import fi.asteriski.nakitin.dto.admin.UserInfoForm;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.repo.OrganizationRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.repo.VerificationTokenRepository;
import fi.asteriski.nakitin.test.factory.TestDataFactory;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
class UserDaoTest extends TestFixtures {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    private UserDao userDao;
    private TestDataFactory testDataFactory;

    private final long maxPasswordAgeInDays = 90L;

    @BeforeEach
    void setUp() {
        userDao = new UserDao(userRepository);
        // Set the maxPasswordAgeInDays using reflection since it's normally injected by Spring
        ReflectionTestUtils.setField(userDao, "maxPasswordAgeInDays", maxPasswordAgeInDays);

        testDataFactory = TestDataFactory.builder()
                .userRepository(userRepository)
                .organizationRepository(organizationRepository)
                .verificationTokenRepository(verificationTokenRepository)
                .build();
    }

    @AfterEach
    void tearDown() {
        testDataFactory.cleanUp();
    }

    @Test
    void saveNewUser_shouldSaveUserSuccessfully() {
        var user = createDefaultUser();

        userDao.saveNewUser(user);

        var savedUser = userRepository.findById(user.getId());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get()).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    void findById_whenUserExists_shouldReturnUser() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        var foundUser = userDao.findById(user.getId());

        assertThat(foundUser).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    void findById_whenUserDoesNotExist_shouldThrowException() {
        var randomId = UUID.randomUUID();

        assertThatThrownBy(() -> userDao.findById(randomId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(randomId.toString());
    }

    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        var foundUser = userDao.findByUsername(user.getUsername());

        assertThat(foundUser).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    void findByUsername_whenUserDoesNotExist_shouldThrowException() {
        var nonExistentUsername = "nonexistent";

        assertThatThrownBy(() -> userDao.findByUsername(nonExistentUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(nonExistentUsername);
    }

    @Test
    void existsByEmail_whenEmailExists_shouldReturnTrue() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        boolean exists = userDao.existsByEmail(user.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_whenEmailDoesNotExist_shouldReturnFalse() {
        boolean exists = userDao.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void editUser_whenChangingBasicInfo_shouldUpdateSuccessfully() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        var userInfoForm = UserInfoForm.builder()
                .id(user.getId())
                .firstName("NewFirst")
                .lastName("NewLast")
                .email("newemail@example.com")
                .build();

        userDao.editUser(userInfoForm, null, Optional.empty());

        var updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("NewFirst");
        assertThat(updatedUser.getLastName()).isEqualTo("NewLast");
        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    void editUser_whenChangingOrganization_shouldUpdateSuccessfully() {
        var user = createDefaultUser();
        var oldOrg = createOrganization("OldOrg");
        var newOrg = createOrganization("NewOrg");
        oldOrg.addUser(user);

        testDataFactory.persistUser(user);
        testDataFactory.persistOrganizations(oldOrg, newOrg);

        var userInfoForm = UserInfoForm.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .currentOrganization(oldOrg.getId())
                .newOrganization(newOrg.getId())
                .build();

        userDao.editUser(userInfoForm, oldOrg, Optional.of(newOrg));

        var updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getOrganizations()).contains(newOrg);
        assertThat(updatedUser.getOrganizations()).doesNotContain(oldOrg);
        assertThat(updatedUser.getUserRole()).isEqualTo(ROLE_ORG_ADMIN);
    }

    @Test
    void editUser_whenAddingOrganization_shouldUpdateSuccessfully() {
        var user = createDefaultUser();
        var newOrg = createOrganization("NewOrg");

        testDataFactory.persistUser(user);
        testDataFactory.persistOrganization(newOrg);

        var userInfoForm = UserInfoForm.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .newOrganization(newOrg.getId())
                .build();

        userDao.editUser(userInfoForm, null, Optional.of(newOrg));

        var updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getOrganizations()).contains(newOrg);
        assertThat(updatedUser.getUserRole()).isEqualTo(ROLE_ORG_ADMIN);
    }

    @Test
    void editUser_whenRemovingOrganization_shouldUpdateSuccessfully() {
        var user = createDefaultUser();
        var oldOrg = createOrganization("OldOrg");

        testDataFactory.persistUser(user);
        testDataFactory.persistOrganization(oldOrg);

        var userInfoForm = UserInfoForm.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .currentOrganization(oldOrg.getId())
                .build();

        userDao.editUser(userInfoForm, oldOrg, Optional.empty());

        var updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getOrganizations()).doesNotContain(oldOrg);
        assertThat(updatedUser.getUserRole()).isEqualTo(ROLE_USER);
    }

    @Test
    void fetchAllUsersForAdmin_shouldReturnPagedResults() {
        testDataFactory.persistUsers(createMultipleDefaultUsers(25));

        var firstPage = userDao.fetchAllUsersForAdmin(0);
        var secondPage = userDao.fetchAllUsersForAdmin(1);

        assertThat(firstPage.getContent()).hasSize(20);
        assertThat(secondPage.getContent()).hasSize(5);
    }

    @Test
    void updatePasswordForUser_shouldUpdatePasswordAndExpirationDate() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);
        var userDto = UserDto.builder().id(user.getId()).password("newPassword").build();

        userDao.updatePasswordForUser(userDto);

        var updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getPassword()).isEqualTo("newPassword");
        assertThat(updatedUser.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(maxPasswordAgeInDays));
    }

    @Test
    void userIsTheOnlyAdmin_whenUserIsOnlyAdmin_shouldReturnTrue() {
        var adminUser = createDefaultAdminUser();
        testDataFactory.persistUser(adminUser);

        boolean isOnlyAdmin = userDao.userIsTheOnlyAdmin(adminUser.getId());

        assertThat(isOnlyAdmin).isTrue();
    }

    @Test
    void userIsTheOnlyAdmin_whenMultipleAdminsExist_shouldReturnFalse() {
        var adminUser1 = createDefaultAdminUser();
        var adminUser2 = createDefaultAdminUser();
        testDataFactory.persistUsers(adminUser1, adminUser2);

        boolean isOnlyAdmin = userDao.userIsTheOnlyAdmin(adminUser1.getId());

        assertThat(isOnlyAdmin).isFalse();
    }

    @Test
    void deleteUser_whenUserExists_shouldRemoveUserFromDatabase() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        userDao.deleteUser(user);

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void fetchUsersByIds_shouldReturnMatchingUsers() {
        var users = createMultipleDefaultUsers(3);
        testDataFactory.persistUsers(users);
        var ids = Arrays.stream(users).map(UserEntity::getId).toList();

        var fetchedUsers = userDao.fetchUsersByIds(ids);

        assertThat(fetchedUsers).hasSize(3);
        assertThat(fetchedUsers.stream().map(UserEntity::getId)).containsExactlyInAnyOrderElementsOf(ids);
    }

    @Test
    void fetchUsers_shouldReturnSetOfIdFirstLastNameDto() {
        var users = createMultipleDefaultUsers(3);
        testDataFactory.persistUsers(users);

        var result = userDao.fetchUsers();

        assertThat(result).hasSize(3);
        assertThat(result.stream().map(IdFirstLastNameDto::id))
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(users).map(UserEntity::getId).toList());
    }

    @Test
    void fetchUserReferencesById_shouldReturnUserReference() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        var reference = userDao.fetchUserReferencesById(user.getId());

        assertThat(reference.getId()).isEqualTo(user.getId());
    }

    @Test
    void fetchUserDetails_whenUserExists_shouldReturnUserDto() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        var userDto = userDao.fetchUserDetails(user.getId());

        assertThat(userDto.getId()).isEqualTo(user.getId());
        assertThat(userDto.getUsername()).isEqualTo(user.getUsername());
        assertThat(userDto.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userDto.getLastName()).isEqualTo(user.getLastName());
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void fetchUserDetails_whenUserDoesNotExist_shouldThrowException() {
        var randomId = UUID.randomUUID();

        assertThatThrownBy(() -> userDao.fetchUserDetails(randomId)).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void findByEmail_whenEmailExists_shouldReturnUser() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        var result = userDao.findByEmail(user.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByEmail_whenEmailDoesNotExist_shouldReturnEmpty() {
        var result = userDao.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteUsersById_shouldRemoveSpecifiedUsers() {
        var users = createMultipleDefaultUsers(3);
        testDataFactory.persistUsers(users);
        var idsToDelete = Arrays.asList(users).subList(0, 2).stream()
                .map(UserEntity::getId)
                .toList();

        userDao.deleteUsersById(idsToDelete);

        assertThat(userRepository.findAllById(idsToDelete)).isEmpty();
        assertThat(userRepository.findById(users[2].getId())).isPresent();
    }

    @Test
    void findByVerificationToken_whenTokenExists_shouldReturnUser() {
        var user = createDefaultUser();
        var token = createToken(user);
        testDataFactory.persistUser(user);
        testDataFactory.persistVerificationToken(token);

        var result = userDao.findByVerificationToken(token.getToken());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void fetchUsersWithExpiringPassword_shouldReturnEmailsOfUsersWithMatchingExpirationDate() {
        var expirationDate = LocalDate.now().plusDays(7);
        var user1 = createExpiringUsers(expirationDate);
        var user2 = createExpiringUsers(expirationDate);
        testDataFactory.persistUsers(user1, user2);

        var result = userDao.fetchUsersWithExpiringPassword(expirationDate);

        assertThat(result).hasSize(2).containsExactlyInAnyOrder(user1.getEmail(), user2.getEmail());
    }

    @Test
    void fetchUsersThatAreAboutToExpire_shouldReturnEmailsOfUsersWithMatchingExpirationDate() {
        var expirationDate = LocalDate.now().minusMonths(7);
        var user1 = createExpiringUsers(expirationDate);
        var user2 = createExpiringUsers(expirationDate);
        testDataFactory.persistUsers(user1, user2);

        var result = userDao.fetchUsersThatAreAboutToExpire(expirationDate);

        assertThat(result).hasSize(2).containsExactlyInAnyOrder(user1.getEmail(), user2.getEmail());
    }

    @Test
    void fetchExpiredOrganizationAdmins_shouldReturnExpiredOrgAdmins() {
        var expiredOrgAdmin = createDefaultUser();
        var nonExpiredOrgAdmin = createDefaultUser();
        var expiryDate = LocalDate.now();
        expiredOrgAdmin.setExpirationDate(expiryDate);
        expiredOrgAdmin.setUserRole(ROLE_ORG_ADMIN);
        nonExpiredOrgAdmin.setExpirationDate(expiryDate.plusDays(1));
        nonExpiredOrgAdmin.setUserRole(ROLE_ORG_ADMIN);
        testDataFactory.persistUsers(expiredOrgAdmin, nonExpiredOrgAdmin);

        var result = userDao.fetchExpiredOrganizationAdmins(expiryDate);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(expiredOrgAdmin.getId());
    }

    @Test
    void fetchExpiredAdmins_shouldReturnExpiredAdmins() {
        var expiredAdmin = createDefaultUser();
        var nonExpiredAdmin = createDefaultUser();
        var expiryDate = LocalDate.now();
        expiredAdmin.setExpirationDate(expiryDate);
        expiredAdmin.setUserRole(ROLE_ADMIN);
        nonExpiredAdmin.setExpirationDate(expiryDate.plusDays(1));
        nonExpiredAdmin.setUserRole(ROLE_ADMIN);
        testDataFactory.persistUsers(expiredAdmin, nonExpiredAdmin);

        var result = userDao.fetchExpiredAdmins(expiryDate);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(expiredAdmin.getId());
    }

    @Test
    void emailInUseByAnotherUser_noOtherUserExistsWithGivenEmail_expectTrue() {
        var user = createUserWithEmail("johnDoe@example.com");
        testDataFactory.persistUser(user);

        var result = userDao.emailNotInUseByAnotherUser("janeDoe@example.com", user.getId());

        assertThat(result).isTrue();
    }

    @Test
    void emailInUseByAnotherUser_otherUserExistsWithGivenEmail_expectFalse() {
        var user = createUserWithEmail("johnDoe@example.com");
        var user2 = createUserWithEmail("johnDoe@example.com");
        testDataFactory.persistUsers(user, user2);

        var result = userDao.emailNotInUseByAnotherUser("johnDoe@example.com", user2.getId());

        assertThat(result).isFalse();
    }

    @Test
    void existsByUserName_whenUsernameExists_shouldReturnTrue() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        boolean exists = userDao.existsByUserName(user.getUsername());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserName_whenUsernameDoesNotExists_shouldReturnFalse() {
        var user = createDefaultUser();
        testDataFactory.persistUser(user);

        boolean exists = userDao.existsByUserName("nonexistent");

        assertThat(exists).isFalse();
    }
}
