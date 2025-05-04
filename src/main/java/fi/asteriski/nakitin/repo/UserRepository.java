/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.repo;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.UserRole;
import fi.asteriski.nakitin.repo.projection.EmailProjection;
import fi.asteriski.nakitin.repo.projection.IdFirstLastNameProjection;
import fi.asteriski.nakitin.repo.projection.UserDetailsProjection;
import java.time.LocalDate;
import java.util.*;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM UserEntity u WHERE u.email = :email AND u.id != :id")
    UUID emailInUseByAnotherUser(@Param("email") String email, @Param("id") UUID id);

    long countAllByUserRoleAndIdNot(@NonNull UserRole userRole, UUID id);

    @Query(
            "select u.id as id, u.firstName as firstName, u.lastName as lastName from UserEntity u where u.userRole != 'ROLE_ADMIN'")
    Set<IdFirstLastNameProjection> fetchAllUsers();

    Optional<UserDetailsProjection> findUserEntityById(UUID id);

    Optional<UserEntity> findByEmail(String email);

    @Modifying
    @Query("DELETE FROM UserEntity u WHERE u.id IN :toDelete")
    void deleteUsersById(@Param("toDelete") List<UUID> toDelete);

    Optional<UserEntity> findByVerificationToken_Token(String verificationTokenToken);

    List<EmailProjection> readAllByExpirationDateIs(LocalDate expirationDate);

    @Modifying
    @Query("UPDATE UserEntity u SET u.enabled = false, u.locked = true WHERE u.expirationDate = :today")
    void disableUsersThatExpireToday(@Param(("today")) LocalDate today);

    @Modifying
    @Query("UPDATE UserEntity u SET u.enabled = true, u.locked = false WHERE u.id = :id")
    void enableUser(@Param(("id")) UUID id);

    @Modifying
    @Query("UPDATE UserEntity u SET u.enabled = false, u.locked = true WHERE u.id = :id")
    void disableUser(@Param("id") UUID id);

    @Modifying
    @Query(
            "DELETE FROM EventTaskEntity t WHERE t.event.id IN (SELECT e.id FROM UserEntity e WHERE e.expirationDate = :expiryDate)")
    void deleteTasksOfExpiredUsers(@Param("expiryDate") LocalDate expiryDate);

    @Modifying
    @Query("DELETE FROM UserEntity u WHERE u.userRole = :role AND u.expirationDate = :expiryDate")
    void deleteExpiredUsers(@Param("expiryDate") LocalDate expiryDate, @Param("role") UserRole role);

    List<UserEntity> findAllByUserRoleAndExpirationDate(UserRole userRole, LocalDate expirationDate);
}
