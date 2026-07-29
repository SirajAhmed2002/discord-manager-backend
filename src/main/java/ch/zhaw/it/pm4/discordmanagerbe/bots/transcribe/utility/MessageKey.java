package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility;

/**
 * Enumeration of all message keys used by the TranscribeBot.
 */
public enum MessageKey {
    // general messages
    COMMAND_NOT_AVAILABLE,
    PERMISSION_DENIED,

    // Voice Channel messages
    JOIN_SUCCESS,
    ALREADY_CONNECTED,
    LEAVE_SUCCESS,
    NOT_IN_VOICE_CHANNEL,
    CONNECTION_ERROR,

    // Channel Lock messages
    CHANNEL_LOCKED,
    CHANNEL_LOCKED_APPROVAL_NEEDED,
    CHANNEL_UNLOCKED,
    ALREADY_LOCKED,
    CHANNEL_UNLOCK_WITH_CANCEL,
    JOIN_FIRST,

    // recording messages
    RECORDING_PERMISSION_REQUIRED,
    RECORDING_ACCEPTED,
    RECORDING_STARTED,
    RECORDING_STOPPED,
    RECORDING_ACTIVE,
    NO_ACTIVE_RECORDING,
    LOCK_FIRST,
    NO_PENDING_REQUEST,
    ALL_APPROVED,
    WAITING_FOR_APPROVAL,

    // recording messages
    HELP_MESSAGE
}
