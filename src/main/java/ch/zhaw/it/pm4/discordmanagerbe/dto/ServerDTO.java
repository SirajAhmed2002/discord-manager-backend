package ch.zhaw.it.pm4.discordmanagerbe.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

/**
 * DTO representing a Discord server.
 * Contains basic server information including metadata and statistics.
 */
public class ServerDTO {
    /** The unique ID of the Discord server */
    private String id;
    /** The name of the Discord server */
    private String name;
    /** The ID of the server owner */
    private String ownerId;
    /** The current number of members in the server */
    private int memberCount;

    /** The creation date of the server */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private OffsetDateTime creationDate;

    /**
     * Gets the server ID.
     * @return The unique Discord server ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the server ID.
     * @param id The unique Discord server ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the server name.
     * @return The server name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the server name.
     * @param name The server name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the owner ID.
     * @return The Discord ID of the server owner
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Sets the owner ID.
     * @param ownerId The Discord ID of the server owner to set
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Gets the member count.
     * @return The current number of server members
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * Sets the member count.
     * @param memberCount The current number of server members to set
     */
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    /**
     * Gets the server creation date.
     * @return The date and time when the server was created
     */
    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the server creation date.
     * @param creationDate The date and time when the server was created to set
     */
    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }
}