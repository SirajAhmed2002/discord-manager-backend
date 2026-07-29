package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * DTO representing a list of Discord server members.
 * Contains server ID and the list of members.
 */
public class MemberListDTO {
    /** The ID of the Discord server */
    private String id;
    /** List of members in the server */
    private List<MemberDTO> members;

    /**
     * Constructor with server ID and members list.
     * @param id The Discord server ID
     * @param members List of server members
     */
    public MemberListDTO(String id, List<MemberDTO> members) {
        this.id = id;
        this.members = members;
    }

    /**
     * Gets the server ID.
     * @return The Discord server ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the server ID.
     * @param id The Discord server ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the list of members.
     * @return List of server members
     */
    public List<MemberDTO> getMembers() {
        return members;
    }

    /**
     * Sets the list of members.
     * @param members List of server members to set
     */
    public void setMembers(List<MemberDTO> members) {
        this.members = members;
    }
}