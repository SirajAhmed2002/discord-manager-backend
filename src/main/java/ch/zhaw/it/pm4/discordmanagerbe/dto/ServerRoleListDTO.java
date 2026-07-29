package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * DTO for server role list responses.
 * Contains all roles available on a Discord server.
 */
public class ServerRoleListDTO {
    /** List of extended roles with full information */
    private final List<ExtendedRoleDTO> roles;

    /**
     * Constructor with roles list.
     * @param roles List of extended role DTOs
     */
    public ServerRoleListDTO(List<ExtendedRoleDTO> roles) {
        this.roles = roles;
    }

    /**
     * Gets the list of roles.
     * @return List of extended role DTOs with full role information
     */
    public List<ExtendedRoleDTO> getRoles() {
        return roles;
    }
}