package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract implementation of the BotState interface that provides default behavior
 * and common utility methods for concrete state implementations.
 */
public abstract class AbstractBotState implements BotState {

    /** the bot instance */
    protected final TranscribeBot bot;

    /**
     * Constructs a new AbstractBotState with the specified bot.
     * @param bot The TranscribeBot instance
     */
    public AbstractBotState(TranscribeBot bot) {
        this.bot = bot;
    }

    /**
     * Default implementation for joining a voice channel.
     * @param event The slash command event
     */
    @Override
    public void joinVoiceChannel(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.COMMAND_NOT_AVAILABLE)).queue();
    }

    /**
     * Default implementation for leaving a voice channel.
     * @param event The slash command event
     */
    @Override
    public void leaveVoiceChannel(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.COMMAND_NOT_AVAILABLE)).queue();
    }

    /**
     * Default implementation for locking a voice channel.
     * @param event The slash command event
     */
    @Override
    public void lockVoiceChannel(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.COMMAND_NOT_AVAILABLE)).queue();
    }

    /**
     * Default implementation for unlocking a voice channel.
     * @param event The slash command event
     */
    @Override
    public void unlockVoiceChannel(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.COMMAND_NOT_AVAILABLE)).queue();
    }

    /**
     * Default implementation for handling recording acceptance.
     * @param event The slash command event
     */
    @Override
    public void handleAcceptRecording(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.COMMAND_NOT_AVAILABLE)).queue();
    }

    /**
     * Default implementation for starting a recording.
     * @param event The slash command event
     */
    @Override
    public void startRecording(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.COMMAND_NOT_AVAILABLE)).queue();
    }

    /**
     * Default implementation for stopping a recording.
     * @param event The slash command event
     */
    @Override
    public void stopRecording(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.COMMAND_NOT_AVAILABLE)).queue();
    }

    /**
     * Locks a voice channel by denying CONNECT permission for @everyone role.
     * @param channel The voice channel to lock
     */
    protected void lockChannel(VoiceChannel channel) {
        Role everyoneRole = channel.getGuild().getPublicRole();
        VoiceChannelManager manager = channel.getManager();

        manager.putPermissionOverride(everyoneRole, null, EnumSet.of(Permission.VOICE_CONNECT)).queue();
    }

    /**
     * Unlocks a voice channel by removing the permission override for @everyone role.
     * This restores the default permissions.
     * @param channel The voice channel to unlock
     */
    protected void unlockChannel(VoiceChannel channel) {
        Role everyoneRole = channel.getGuild().getPublicRole();
        Objects.requireNonNull(channel.getPermissionOverride(everyoneRole))
                .delete()
                .queue();
    }

    /**
     * Requests recording permission from all users in the voice channel.
     * @param event The slash command event
     * @param channel The voice channel where recording will take place
     */
    protected void requestRecordingPermission(SlashCommandInteractionEvent event, VoiceChannel channel) {
        String channelId = channel.getId();
        clearPreviousRequests(channelId);
        collectChannelUsers(channel, event.getJDA().getSelfUser().getId());

        String userList = buildUserList(channel, event.getJDA().getSelfUser().getId());
        event.getHook().sendMessage(BotMessages.get(MessageKey.CHANNEL_LOCKED_APPROVAL_NEEDED, userList)).queue();
    }

    /**
     * Clears any previous recording permission requests for the specified channel.
     * Removes the channel from pending acceptances and accepted users, and initializes
     * a new set of pending users for the channel.
     *
     * @param channelId The ID of the voice channel to clear requests for
     */
    private void clearPreviousRequests(String channelId) {
        bot.getPendingAcceptances().remove(channelId);
        bot.getAcceptedUsers().remove(channelId);

        Set<String> pendingUsers = new HashSet<>();
        bot.getPendingAcceptances().put(channelId, pendingUsers);
    }

    /**
     * Builds a comma-separated list of user display names in the specified voice channel,
     * excluding the bot itself.
     *
     * @param channel The voice channel to retrieve user names from
     * @param botId The ID of the bot to exclude from the list
     */
    private void collectChannelUsers(VoiceChannel channel, String botId) {
        Set<String> pendingUsers = bot.getPendingAcceptances().get(channel.getId());

        channel.getMembers().stream()
                .map(Member::getId)
                .filter(id -> !id.equals(botId))
                .forEach(pendingUsers::add);
    }

    /**
     * Builds a comma-separated list of user display names in the specified voice channel,
     * excluding the bot itself.
     *
     * @param channel The voice channel to retrieve usernames from
     * @param botId The ID of the bot to exclude from the list
     * @return A comma-separated string of user display names
     */
    private String buildUserList(VoiceChannel channel, String botId) {
        return channel.getMembers().stream()
                .filter(member -> !member.getId().equals(botId))
                .map(Member::getEffectiveName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Validates if a member is currently in a voice channel.
     * @param member The member to check
     * @param event The slash command event
     * @return True if member is in a voice channel, false otherwise
     */
    protected boolean validateMemberInVoiceChannel(Member member, SlashCommandInteractionEvent event) {
        boolean isValid = Optional.ofNullable(member)
                .map(Member::getVoiceState)
                .filter(GuildVoiceState::inAudioChannel)
                .isPresent();

        if (!isValid) {
            event.getHook().sendMessage(BotMessages.get(MessageKey.NOT_IN_VOICE_CHANNEL))
                    .setEphemeral(true).queue();
        }
        return isValid;
    }

    /**
     * Gets the voice channel a member is connected to.
     * Validates if the member is in a voice channel first and sends an error message if not.
     *
     * @param member The member to get the voice channel for
     * @param event The slash command event, used for sending error replies
     * @return The voice channel the member is in, or null if the member is not in a voice channel
     */
    protected VoiceChannel getAndValidateMemberVoiceChannel(Member member, SlashCommandInteractionEvent event) {
        if (!validateMemberInVoiceChannel(member, event)) {
            return null;
        }

        return Objects.requireNonNull(Objects.requireNonNull(member.getVoiceState()).getChannel()).asVoiceChannel();
    }
}