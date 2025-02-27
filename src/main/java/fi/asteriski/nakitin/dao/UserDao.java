/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import fi.asteriski.nakitin.dto.UserDto;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.UserRole;
import fi.asteriski.nakitin.repo.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDao {
    @NonNull
    private final UserRepository userRepository;

    @Value("${fi.asteriski.config.maxPasswordAgeInDays}")
    private Long maxPasswordAgeInDays;

    public void saveUser(UserDto dto) {
        userRepository.save(UserEntity.builder()
                .username(dto.username())
                .email(dto.email())
                .password(dto.password())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .userRole(UserRole.ROLE_USER)
                .expirationDate(LocalDate.now().plusDays(maxPasswordAgeInDays))
                .build());
    }

    public Optional<UserEntity> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUserName(String username) {
        return userRepository.existsByUsername(username);
    }
}
