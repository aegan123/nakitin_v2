/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import fi.asteriski.nakitin.dto.EventDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "events",
        indexes = {@Index(name = "idx_date", columnList = "date")})
@Data
@NoArgsConstructor
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    @Column(nullable = false)
    private String venue;

    @NonNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NonNull
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrganizationEntity organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity createdBy;

    @NonNull
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventTaskEntity> tasks = new HashSet<>();

    private UUID signupSystemEvent;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Override
    public String toString() {
        return String.format("%s: %s", organizer.getName(), name);
    }

    public void addTask(EventTaskEntity task) {
        tasks.add(task);
        task.setEvent(this);
    }

    public void removeTask(EventTaskEntity task) {
        tasks.remove(task);
        task.setEvent(null);
    }

    public EventDto toDto() {
        return EventDto.builder()
                .id(id)
                .name(name)
                .venue(venue)
                .description(description)
                .date(date)
                .organizer(organizer.toDto())
                .tasks(tasks.stream().map(EventTaskEntity::toDto).collect(Collectors.toSet()))
                .build();
    }
}
