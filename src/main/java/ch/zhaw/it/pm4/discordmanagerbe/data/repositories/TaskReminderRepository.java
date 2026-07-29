package ch.zhaw.it.pm4.discordmanagerbe.data.repositories;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for TaskReminderEntity operations.
 * Provides CRUD operations and custom queries for task reminder management.
 */
@Repository
public interface TaskReminderRepository extends JpaRepository<TaskReminderEntity, Long>{

    /**
     * Finds all reminders that are due (reminder time <= current time).
     * Used to identify reminders that should be triggered.
     *
     * @param currentTime current timestamp in milliseconds
     * @return list of due reminders
     */
    @Query("SELECT r FROM TaskReminderEntity r WHERE r.reminderTime <= :currentTime")
    List<TaskReminderEntity> findAllDueReminder(@Param("currentTime") long currentTime);

    /**
     * Deletes a task reminder by its ID.
     * Used to remove triggered or cancelled reminders.
     *
     * @param id reminder ID to delete
     */
    @Modifying
    @Query("DELETE FROM TaskReminderEntity r WHERE r.id = :id")
    void deleteTaskReminderById(@Param("id") Long id);
}