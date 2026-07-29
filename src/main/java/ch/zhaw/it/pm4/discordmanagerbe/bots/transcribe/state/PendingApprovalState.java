package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession.MultiUserRecordingSession;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the state when the bot is waiting for users to accept the recording.
 * Handles user acceptances and transitions to RecordingState when all users have accepted.
 */
@Component
public class PendingApprovalState extends AbstractBotState {
    private static final Logger logger = LoggerFactory.getLogger(PendingApprovalState.class);

    private final ApplicationContext applicationContext;

    /**
     * Constructs a new PendingApprovalState with the specified bot.
     * @param bot The TranscribeBot instance
     */
    public PendingApprovalState(TranscribeBot bot,
                                ApplicationContext applicationContext) {
        super(bot);
        this.applicationContext = applicationContext;
    }

    /**
     * Handles leaving the voice channel, cleans up pending requests, and transitions to DisconnectedState.
     * @param event The slash command event
     */
    @Override
    public void leaveVoiceChannel(SlashCommandInteractionEvent event) {
        AudioManager audioManager = bot.getActiveAudioManager();
        if (audioManager != null && audioManager.isConnected()) {
            logger.info("Leaving voice channel");
            unlockVoiceChannel(event);
            disconnectFromVoiceChannel(audioManager);
            cleanupChannelRequests(event);
            updateStateAndNotify(event);
        }
    }

    /**
     * Disconnects from the voice channel and cleans up audio resources.
     * @param audioManager The audio manager to disconnect
     */
    private void disconnectFromVoiceChannel(AudioManager audioManager) {
        audioManager.closeAudioConnection();
        bot.setActiveAudioManager(null);
    }

    /**
     * Cleans up channel-specific request data when leaving a channel.
     * @param event The slash command event
     */
    private void cleanupChannelRequests(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member != null) {
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null && voiceState.getChannel() != null) {
                String channelId = voiceState.getChannel().getId();
                removeChannelData(channelId);
            }
        }
    }

    /**
     * Removes all data associated with a specific channel.
     * @param channelId The ID of the channel to remove data for
     */
    private void removeChannelData(String channelId) {
        bot.getPendingAcceptances().remove(channelId);
        bot.getAcceptedUsers().remove(channelId);
    }

    /**
     * Updates the bot state and notifies the user of successful disconnection.
     * @param event The slash command event
     */
    private void updateStateAndNotify(SlashCommandInteractionEvent event) {
        bot.setState(bot.getDisconnectedState());
        event.getHook().sendMessage(BotMessages.get(MessageKey.LEAVE_SUCCESS)).queue();
        logger.info("Successfully left voice channel");
    }

    /**
     * Unlocks the voice channel, clears all recording requests, and transitions to ConnectedState.
     * @param event The slash command event
     */
    @Override
    public void unlockVoiceChannel(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        VoiceChannel voiceChannel = getAndValidateMemberVoiceChannel(member, event);
        if (voiceChannel == null) {
            return;
        }

        String channelId = voiceChannel.getId();
        bot.getPendingAcceptances().remove(channelId);
        bot.getAcceptedUsers().remove(channelId);

        unlockChannel(voiceChannel);
        event.getHook().sendMessage(BotMessages.get(MessageKey.CHANNEL_UNLOCK_WITH_CANCEL)).queue();

        bot.setState(bot.getConnectedState());
    }

    /**
     * Processes a user's acceptance of recording and updates pending/accepted user lists.
     * @param event The slash command event
     */
    @Override
    public void handleAcceptRecording(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        VoiceChannel channel = getAndValidateMemberVoiceChannel(member, event);
        if (channel == null) {
            return;
        }

        String channelId = channel.getId();

        if (!validatePendingRequest(channelId, event)) {
            return;
        }

        assert member != null;
        processAcceptance(member, channelId);
        notifyAcceptanceStatus(member, event);
    }

    /**
     * Validates that there is a pending recording request for the channel.
     * @param channelId The ID of the channel to check
     * @param event The slash command event
     * @return True if there is a pending request, false otherwise
     */
    private boolean validatePendingRequest(String channelId, SlashCommandInteractionEvent event) {
        if (!bot.getPendingAcceptances().containsKey(channelId)) {
            event.getHook().sendMessage(BotMessages.get(MessageKey.NO_PENDING_REQUEST))
                    .setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    /**
     * Processes a user's acceptance of recording by updating the tracking collections.
     * @param member The member who accepted
     * @param channelId The ID of the channel
     */
    private void processAcceptance(Member member, String channelId) {
        Set<String> accepted = bot.getAcceptedUsers().computeIfAbsent(channelId, k -> new HashSet<>());
        accepted.add(member.getId());

        Set<String> pending = bot.getPendingAcceptances().get(channelId);
        pending.remove(member.getId());
    }

    /**
     * Notifies the channel that a user has accepted the recording.
     * @param acceptingMember The member who accepted
     * @param event The slash command event
     */
    private void notifyAcceptanceStatus(Member acceptingMember, SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.RECORDING_ACCEPTED) + " " + acceptingMember.getEffectiveName()).queue();
    }

    /**
     * Attempts to start recording if all users have accepted, otherwise shows pending users.
     * @param event The slash command event
     */
    @Override
    public void startRecording(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        VoiceChannel channel = getAndValidateMemberVoiceChannel(member, event);
        if (channel == null) {
            return;
        }

        if (!isChannelLocked(channel)) {
            event.getHook().sendMessage(BotMessages.get(MessageKey.LOCK_FIRST))
                    .setEphemeral(true).queue();
            return;
        }

        String channelId = channel.getId();

        if (hasPendingApprovals(channel, channelId, event)) {
            return;
        }

        startRecordingSession(event, channel, channelId);
    }

    /**
     * Checks if the voice channel is locked by verifying permission overrides.
     * @param channel The voice channel to check
     * @return True if the channel is locked, false otherwise
     */
    private boolean isChannelLocked(VoiceChannel channel) {
        Role everyoneRole = channel.getGuild().getPublicRole();
        return channel.getPermissionOverride(everyoneRole) != null &&
                Objects.requireNonNull(channel.getPermissionOverride(everyoneRole)).getDenied().contains(Permission.VOICE_CONNECT);
    }

    /**
     * Checks if there are pending approvals for the channel and notifies the user if so.
     * @param channel The voice channel
     * @param channelId The ID of the channel
     * @param event The slash command event
     * @return True if there are pending approvals, false otherwise
     */
    private boolean hasPendingApprovals(VoiceChannel channel, String channelId, SlashCommandInteractionEvent event) {
        if (bot.getPendingAcceptances().containsKey(channelId) &&
                !bot.getPendingAcceptances().get(channelId).isEmpty()) {

            notifyPendingApprovals(channel, channelId, event);
            return true;
        }
        return false;
    }

    /**
     * Notifies the user about which members still need to approve the recording.
     * @param channel The voice channel
     * @param channelId The ID of the channel
     * @param event The slash command event
     */
    private void notifyPendingApprovals(VoiceChannel channel, String channelId, SlashCommandInteractionEvent event) {
        List<String> pendingNames = getPendingUserNames(channel, channelId);

        StringBuilder message = new StringBuilder();

        if (pendingNames.isEmpty()) {
            message.append(BotMessages.get(MessageKey.ALL_APPROVED));
        } else {
            message.append(BotMessages.get(MessageKey.WAITING_FOR_APPROVAL));
            message.append(" **").append(String.join("**, **", pendingNames)).append("**");
        }

        event.getHook().sendMessage(message.toString()).queue();
    }

    /**
     * Gets a list of names of users who have not yet approved the recording.
     * @param channel The voice channel
     * @param channelId The ID of the channel
     * @return A list of user names
     */
    private List<String> getPendingUserNames(VoiceChannel channel, String channelId) {
        return channel.getMembers().stream()
                .filter(m -> bot.getPendingAcceptances().get(channelId).contains(m.getId()))
                .map(Member::getEffectiveName)
                .collect(Collectors.toList());
    }

    /**
     * Starts the recording session if not already active.
     * @param event The slash command event
     * @param channel The voice channel
     * @param channelId The ID of the channel
     */
    private void startRecordingSession(SlashCommandInteractionEvent event, VoiceChannel channel, String channelId) {
        if (isRecordingActive(channelId, event)) {
            return;
        }

        try {
            createAndStartRecordingSession(channel, channelId);
            bot.setState(bot.getRecordingState());
            event.getHook().sendMessage(BotMessages.get(MessageKey.RECORDING_STARTED, channel)).queue();
        } catch (Exception e) {
            logger.error("Error starting recording", e);
            event.getHook().sendMessage("Fehler beim Starten der Aufnahme: " + e.getMessage())
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Checks if there is already an active recording for the channel.
     * @param channelId The ID of the channel to check
     * @param event The slash command event
     * @return True if there is an active recording, false otherwise
     */
    private boolean isRecordingActive(String channelId, SlashCommandInteractionEvent event) {
        if (bot.getActiveSessions().containsKey(channelId)) {
            event.getHook().sendMessage(BotMessages.get(MessageKey.RECORDING_ACTIVE))
                    .setEphemeral(true).queue();
            return true;
        }
        return false;
    }

    /**
     * Creates and starts a new recording session for the channel.
     * @param channel The voice channel
     * @param channelId The ID of the channel
     */
    private void createAndStartRecordingSession(VoiceChannel channel, String channelId) {
        MultiUserRecordingSession session = applicationContext.getBean(MultiUserRecordingSession.class);
        session.setAudioManager(bot.getActiveAudioManager());
        session.setChannel(channel);
        bot.getActiveSessions().put(channelId, session);

        bot.getActiveAudioManager().setReceivingHandler(session);
    }
}
