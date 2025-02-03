/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import fi.asteriski.nakitin.dto.EventDto;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

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

    @NonNull
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    private OrganizationEntity organizer;

    @NonNull
    @ManyToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    private UserEntity createdBy;

    private UUID signupSystemEvent;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Override
    public String toString() {
        return organizer.getName() + ": " + name;
    }

    public EventDto toDto() {
        return EventDto.builder()
                .id(id)
                .name(name)
                .venue(venue)
                .description(description)
                .date(date)
                .organizer(organizer.toDto())
                .build();
    }
}
