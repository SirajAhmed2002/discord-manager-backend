package ch.zhaw.it.pm4.discordmanagerbe.data.repositories;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.DiscordServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DiscordServer entity operations.
 * Provides CRUD operations and custom queries for Discord server management.
 */
@Repository
public interface DiscordServerRepository extends JpaRepository<DiscordServer, String> {

    /**
     * Finds all servers owned by a specific user.
     *
     * @param ownerId Discord ID of the server owner
     * @return list of servers owned by the user
     */
    List<DiscordServer> findByOwnerId(String ownerId);

    /**
     * Finds a server by its Discord server ID.
     *
     * @param serverId Discord server ID
     * @return optional containing the server if found
     */
    Optional<DiscordServer> findByServerId(String serverId);

    /**
     * Checks if a server exists by its Discord server ID.
     *
     * @param serverId Discord server ID
     * @return true if server exists, false otherwise
     */
    boolean existsByServerId(String serverId);
}