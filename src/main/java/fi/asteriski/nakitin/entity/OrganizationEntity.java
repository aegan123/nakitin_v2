/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import fi.asteriski.nakitin.dto.OrganizationDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserEntity> users = new HashSet<>();

    @NonNull
    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EventEntity> events;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime createdAt;

    public void addUser(UserEntity user) {
        users.add(user);
        user.setOrganization(this);
    }

    public void removeUser(UserEntity user) {
        users.remove(user);
        user.setOrganization(null);
    }

    public void addEvent(EventEntity comment) {
        events.add(comment);
        comment.setOrganizer(this);
    }

    public void removeEvent(EventEntity comment) {
        events.remove(comment);
        comment.setOrganizer(null);
    }

    public OrganizationDto toDto() {
        return OrganizationDto.builder().id(id).name(name).build();
    }
}
