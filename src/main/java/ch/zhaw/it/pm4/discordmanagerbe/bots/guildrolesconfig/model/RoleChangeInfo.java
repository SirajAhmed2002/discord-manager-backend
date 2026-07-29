package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model;

import java.util.List;

/**
 * Information about changes made to a role during the sync process.
 */
public class RoleChangeInfo {

    /**
     * The name of the role.
     */
    private final String name;

    /**
     * The unique identifier of the role.
     */
    private final String id;

    /**
     * List of changes made to the role.
     */
    private final List<String> changes;

    /**
     * Constructor for creating a RoleChangeInfo instance.
     * @param name name of the role
     * @param id unique identifier of the role
     * @param changes list of changes made to the role
     */
    public RoleChangeInfo(String name, String id, List<String> changes) {
        this.name = name;
        this.id = id;
        this.changes = changes;
    }

    /**
     * Get the name of the role.
     * @return the name of the role
     */
    public String getName() {
        return name;
    }

    /**
     * Get the unique identifier of the role.
     * @return the unique identifier of the role
     */
    public String getId() {
        return id;
    }

    /**
     * Get the list of changes made to the role.
     * @return the list of changes made to the role
     */
    public List<String> getChanges() {
        return changes;
    }
}
