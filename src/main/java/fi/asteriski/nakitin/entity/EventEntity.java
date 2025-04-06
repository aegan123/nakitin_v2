/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import fi.asteriski.nakitin.dto.EventDto;
import fi.asteriski.nakitin.dto.UpcomingEventDto;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "events",
        indexes = {@Index(name = "idx_date", columnList = "date")})
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@NamedEntityGraphs(
        value = {
            @NamedEntityGraph(
                    name = "graph_event_organization",
                    attributeNodes = {@NamedAttributeNode(value = "organizer")}),
            @NamedEntityGraph(
                    name = "graph_event_eventTasks",
                    attributeNodes = {@NamedAttributeNode(value = "tasks")})
        })
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

    @ManyToOne(fetch = FetchType.EAGER)
    private OrganizationEntity organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventTaskEntity> tasks = new ArrayList<>();

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
                .tasks(tasks.stream().map(EventTaskEntity::toDto).toList())
                .createdAt(createdAt)
                .build();
    }

    public EventDto toEventPageDto() {
        return EventDto.builder()
                .id(id)
                .name(name)
                .venue(venue)
                .description(description)
                .date(date)
                .organizer(organizer.toEventPageDto())
                .tasks(tasks.stream().map(EventTaskEntity::toDto).toList())
                .abbreviatedDescription(getAbbreviatedDescription())
                .createdAt(createdAt)
                .build();
    }

    public EventDto toAdminDto() {
        return EventDto.builder()
                .id(id)
                .name(name)
                .venue(venue)
                .description(description)
                .date(date)
                .tasks(tasks.stream().map(EventTaskEntity::toDto).toList())
                .createdAt(createdAt)
                .build();
    }

    public UpcomingEventDto toUpcomingEventDto() {
        return new UpcomingEventDto(id, name, organizer.getName(), date);
    }

    private String getAbbreviatedDescription() {
        var abbreviatedDescription = description.substring(0, Math.min(100, description.length()));
        if (abbreviatedDescription.length() != description.length()) {
            abbreviatedDescription += "...";
        }
        return abbreviatedDescription;
    }
}
