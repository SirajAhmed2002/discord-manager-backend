package ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation;

/**
 * Enumeration of available server bot types for Discord server management functionality.
 * Each type represents a specific set of server administration and configuration capabilities.
 */
public enum ServerBotType {

    /**
     * Bot for configuring guild settings and preferences.
     */
    GUILD_CONFIG,

    /**
     * Bot for listing and managing guilds.
     */
    GUILD_LIST,

    /**
     * Bot for displaying detailed guild information.
     */
    GUILD_INFO,

    /**
     * Bot for listing and managing guild members.
     */
    GUILD_MEMBER_LIST,

    /**
     * Bot for creating and managing guild invitations.
     */
    GUILD_INVITE_CREATE,

    /**
     * Bot for listing and managing guild permissions.
     */
    GUILD_PERMISSION_LIST,

    /**
     * Bot for configuring guild roles and role settings.
     */
    GUILD_ROLES_CONFIG,

    /**
     * Bot for listing guild roles and role information.
     */
    GUILD_ROLES_LIST,

    /**
     * Bot for configuring member role assignments.
     */
    GUILD_MEMBER_ROLES_CONFIG,
    /**
     * Bot for configuring a role-permission to a channel
     */
    GUILD_CHANNEL_ROLE_PERMISSION,
    /**
     * Default value indicating no server bot type assigned.
     */
    NONE
}
