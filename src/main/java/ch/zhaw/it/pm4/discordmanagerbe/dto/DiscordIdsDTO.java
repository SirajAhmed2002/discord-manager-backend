package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * DTO for Discord Server, Channel, and User IDs.
 * Provides a convenient way to group related Discord identifiers.
 */
public class DiscordIdsDTO {

    /** The Discord server ID */
    private String serverId;
    /** The Discord channel ID */
    private String channelId;
    /** The Discord user ID */
    private String userId;

    /**
     * Default constructor.
     */
    public DiscordIdsDTO() {
        // Default constructor
    }

    /**
     * Constructor with all IDs.
     * @param serverId The Discord server ID
     * @param channelId The Discord channel ID
     * @param userId The Discord user ID
     */
    public DiscordIdsDTO(String serverId, String channelId, String userId) {
        this.serverId = serverId;
        this.channelId = channelId;
        this.userId = userId;
    }

    /**
     * Creates a new builder instance for constructing DiscordIdsDTO objects.
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the server ID.
     * @return The Discord server ID
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Sets the server ID.
     * @param serverId The Discord server ID to set
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Gets the channel ID.
     * @return The Discord channel ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the channel ID.
     * @param channelId The Discord channel ID to set
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Gets the user ID.
     * @return The Discord user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     * @param userId The Discord user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Builder class for constructing DiscordIdsDTO objects step by step.
     */
    public static class Builder {
        /** The Discord server ID */
        private String serverId;
        /** The Discord channel ID */
        private String channelId;
        /** The Discord user ID */
        private String userId;

        /**
         * Sets the server ID.
         * @param serverId The Discord server ID
         * @return This builder instance
         */
        public Builder serverId(String serverId) {
            this.serverId = serverId;
            return this;
        }

        /**
         * Sets the channel ID.
         * @param channelId The Discord channel ID
         * @return This builder instance
         */
        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        /**
         * Sets the user ID.
         * @param userId The Discord user ID
         * @return This builder instance
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Builds the DiscordIdsDTO object with the configured values.
         * @return A new DiscordIdsDTO instance
         */
        public DiscordIdsDTO build() {
            return new DiscordIdsDTO(serverId, channelId, userId);
        }
    }

    /**
     * Returns a string representation of the DiscordIdsDTO.
     * @return String representation containing all ID values
     */
    @Override
    public String toString() {
        return "DiscordIdsDTO{" +
                "serverId='" + serverId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}