/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static fi.asteriski.nakitin.entity.UserRole.ROLE_ADMIN;
import static fi.asteriski.nakitin.utils.Constants.SORT_BY_LASTNAME_ASC;

import fi.asteriski.nakitin.dto.UserDto;
import fi.asteriski.nakitin.entity.UserEntity;
import fi.asteriski.nakitin.entity.UserRole;
import fi.asteriski.nakitin.repo.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDao {
    @NonNull
    private final UserRepository userRepository;

    @Value("${fi.asteriski.config.maxPasswordAgeInDays}")
    private Long maxPasswordAgeInDays;

    public UserEntity saveNewUser(UserDto dto) {
        return userRepository.save(UserEntity.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .userRole(UserRole.ROLE_USER)
                .expirationDate(LocalDate.now().plusDays(maxPasswordAgeInDays))
                .build());
    }

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

    public void editUser(UserDto userDto) {
        var user = findById(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        userRepository.save(user);
    }

    public List<UserEntity> fetchAllUsers() {
        return userRepository.findAll(SORT_BY_LASTNAME_ASC);
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
}
