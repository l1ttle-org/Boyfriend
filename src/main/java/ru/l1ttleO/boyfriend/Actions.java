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
import net.dv8tion.jda.api.entities.Guild.Ban;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.commands.util.Sender;
import ru.l1ttleO.boyfriend.settings.GuildSettings;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class Actions {

    public static final @NotNull ThreadGroup BANS_THREAD_GROUP = new ThreadGroup("Bans");
    public static final @NotNull ThreadGroup MUTES_THREAD_GROUP = new ThreadGroup("Mutes");
    public static final @NotNull HashMap<Long, HashMap<Long, Thread>> BANS = new HashMap<>();
    public static final @NotNull HashMap<Long, HashMap<Long, Thread>> MUTES = new HashMap<>();

    public static void kickMember(final @Nullable Sender sender, final @NotNull Member author, final @NotNull Member kicked, final String reason) {
        final Guild guild = author.getGuild();
        final BotLocale locale = GuildSettings.LOCALE.get(guild);
        if (Utils.isCreator(kicked.getIdLong())) {
            if (sender != null)
                sender.reply(tl("actions.kick.creator", locale));
            return;
        }
        String privateText = tl("actions.kick.private", locale, author.getAsMention(), reason);
        final List<Invite> invites = guild.retrieveInvites().complete();
        if (!invites.isEmpty())
            privateText += tl("actions.kick.private.invite", locale, "https://discord.gg/" + invites.get(0).getCode());
        sendDirectMessage(kicked.getUser(), privateText);
        guild.kick(kicked).queue();
        if (sender != null)
            sender.reply(tl("actions.kick.response", locale, kicked.getAsMention(), reason));
        sendNotification(guild, tl("actions.kick.audit", locale, author.getAsMention(), kicked.getAsMention(), reason), sender == null);
    }

    public static void banMember(final @Nullable Sender sender, final @NotNull Member author, final @NotNull User banned, final String reason, final long duration, final String durationString) {
        final Guild guild = author.getGuild();
        final BotLocale locale = GuildSettings.LOCALE.get(guild);
        if (Utils.isCreator(banned.getIdLong())) {
            if (sender != null)
                sender.reply(tl("actions.ban.creator", locale));
            return;
        }
        final Ban prevBan = Utils.getBan(guild, banned);
        String privateText = tl("actions.ban.private", locale, author.getAsMention(), durationString, reason);
        final String replyText = tl(prevBan == null ? "actions.ban.response" : "actions.ban.response.reapply",
            locale, banned.getAsMention(), durationString, reason);
        if (duration > 0) {
            final List<Invite> invites = guild.retrieveInvites().complete();
            if (!invites.isEmpty())
                privateText += tl("actions.ban.private.invite", locale, "https://discord.gg/" + invites.get(0).getCode());
        }
        sendDirectMessage(banned, privateText);
        final String banEntryReason = "(%s: %s%s) %s".formatted(author.getUser().getAsTag(), tl("duration.for", locale), durationString, reason);
        if (prevBan != null && !banEntryReason.equals(prevBan.getReason()))
            guild.unban(banned).queue();
        guild.ban(banned, 0, banEntryReason).complete();
        final HashMap<Long, Thread> guildBans = BANS.getOrDefault(guild.getIdLong(), new HashMap<>());
        final Thread existingBan = guildBans.get(banned.getIdLong());
        if (existingBan != null)
            existingBan.interrupt();
        if (duration > 0) {
            final DelayedRunnable runnable = new DelayedRunnable(BANS_THREAD_GROUP, (DelayedRunnable dr) -> {
                if (!banEntryReason.equals(guild.retrieveBan(banned).complete().getReason()))
                    return;
                unbanMember(null, guild.getSelfMember(), banned, tl("common.punishment_expired", locale), sender == null);
            }, "Ban timer " + banned.getId(), duration, null);
            guildBans.put(banned.getIdLong(), runnable.thread);
            BANS.put(guild.getIdLong(), guildBans);
        }
        if (sender != null)
            sender.reply(replyText);
        final String notificationText = tl("actions.ban.audit" + (prevBan != null ? ".reapply" : ""), locale,
            author.getAsMention(), banned.getAsMention(), durationString, reason);
        sendNotification(guild, notificationText, sender == null);
    }

    public static void unbanMember(final @Nullable Sender sender, final @NotNull Member author, final @NotNull User unbanned, final String reason, final boolean silent) {
        final Guild guild = author.getGuild();
        if (Utils.getBan(guild, unbanned) == null)
            return;
        final BotLocale locale = GuildSettings.LOCALE.get(guild);
        guild.unban(unbanned).queue();
        final Thread existingBan = BANS.getOrDefault(guild.getIdLong(), new HashMap<>()).remove(unbanned.getIdLong());
        if (existingBan != null)
            existingBan.interrupt();
        if (sender != null)
            sender.reply(tl("actions.unban.response", locale, unbanned.getAsMention(), reason));
        final String notificationText = tl("actions.unban.audit", locale, author.getAsMention(), unbanned.getAsMention(), reason);
        sendNotification(guild, notificationText, silent);
    }

    public static void muteMember(final @Nullable Sender sender, final @NotNull Role role, final @NotNull Member author, final @NotNull Member muted, final String reason, final long duration, final String durationString) {
        final Guild guild = author.getGuild();
        final BotLocale locale = GuildSettings.LOCALE.get(guild);
        if (Utils.isCreator(muted.getIdLong())) {
            if (sender != null)
                sender.reply(tl("actions.mute.creator", locale));
            return;
        }
        final boolean remute = muted.getRoles().contains(role);
        final String replyText = tl("actions.mute.response" + (remute ? ".reapply" : ""), locale,
            muted.getAsMention(), durationString, reason);
        guild.addRoleToMember(muted, role).queue();
        final HashMap<Long, Thread> guildMutes = MUTES.getOrDefault(guild.getIdLong(), new HashMap<>());
        final Thread existingMute = guildMutes.get(muted.getIdLong());
        if (existingMute != null)
            existingMute.interrupt();
        if (duration > 0) {
            final DelayedRunnable runnable = new DelayedRunnable(MUTES_THREAD_GROUP,
                    (DelayedRunnable thisDR) -> unmuteMember(null, role, guild.getSelfMember(), muted,
                            tl("common.punishment_expired", locale), sender == null),
                                                                 "Mute timer " + muted.getId(), duration, null);
            guildMutes.put(muted.getIdLong(), runnable.thread);
            MUTES.put(guild.getIdLong(), guildMutes);
        }
        if (sender != null)
            sender.reply(replyText);
        final String notificationText = tl("actions.mute.audit" + (remute ? ".reapply" : ""), locale,
            author.getAsMention(), muted.getAsMention(), durationString, reason);
        sendNotification(guild, notificationText, sender == null);
    }

    public static void unmuteMember(final @Nullable Sender sender, final @NotNull Role role, final @NotNull Member author, final @NotNull Member unmuted, final String reason, final boolean silent) {
        if (!unmuted.getRoles().contains(role)) return;
        final Guild guild = author.getGuild();
        final BotLocale locale = GuildSettings.LOCALE.get(guild);
        guild.removeRoleFromMember(unmuted, role).queue();
        final Thread existingMute = MUTES.getOrDefault(unmuted.getGuild().getIdLong(), new HashMap<>()).remove(unmuted.getIdLong());
        if (existingMute != null)
            existingMute.interrupt();
        if (sender != null)
            sender.reply(tl("actions.unmute.response", locale, unmuted.getAsMention(), reason));
        sendNotification(guild, tl("actions.unmute.audit", locale, author.getAsMention(), unmuted.getAsMention(), reason), silent);
    }

    public static void sendNotification(final @NotNull Guild guild, final @NotNull String text, final boolean silent) {
        // TODO use config (admin notifications channel)
        final TextChannel adminNotifChannel = guild.getJDA().getTextChannelById("870929165141032971");
        if (!Boyfriend.isRunning()) return;
        if (adminNotifChannel != null)
            adminNotifChannel.sendMessage(text).queue();
        if (!silent && guild.getSystemChannel() != null)
            guild.getSystemChannel().sendMessage(text).queue();
    }

    public static void sendDirectMessage(final @NotNull User user, final @NotNull String message) {
        try {
            user.openPrivateChannel().complete().sendMessage(message).complete();
        } catch (final ErrorResponseException e) { /* can't DM to this user */ }
    }
}
