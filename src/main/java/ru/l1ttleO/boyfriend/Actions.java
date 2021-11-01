/*
    This file is part of Boyfriend
    Copyright (C) 2021  l1ttleO

    Boyfriend is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Boyfriend is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Boyfriend.  If not, see <https://www.gnu.org/licenses/>.
*/

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

import static ru.l1ttleO.boyfriend.I18n.tl;

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
                channel.sendMessage(tl("unban.response", unbanned.getAsMention(), reason)).queue();
        }
        sendNotification(guild, tl("audit.member_unbanned", author.getAsMention(), unbanned.getAsMention(), reason), silent);
    }

    public static void kickMember(final @NotNull MessageChannel channel, final @NotNull Member author, final @NotNull Member kicked, final String reason, final boolean silent) {
        if (kicked.getId().equals("504343489664909322")) {
            channel.sendMessage(tl("kick.creator")).queue();
            return;
        }
        final Guild guild = author.getGuild();
        String privateText = tl("private.kicked", author.getAsMention(), reason);
        final List<Invite> invites = guild.retrieveInvites().complete();
        if (!invites.isEmpty())
            privateText += tl("private.invite", invites.get(0).getCode());
        sendDirectMessage(kicked.getUser(), privateText);
        guild.kick(kicked).queue();
        if (!silent)
            channel.sendMessage(tl("kick.response", kicked.getAsMention(), reason)).queue();
        sendNotification(guild, tl("audit.member_kicked", author.getAsMention(), kicked.getAsMention(), reason), silent);
    }

    public static void banMember(final @NotNull MessageChannel channel, final @NotNull Member author, final @NotNull User banned, final String reason, final long duration, final String durationString, final boolean silent) {
        if (banned.getId().equals("504343489664909322")) {
            channel.sendMessage(tl("ban.creator")).queue();
            return;
        }
        final Guild guild = author.getGuild();
        String privateText = tl("private.banned", author.getAsMention(), durationString, reason);
        String replyText;
        try {
            guild.retrieveBan(banned).complete();
            replyText = tl("ban.response_reban", banned.getAsMention(), durationString, reason);
        } catch (final @NotNull ErrorResponseException e) { /* wasn't banned */
            replyText = tl("ban.response", banned.getAsMention(), durationString, reason);
        }
        if (duration > 0) {
            final List<Invite> invites = guild.retrieveInvites().complete();
            if (!invites.isEmpty())
                privateText += tl("private.invite_tempban", invites.get(0).getCode());
        }
        sendDirectMessage(banned, privateText);
        final String banEntryReason = "(%s: %s%s) %s".formatted(author.getUser().getAsTag(), tl("duration.for"), durationString, reason);
        guild.ban(banned, 0, banEntryReason).complete();
        final HashMap<Long, Thread> guildBans = BANS.getOrDefault(guild.getIdLong(), new HashMap<>());
        final Thread existingBan = guildBans.get(banned.getIdLong());
        if (existingBan != null)
            existingBan.interrupt();
        if (duration > 0) {
            final DelayedRunnable runnable = new DelayedRunnable(BANS_THREAD_GROUP, (DelayedRunnable dr) -> {
                if (!banEntryReason.equals(guild.retrieveBan(banned).complete().getReason()))
                    return;
                unbanMember(null, guild.getSelfMember(), banned, tl("common.punishment_expired"), silent);
            }, "Ban timer " + banned.getId(), duration, null);
            guildBans.put(banned.getIdLong(), runnable.thread);
            BANS.put(guild.getIdLong(), guildBans);
        }
        if (!silent)
            channel.sendMessage(replyText).queue();
        sendNotification(guild, tl("audit.member_banned", author.getAsMention(), banned.getAsMention(), durationString, reason), silent);
    }

    public static void muteMember(final @NotNull MessageChannel channel, final @NotNull Role role, final @NotNull Member author, final @NotNull Member muted, final String reason, final long duration, final String durationString, final boolean silent) {
        if (muted.getId().equals("504343489664909322")) {
            channel.sendMessage(tl("mute.creator")).queue();
            return;
        }
        final Guild guild = author.getGuild();
        final boolean remute = muted.getRoles().contains(role);
        final String replyText = (remute ? tl("mute.response_remute") :
            tl("mute.response")).formatted(muted.getAsMention(), durationString, reason);
        final String notificationText = (remute ? tl("audit.member_remuted") :
            tl("audit.member_muted")).formatted(author.getAsMention(), muted.getAsMention(), durationString, reason);
        guild.addRoleToMember(muted, role).queue();
        final HashMap<Long, Thread> guildMutes = MUTES.getOrDefault(guild.getIdLong(), new HashMap<>());
        final Thread existingMute = guildMutes.get(muted.getIdLong());
        if (existingMute != null)
            existingMute.interrupt();
        if (duration > 0) {
            final DelayedRunnable runnable = new DelayedRunnable(MUTES_THREAD_GROUP,
                    (DelayedRunnable thisDR) -> unmuteMember(null, role, guild.getSelfMember(), muted,
                            tl("common.punishment_expired"), silent),
                                                                 "Mute timer " + muted.getId(), duration, null);
            guildMutes.put(muted.getIdLong(), runnable.thread);
            MUTES.put(guild.getIdLong(), guildMutes);
        }
        if (!silent)
            channel.sendMessage(replyText).queue();
        sendNotification(guild, notificationText, silent);
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
                channel.sendMessage(tl("unmute.response", unmuted.getAsMention(), reason)).queue();
        }
        sendNotification(guild, tl("audit.member_unmuted", author.getAsMention(), unmuted.getAsMention(), reason), silent);
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
