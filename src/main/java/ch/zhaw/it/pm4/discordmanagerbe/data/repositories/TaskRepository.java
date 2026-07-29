package ch.zhaw.it.pm4.discordmanagerbe.data.repositories;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for TaskEntity operations.
 * Provides CRUD operations and custom queries for task management.
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    /**
     * Finds the newest task for a specific user.
     * Orders by task ID descending and returns only the first result.
     *
     * @param userId Discord user ID
     * @return the most recently created task for the user
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId ORDER BY t.taskId DESC LIMIT 1")
    TaskEntity findNewestTaskByUserId(@Param("userId") String userId);

    /**
     * Finds all tasks for a specific user ordered by task ID ascending.
     *
     * @param userId Discord user ID
     * @return list of all tasks for the user ordered by creation time
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId ORDER BY t.taskId ASC")
    List<TaskEntity> findAllTaskByUserId(@Param("userId") String userId);

    /**
     * Finds all tasks that are due (due time <= current time).
     * Used to identify tasks that have reached their deadline.
     *
     * @param currentTime current timestamp in milliseconds
     * @return list of overdue tasks
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.timeToBeDone <= :currentTime")
    List<TaskEntity> findAllDueTasks(@Param("currentTime") long currentTime);
}