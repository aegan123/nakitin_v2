/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import fi.asteriski.nakitin.dto.UserDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;
import java.util.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(
        name = "users",
        indexes = {
            @Index(name = "idx_username", columnList = "username"),
            @Index(name = "idx_email", columnList = "email")
        })
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

    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.EAGER)
    @JoinTable(
            name = "organizationadmins",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id"))
    @Builder.Default
    private Set<OrganizationEntity> organizations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<EventEntity> events;

    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.EAGER)
    @JoinTable(
            name = "nakittautuneet",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_task_id"))
    @Builder.Default
    private List<EventTaskEntity> eventsTasks = new ArrayList<>();

    @NonNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole userRole = UserRole.ROLE_USER;

    @Builder.Default
    @Column(nullable = false)
    private Boolean locked = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Transient
    private boolean isAdmin = false;

    public boolean isOrganisationAdmin() {
        return userRole == UserRole.ROLE_ORG_ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(userRole.name()));
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
        return !expirationDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void addEventTask(EventTaskEntity eventTask) {
        eventsTasks.add(eventTask);
        eventTask.getVolunteers().add(this);
    }

    public void removeEventTask(EventTaskEntity eventTask) {
        eventsTasks.remove(eventTask);
        eventTask.getVolunteers().remove(this);
    }

    public void addEvent(EventEntity event) {
        events.add(event);
        event.setCreatedBy(this);
    }

    public void removeEvent(EventEntity event) {
        events.remove(event);
        event.setCreatedBy(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;
        return id != null && id.equals(((UserEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public UserDto toDto() {
        return UserDto.builder()
                .id(id)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .build();
    }
}
