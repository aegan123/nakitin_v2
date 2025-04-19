/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import static fi.asteriski.nakitin.entity.UserRole.*;

import fi.asteriski.nakitin.dto.UserDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@NamedEntityGraphs(
        value = {
            @NamedEntityGraph(
                    name = "graph_user_organizations",
                    attributeNodes = {
                        @NamedAttributeNode(value = "organizations", subgraph = "subgraph_user_organizations_users")
                    },
                    subgraphs = {
                        @NamedSubgraph(
                                name = "subgraph_user_organizations_users",
                                attributeNodes = {@NamedAttributeNode(value = "users")})
                    }),
            @NamedEntityGraph(
                    name = "graph_user_tasks",
                    attributeNodes = {@NamedAttributeNode(value = "eventTasks")}),
            @NamedEntityGraph(
                    name = "graph_user_events",
                    attributeNodes = {@NamedAttributeNode(value = "events")})
        })
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
            fetch = FetchType.LAZY)
    @JoinTable(
            name = "nakittautuneet",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_task_id"))
    @Builder.Default
    private Set<EventTaskEntity> eventTasks = new LinkedHashSet<>();

    @NonNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole userRole = ROLE_USER;

    @Builder.Default
    @Column(nullable = false)
    private Boolean locked = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(length = 64)
    private String verificationToken;

    @Column
    private LocalDateTime verificationTokenExpiry;

    public boolean isOrganisationAdmin() {
        return userRole == ROLE_ORG_ADMIN;
    }

    public boolean isAdmin() {
        return userRole == ROLE_ADMIN;
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
        eventTasks.add(eventTask);
        eventTask.getVolunteers().add(this);
    }

    public void removeEventTask(EventTaskEntity eventTask) {
        eventTasks.remove(eventTask);
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

    public void removeOrganizationAdminRights() {
        setUserRole(ROLE_USER);
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
