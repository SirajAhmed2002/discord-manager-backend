package ch.zhaw.it.pm4.discordmanagerbe.data.repositories;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Subject entity operations.
 * Provides CRUD operations and custom queries for subject management with Discord context.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Finds all subjects for a specific Discord context (server, channel, user).
     *
     * @param serverId Discord server ID
     * @param channelId Discord channel ID
     * @param userId Discord user ID
     * @return list of subjects for the specified context
     */
    List<Subject> findByServerIdAndChannelIdAndUserId(String serverId, String channelId, String userId);

    /**
     * Finds all distinct semesters for a specific Discord context.
     * Returns only non-null semesters ordered alphabetically.
     *
     * @param serverId Discord server ID
     * @param channelId Discord channel ID
     * @param userId Discord user ID
     * @return list of distinct semesters
     */
    @Query("SELECT DISTINCT s.semester FROM Subject s WHERE s.serverId = :serverId AND s.channelId = :channelId AND s.userId = :userId AND s.semester IS NOT NULL ORDER BY s.semester")
    List<String> findSemestersByServerIdAndChannelIdAndUserId(String serverId, String channelId, String userId);

    /**
     * Finds all subjects for a specific Discord context and semester.
     *
     * @param serverId Discord server ID
     * @param channelId Discord channel ID
     * @param userId Discord user ID
     * @param semester semester identifier
     * @return list of subjects for the specified context and semester
     */
    List<Subject> findByServerIdAndChannelIdAndUserIdAndSemester(String serverId, String channelId, String userId, String semester);

    /**
     * Finds a subject by Discord context, name, and semester.
     * Used to check for duplicate subjects.
     *
     * @param serverId Discord server ID
     * @param channelId Discord channel ID
     * @param userId Discord user ID
     * @param name subject name
     * @param semester semester identifier
     * @return optional containing the subject if found
     */
    Optional<Subject> findByServerIdAndChannelIdAndUserIdAndNameAndSemester(String serverId, String channelId, String userId, String name, String semester);
}