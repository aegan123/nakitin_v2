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
@Table(name = "organizations")
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

    @ManyToMany(mappedBy = "organizations")
    @Builder.Default
    private Set<UserEntity> users = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organizer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
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
        user.getOrganizations().remove(this);
    }

    public void addEvent(EventEntity comment) {
        events.add(comment);
        comment.setOrganizer(this);
    }

    public void removeEvent(EventEntity comment) {
        events.remove(comment);
        comment.setOrganizer(null);
    }

    public boolean hasNoUsers() {
        return users.isEmpty();
    }

    public OrganizationDto toDto() {
        return OrganizationDto.builder().id(id).name(name).users(users).build();
    }
}
