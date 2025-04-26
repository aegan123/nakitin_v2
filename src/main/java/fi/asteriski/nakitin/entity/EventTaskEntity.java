/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.entity;

import fi.asteriski.nakitin.dto.EventTaskDto;
import fi.asteriski.nakitin.utils.LocaleDateFormattable;
import fi.asteriski.nakitin.utils.LocaleTimeFormattable;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import org.hibernate.annotations.*;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NaturalIdCache
@Builder
@AllArgsConstructor
@NamedEntityGraphs(
        value = {
            @NamedEntityGraph(
                    name = "graph_eventTask_volunteers",
                    attributeNodes = {@NamedAttributeNode(value = "volunteers")}),
            @NamedEntityGraph(
                    name = "graph_eventTask_event",
                    attributeNodes = {@NamedAttributeNode(value = "event")})
        })
public class EventTaskEntity implements LocaleDateFormattable, LocaleTimeFormattable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
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
    @Builder.Default
    private Integer personCount = 1;

    @ManyToMany(mappedBy = "eventTasks")
    @Builder.Default
    private Set<UserEntity> volunteers = new LinkedHashSet<>();

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Override
    public String toString() {
        return String.format(
                "%s @ %s by %s", taskName, event.getName(), event.getOrganizer().getName());
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
        var other = (EventTaskEntity) o;
        return Objects.equals(taskName, other.taskName)
                && Objects.equals(date, other.date)
                && Objects.equals(startTime, other.startTime)
                && Objects.equals(endTime, other.endTime)
                && Objects.equals(personCount, other.personCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName);
    }
}
