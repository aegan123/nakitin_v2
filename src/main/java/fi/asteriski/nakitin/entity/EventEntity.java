/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "events")
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
    private ZonedDateTime date;

    @NonNull
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, orphanRemoval = true)
    private OrganizationEntity organizer;

    @NonNull
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, optional = false)
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
}
