/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.repo;

import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.VerificationTokenEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, UUID> {
    Optional<VerificationTokenEntity> findByToken(String token);

    void deleteByUser(UserEntity user);

    List<VerificationTokenEntity> findAllByExpiryDateIsBefore(LocalDateTime expiryDateBefore);

    void deleteAllByUserIn(Collection<UserEntity> users);
}
