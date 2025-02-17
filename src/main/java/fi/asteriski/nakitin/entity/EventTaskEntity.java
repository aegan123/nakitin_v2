/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import fi.asteriski.nakitin.dto.EventTaskDto;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NaturalIdCache
@Builder
@AllArgsConstructor
public class EventTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private EventEntity event;

    @NonNull
    @Column(nullable = false)
    @NaturalId
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

    @ManyToMany(mappedBy = "eventsTasks")
    private Set<UserEntity> volunteers = new HashSet<>();

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Override
    public String toString() {
        return String.format(
                "% @ % by %", taskName, event.getName(), event.getOrganizer().getName());
    }

    public EventTaskDto toDto() {
        return EventTaskDto.builder()
                .id(id)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .personCount(personCount)
                .taskName(taskName)
                .volunteers(volunteers.stream().map(UserEntity::toDto).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(taskName, ((EventTaskEntity) o).taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName);
    }
}
