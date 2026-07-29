package ch.zhaw.it.pm4.discordmanagerbe.data.entities;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a Discord server with enabled bot configurations.
 * Manages bot activation states and server metadata.
 */
@Entity
@Table(name = "discord_servers")
public class DiscordServer {

    /** Unique Discord server identifier */
    @Id
    @Column(name = "server_id", nullable = false)
    private String serverId;

    /** Display name of the Discord server */
    @Column(name = "server_name", nullable = false)
    private String serverName;

    /** Discord ID of the server owner */
    @Column(name = "owner_id")
    private String ownerId;

    /** Timestamp when the server was first registered */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last server update */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Set of enabled bot types for this server */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "server_enabled_bots",
            joinColumns = @JoinColumn(name = "server_id")
    )
    @Column(name = "bot_type")
    @Enumerated(EnumType.STRING)
    private Set<SlashCommandBotType> enabledBots = new HashSet<>();

    /**
     * Default constructor initializing creation timestamp.
     */
    public DiscordServer() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a new Discord server with specified details.
     *
     * @param serverId Discord server ID
     * @param serverName server display name
     * @param ownerId Discord ID of the server owner
     */
    public DiscordServer(String serverId, String serverName, String ownerId) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.ownerId = ownerId;
        this.createdAt = LocalDateTime.now();
        // All bots are disabled by default (empty set)
    }

    /**
     * Enables a specific bot type for this server.
     * Updates the modification timestamp if successful.
     *
     * @param botType the bot type to enable
     * @return true if the bot was newly enabled, false if already enabled
     */
    public boolean enableBot(SlashCommandBotType botType) {
        boolean added = enabledBots.add(botType);
        if (added) {
            this.updatedAt = LocalDateTime.now();
        }
        return added;
    }

    /**
     * Disables a specific bot type for this server.
     * Updates the modification timestamp if successful.
     *
     * @param botType the bot type to disable
     * @return true if the bot was disabled, false if not previously enabled
     */
    public boolean disableBot(SlashCommandBotType botType) {
        boolean removed = enabledBots.remove(botType);
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    /**
     * Checks if a specific bot type is enabled for this server.
     *
     * @param botType the bot type to check
     * @return true if the bot is enabled, false otherwise
     */
    public boolean isBotEnabled(SlashCommandBotType botType) {
        return enabledBots.contains(botType);
    }

    /**
     * Gets the Discord server ID.
     *
     * @return the server ID
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Sets the Discord server ID.
     *
     * @param serverId the server ID
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Gets the server display name.
     *
     * @return the server name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Sets the server display name and updates modification timestamp.
     *
     * @param serverName the server name
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the Discord ID of the server owner.
     *
     * @return the owner ID
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Sets the Discord ID of the server owner and updates modification timestamp.
     *
     * @param ownerId the owner ID
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the server creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return the update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Gets a copy of enabled bot types.
     *
     * @return set of enabled bot types
     */
    public Set<SlashCommandBotType> getEnabledBots() {
        return new HashSet<>(enabledBots);
    }

    @Override
    public String toString() {
        return "DiscordServer{" +
                "serverId='" + serverId + '\'' +
                ", serverName='" + serverName + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", enabledBots=" + enabledBots +
                '}';
    }
}