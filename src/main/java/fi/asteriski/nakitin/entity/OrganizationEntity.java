/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import fi.asteriski.nakitin.dto.OrganizationDto;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "organizations",
        indexes = {@Index(name = "idx_organization_name", columnList = "name", unique = true)})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraphs(
        value = {
            @NamedEntityGraph(
                    name = "graph_organizations_events",
                    attributeNodes = {@NamedAttributeNode(value = "events")}),
            @NamedEntityGraph(
                    name = "graph_organizations_users",
                    attributeNodes = {@NamedAttributeNode(value = "users")}),
        })
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "organizations", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<UserEntity> users = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organizer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventEntity> events;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime createdAt;

    public void addUser(UserEntity user) {
        users.add(user);
        user.getOrganizations().add(this);
    }

    public void removeUser(UserEntity user) {
        users.remove(user);
        user.getOrganizations().removeIf(this::equals);
    }

    public void addEvent(EventEntity event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(event);
        event.setOrganizer(this);
    }

    public void removeEvent(EventEntity event) {
        events.remove(event);
        event.setOrganizer(null);
    }

    public OrganizationDto toDto() {
        return OrganizationDto.builder()
                .id(id)
                .name(name)
                .users(users)
                .events(events.stream().map(EventEntity::toDto).toList())
                .build();
    }

    public OrganizationDto toEventPageDto() {
        return OrganizationDto.builder().id(id).name(name).build();
    }

    public OrganizationDto toAdminDto() {
        return OrganizationDto.builder()
                .id(id)
                .name(name)
                .users(users)
                .events(events.stream().map(EventEntity::toAdminDto).toList())
                .build();
    }
}
