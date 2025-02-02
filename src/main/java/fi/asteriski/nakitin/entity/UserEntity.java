/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(
        name = "users",
        indexes = {@Index(name = "idx_username", columnList = "username")})
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(unique = true, nullable = false)
    private String username;

    @NonNull
    @Column(nullable = false)
    private String password;

    @NonNull
    @Column(nullable = false)
    private String firstName;

    @NonNull
    @Column(nullable = false)
    private String lastName;

    @Email
    @NonNull
    @Column(nullable = false)
    private String email;

    @NonNull
    @Column(nullable = false)
    private LocalDate expirationDate;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EventEntity> events;

    @NonNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole userRole = UserRole.ROLE_USER;

    @Builder.Default
    private Boolean locked = false;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    @Transient
    private boolean isAdmin = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final var simpleGrantedAuthority = new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(simpleGrantedAuthority);
    }

    @Override
    public boolean isAccountNonExpired() {
        return isCredentialsNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return expirationDate == null || expirationDate.isAfter(LocalDate.now());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
