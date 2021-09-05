package ru.l1ttleO.boyfriend;

import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Actions {

    public static final @NotNull ThreadGroup BANS_THREAD_GROUP = new ThreadGroup("Bans");
    public static final @NotNull ThreadGroup MUTES_THREAD_GROUP = new ThreadGroup("Mutes");
    public static final @NotNull HashMap<Long, HashMap<Long, Thread>> BANS = new HashMap<>();
    public static final @NotNull HashMap<Long, HashMap<Long, Thread>> MUTES = new HashMap<>();

    public static void unbanMember(final @Nullable MessageChannel channel, final @NotNull Member author, final @NotNull User unbanned, final String reason, final boolean silent) {
        final Guild guild = author.getGuild();
        guild.unban(unbanned).queue();
        final Thread existingBan = BANS.getOrDefault(guild.getIdLong(), new HashMap<>()).remove(unbanned.getIdLong());
        if (channel != null) {
            if (existingBan != null)
                existingBan.interrupt();
            if (!silent)
                channel.sendMessage("Возвращён из бана %s за `%s`".formatted(unbanned.getAsMention(), reason)).queue();
        }
        sendNotification(guild, "%s возвращает из бана %s: `%s`".formatted(author.getAsMention(), unbanned.getAsMention(), reason), silent);
    }

    public static void kickMember(final @NotNull MessageChannel channel, final @NotNull Member author, final @NotNull Member kicked, final String reason, final boolean silent) {
        final Guild guild = author.getGuild();
        String privateText = "Тебя выгнал %s за `%s`.".formatted(author.getAsMention(), reason);
        final List<Invite> invites = guild.retrieveInvites().complete();
        if (!invites.isEmpty())
            privateText += """
                \n
                Ты можешь перезайти по этой ссылке:
                https://discord.gg/%s""".formatted(invites.get(0).getCode());
        sendDirectMessage(kicked.getUser(), privateText);
        guild.kick(kicked).queue();
        if (!silent)
            channel.sendMessage("Выгнан %s за `%s`".formatted(kicked.getAsMention(), reason)).queue();
        sendNotification(guild, "%s выгоняет %s за `%s`".formatted(author.getAsMention(), kicked.getAsMention(), reason), silent);
    }

    public static void banMember(final @NotNull MessageChannel channel, final @NotNull Member author, final @NotNull User banned, final String reason, final long duration, final String durationString, final boolean silent) {
        final Guild guild = author.getGuild();
        String privateText = "Тебя забанил %s на%s за `%s`.".formatted(author.getAsMention(), durationString, reason);
        String replyText;
        try {
            guild.retrieveBan(banned).complete();
            replyText = "Теперь %s забанен на%s за `%s`".formatted(banned.getAsMention(), durationString, reason);
        } catch (final @NotNull ErrorResponseException e) { /* wasn't banned */
            replyText = "Забанен %s на%s за `%s`".formatted(banned.getAsMention(), durationString, reason);
        }
        if (duration > 0) {
            final List<Invite> invites = guild.retrieveInvites().complete();
            if (!invites.isEmpty())
                privateText += """
                               \n
                               По окончании бана ты сможешь перезайти по этой ссылке:
                               https://discord.gg/""" + invites.get(0).getCode();
        }
        sendDirectMessage(banned, privateText);
        final String banEntryReason = "(%s: на%s) %s".formatted(author.getUser().getAsTag(), durationString, reason);
        guild.ban(banned, 0, banEntryReason).complete();
        final HashMap<Long, Thread> guildBans = BANS.getOrDefault(guild.getIdLong(), new HashMap<>());
        final Thread existingBan = guildBans.get(banned.getIdLong());
        if (existingBan != null)
            existingBan.interrupt();
        if (duration > 0) {
            final DelayedRunnable runnable = new DelayedRunnable(BANS_THREAD_GROUP, (DelayedRunnable dr) -> {
                if (!banEntryReason.equals(guild.retrieveBan(banned).complete().getReason()))
                    return;
                unbanMember(null, guild.getSelfMember(), banned, "Время наказания истекло", silent);
            }, "Ban timer " + banned.getId(), duration, null);
            guildBans.put(banned.getIdLong(), runnable.thread);
            BANS.put(guild.getIdLong(), guildBans);
        }
        if (!silent)
            channel.sendMessage(replyText).queue();
        sendNotification(guild, "%s банит %s на%s за `%s`".formatted(author.getAsMention(), banned.getAsMention(), durationString, reason), silent);
    }

    public static void muteMember(final @NotNull MessageChannel channel, final @NotNull Role role, final @NotNull Member author, final @NotNull Member muted, final String reason, final long duration, final String durationString, final boolean silent) {
        final Guild guild = author.getGuild();
        final String replyText = muted.getRoles().contains(role) ?
            "Заглушен %s на%s за `%s`".formatted(muted.getAsMention(), durationString, reason) :
            "Теперь %s заглушен на%s за `%s`".formatted(muted.getAsMention(), durationString, reason);
        guild.addRoleToMember(muted, role).queue();
        final HashMap<Long, Thread> guildMutes = MUTES.getOrDefault(guild.getIdLong(), new HashMap<>());
        final Thread existingMute = guildMutes.get(muted.getIdLong());
        if (existingMute != null)
            existingMute.interrupt();
        if (duration > 0) {
            final DelayedRunnable runnable = new DelayedRunnable(MUTES_THREAD_GROUP, (DelayedRunnable thisDR) -> unmuteMember(null, role, guild.getSelfMember(), muted, "Время наказания истекло", silent),
                                                                 "Mute timer " + muted.getId(), duration * 1000L, null);
            guildMutes.put(muted.getIdLong(), runnable.thread);
            MUTES.put(guild.getIdLong(), guildMutes);
        }
        if (!silent)
            channel.sendMessage(replyText).queue();
        sendNotification(guild, "%s глушит %s на%s за `%s`".formatted(author.getAsMention(), muted.getAsMention(), durationString, reason), silent);
    }

    public static void unmuteMember(final @Nullable MessageChannel channel, final @NotNull Role role, final @NotNull Member author, final @NotNull Member unmuted, final String reason, final boolean silent) {
        final Guild guild = author.getGuild();
        if (!unmuted.getRoles().contains(role)) return;
        guild.removeRoleFromMember(unmuted, role).queue();
        final Thread existingMute = MUTES.getOrDefault(unmuted.getGuild().getIdLong(), new HashMap<>()).remove(unmuted.getIdLong());
        if (channel != null) {
            if (existingMute != null)
                existingMute.interrupt();
            if (!silent)
                channel.sendMessage("Выпущен из карцера %s за `%s`".formatted(unmuted.getAsMention(), reason)).queue();
        }
        sendNotification(guild, "%s выпускает из карцера %s: `%s`".formatted(author.getAsMention(), unmuted.getAsMention(), reason), silent);
    }

    public static void sendNotification(final @NotNull Guild guild, final @NotNull String text, final boolean silent) {
        final TextChannel channel = guild.getJDA().getTextChannelById("870929165141032971");
        if (channel != null)
            channel.sendMessage(text).queue();
        if (!silent && guild.getSystemChannel() != null)
            guild.getSystemChannel().sendMessage(text).queue();
    }

    public static void sendDirectMessage(final @NotNull User user, final @NotNull String message) {
        try {
            user.openPrivateChannel().complete().sendMessage(message).complete();
        } catch (final @NotNull ErrorResponseException e) { /* can't DM to this user */ }
    }
}
