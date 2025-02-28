/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.service;

import fi.asteriski.nakitin.dao.UserDao;
import fi.asteriski.nakitin.dto.SignupForm;
import fi.asteriski.nakitin.dto.UserDto;
import fi.asteriski.nakitin.entity.UserEntity;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserDao userDao;

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
    public void updateUser(UserDto userDto) {
        userDao.editUser(userDto);
    }
}
