/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fi.asteriski.nakitin.entity.EventTaskEntity;
import fi.asteriski.nakitin.exceptions.EventTaskNotFoundException;
import fi.asteriski.nakitin.repo.EventRepository;
import fi.asteriski.nakitin.repo.EventTaskRepository;
import fi.asteriski.nakitin.repo.OrganizationRepository;
import fi.asteriski.nakitin.repo.UserRepository;
import fi.asteriski.nakitin.test.factory.TestDataFactory;
import fi.asteriski.nakitin.test.fixtures.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class EventTaskDaoTest extends TestFixtures {

    @Autowired
    private EventTaskRepository eventTaskRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private EventTaskDao eventTaskDao;
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        eventTaskDao = new EventTaskDao(eventTaskRepository);
        testDataFactory = TestDataFactory.builder()
                .eventTaskRepository(eventTaskRepository)
                .eventRepository(eventRepository)
                .userRepository(userRepository)
                .organizationRepository(organizationRepository)
                .build();
    }

    @AfterEach
    void tearDown() {
        testDataFactory.cleanUp();
    }

    @Test
    void fetchEventTasksByIds_whenTasksExist_shouldReturnTasks() {
        var task1 = createEventTask("Task 1", LocalDate.now(), 2);
        var task2 = createEventTask("Task 2", LocalDate.now(), 3);
        testDataFactory.persistEventTasks(task1, task2);
        var taskIds = List.of(task1.getId(), task2.getId(), UUID.randomUUID());

        var result = eventTaskDao.fetchEventTasksByIds(taskIds);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(EventTaskEntity::getTaskName)
                .containsExactlyInAnyOrder(task1.getTaskName(), task2.getTaskName());
    }

    @Test
    void fetchEventTasksByIds_whenNoTasksExist_shouldReturnEmptyList() {
        var nonExistentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        var result = eventTaskDao.fetchEventTasksByIds(nonExistentIds);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchUsersEventTasks_whenUserHasTasks_shouldReturnTasks() {
        var user = testDataFactory.persistUser(createDefaultUser());
        var tasks = testDataFactory.persistEventTasks(
                createEventTask("Task 1", LocalDate.now(), 2), createEventTask("Task 2", LocalDate.now(), 3));
        user.addEventTask(tasks.getFirst());
        user.addEventTask(tasks.getLast());
        testDataFactory.persistEventTasks(tasks.getFirst(), tasks.getLast());
        user = testDataFactory.persistUser(user);

        var result = eventTaskDao.fetchUsersEventTasks(user);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(EventTaskEntity::getTaskName)
                .containsExactlyInAnyOrder(
                        tasks.getFirst().getTaskName(), tasks.getLast().getTaskName());
    }

    @Test
    void fetchUsersEventTasks_whenUserHasNoTasks_shouldReturnEmptyList() {
        var user = testDataFactory.persistUser(createDefaultUser());

        var result = eventTaskDao.fetchUsersEventTasks(user);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchTask_whenTaskExists_shouldReturnTask() {
        var task = createEventTask("Test Task", LocalDate.now(), 2);
        testDataFactory.persistEventTask(task);

        var result = eventTaskDao.fetchTask(task.getId());

        assertThat(result.getId()).isEqualTo(task.getId());
        assertThat(result.getTaskName()).isEqualTo("Test Task");
    }

    @Test
    void fetchTask_whenTaskDoesNotExist_shouldThrowException() {
        var nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> eventTaskDao.fetchTask(nonExistentId))
                .isInstanceOf(EventTaskNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    void deleteTask_shouldRemoveTaskFromDatabase() {
        var task = testDataFactory.persistEventTask(createEventTask("Test Task", LocalDate.now(), 2));

        eventTaskDao.deleteTask(task);

        assertThat(eventTaskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    void deleteTask_whenTaskHasVolunteers_shouldRemoveTaskAndDisassociateVolunteers() {
        var user = testDataFactory.persistUser(createDefaultUser());

        var task = createEventTask("Test Task", LocalDate.now(), 2);
        task.setVolunteers(Set.of(user));
        testDataFactory.persistEventTask(task);

        eventTaskDao.deleteTask(task);

        assertThat(eventTaskRepository.findById(task.getId())).isEmpty();
        var savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getEventTasks()).isEmpty();
    }
}
