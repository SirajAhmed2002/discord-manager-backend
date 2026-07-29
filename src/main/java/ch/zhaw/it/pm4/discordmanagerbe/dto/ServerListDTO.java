package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * DTO representing a list of Discord servers.
 * Contains multiple server information objects.
 */
public class ServerListDTO {
    /** List of Discord servers */
    private List<ServerDTO> servers;

    /**
     * Gets the list of servers.
     * @return List of Discord servers
     */
    public List<ServerDTO> getServers() {
        return servers;
    }

    /**
     * Sets the list of servers.
     * @param servers List of Discord servers to set
     */
    public void setServers(List<ServerDTO> servers) {
        this.servers = servers;
    }
}