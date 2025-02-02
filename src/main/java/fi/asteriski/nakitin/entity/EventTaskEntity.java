/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
public class EventTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private EventEntity event;

    @NonNull
    @Column(nullable = false)
    private String taskName;

    @NonNull
    @Column(nullable = false)
    private LocalDate date;

    @NonNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NonNull
    @Column(nullable = false)
    private LocalTime endTime;

    @NonNull
    @Column(nullable = false)
    @Min(value = 1)
    private Integer personCount = 1;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Override
    public String toString() {
        return taskName + " @ " + event.getName() + " by "
                + event.getOrganizer().getName();
    }
}
