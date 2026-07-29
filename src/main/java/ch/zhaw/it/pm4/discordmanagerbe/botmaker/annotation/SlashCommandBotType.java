package ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation;

/**
 * Enumeration of available slash command bot types for Discord slash command functionality.
 * Each type represents a specific category of slash commands and related features.
 */
public enum SlashCommandBotType {

    /**
     * Bot providing music playback and audio management commands.
     */
    MUSIC,

    /**
     * Bot providing audio transcription and speech-to-text commands.
     */
    TRANSCRIPTION,

    /**
     * Bot providing grade calculation and academic management commands.
     */
    GRADE_CALCULATOR,

    /**
     * Bot providing task management and todo list commands.
     */
    TODO,

    /**
     * Bot providing schedule and timetable management commands.
     */
    TIMETABLE,

    /**
     * Default value indicating no slash command bot type assigned.
     */
    NONE;
}
