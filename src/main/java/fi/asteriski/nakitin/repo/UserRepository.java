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
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @NativeQuery(
            value = """
            select id from users
            where email = :email and id != :id
            """)
    UUID emailInUseByAnotherUser(@Param("email") String email, @Param("id") UUID id);

    long countAllByUserRoleAndIdNot(@NonNull UserRole userRole, UUID id);

    @NativeQuery(
            value =
                    """
                select id, first_name, last_name from users where user_role != 'ROLE_ADMIN'
            """)
    Set<IdFirstLastNameProjection> fetchAllUsers();

    Optional<UserDetailsProjection> findUserEntityById(UUID id);

    Optional<UserEntity> findByEmail(String email);

    @Modifying
    @NativeQuery("""
        delete from users where id in :toDelete
        """)
    void deleteUsersById(@Param("toDelete") List<UUID> toDelete);

    Optional<UserEntity> findByVerificationToken_Token(String verificationTokenToken);

    List<EmailProjection> readAllByExpirationDateIs(LocalDate expirationDate);

    @Modifying
    @NativeQuery(
            """
            UPDATE users
            SET enabled = false, locked = true
            WHERE expiration_date = :today
        """)
    void disableUsersThatExpireToday(@Param(("today")) LocalDate today);

    @Modifying
    @NativeQuery(
            """
            UPDATE users
            SET enabled = true, locked = false
            WHERE id = :id
        """)
    void enableUser(@Param(("id")) UUID id);

    @Modifying
    @NativeQuery(
            """
            UPDATE users
            SET enabled = false, locked = true
            WHERE id = :id
        """)
    void disableUser(@Param("id") UUID id);

    @Modifying
    @NativeQuery(
            """
            DELETE from nakittautuneet WHERE user_id in (SELECT u.id from users u WHERE expiration_date = :expiryDate)
        """)
    void deleteTasksOfExpiredUsers(@Param("expiryDate") LocalDate expiryDate);

    @Modifying
    @NativeQuery(
            """
            DELETE from users WHERE user_role = :role
             AND id in (SELECT u.id from users u WHERE expiration_date = :expiryDate)
        """)
    void deleteExpiredUsers(@Param("expiryDate") LocalDate expiryDate, @Param("role") String role);

    List<UserEntity> findAllByUserRoleAndExpirationDate(UserRole userRole, LocalDate expirationDate);
}
