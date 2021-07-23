package ru.l1ttleO.boyfriend;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class Actions {

    public static final HashMap<Long, HashMap<Long, Thread>> BANS = new HashMap<>();

    public static void banMember(final MessageChannel channel, final Member author, final User banned, final String reason, final int duration, final String durationString) {
        final Guild guild = author.getGuild();
        final TextChannel logChannel = guild.getSystemChannel();
        String DMtext = "Тебя забанил %s на%s за `%s`.".formatted(author.getAsMention(), durationString, reason);
        String replyText;
        try {
            guild.retrieveBan(banned).complete();
            replyText = "Теперь %s забанен на%s за `%s`".formatted(banned.getAsMention(), durationString, reason);
        } catch (final ErrorResponseException e) { /* wasn't banned */
            replyText = "Забанен %s на%s за `%s`".formatted(banned.getAsMention(), durationString, reason);
        }
        guild.ban(banned, 0, "(%s: на%s) %s".formatted(author.getUser().getName() + "#" + author.getUser().getDiscriminator(), durationString, reason)).complete();
        final String banEntryReason = guild.retrieveBan(banned).complete().getReason();
        final HashMap<Long, Thread> guildBans = BANS.getOrDefault(guild.getIdLong(), new HashMap<>());
        final Thread existingBan = guildBans.get(banned.getIdLong());
        if (existingBan != null)
            existingBan.interrupt();
        if (duration > 0) {
            final List<Invite> invites = guild.retrieveInvites().complete();
            if (invites.size() > 0)
                DMtext += """
                    \n
                    По окончании бана ты сможешь перезайти по этой ссылке:
                    https://discord.gg/%s""".formatted(invites.get(0).getCode());
            final Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(duration * 1000L);
                    if (banEntryReason == null)
                        throw new IllegalStateException("Причина бана является null");
                    if (!banEntryReason.equals(guild.retrieveBan(banned).complete().getReason()))
                        return;
                    unbanMember(null, guild.getSelfMember(), banned, "Время наказания истекло");
                } catch (final InterruptedException e) {
                    if (logChannel != null)
                        logChannel.sendMessage("[!] Прерван таймер разбана для %s".formatted(banned.getAsMention())).queue();
                }
            }, "Ban timer " + banned.getId());
            guildBans.put(banned.getIdLong(), thread);
            BANS.put(guild.getIdLong(), guildBans);
            thread.start();
        }
        try {
            banned.openPrivateChannel().complete().sendMessage(DMtext).complete();
        } catch (final ErrorResponseException e) { /* can't DM to this user */ }
        channel.sendMessage(replyText).queue();
        if (logChannel != null)
            logChannel.sendMessage("%s банит %s на%s за `%s`".formatted(author.getAsMention(), banned.getAsMention(), durationString, reason)).queue();
    }

    public static void unbanMember(final MessageChannel channel, final Member author, final User unbanned, final String reason) {
        author.getGuild().unban(unbanned).queue();
        final Thread existingBan = BANS.getOrDefault(author.getGuild().getIdLong(), new HashMap<>()).remove(unbanned.getIdLong());
        if (channel != null) {
            if (existingBan != null) existingBan.interrupt();
            channel.sendMessage("Возвращён из бана %s за `%s`".formatted(unbanned.getAsMention(), reason)).queue();
        }
        Objects.requireNonNull(author.getGuild().getSystemChannel()).sendMessage("%s возвращает из бана %s: `%s`".formatted(author.getAsMention(), unbanned.getAsMention(), reason)).queue();
    }

    public static void kickMember(final MessageChannel channel, final Member author, final Member kicked, final String reason) {
        String DMtext = "Тебя выгнал %s за `%s`.".formatted(author.getAsMention(), reason);
        final List<Invite> invites = author.getGuild().retrieveInvites().complete();
        if (invites.size() > 0)
            DMtext += """
                \n
                Ты можешь перезайти по этой ссылке:
                https://discord.gg/%s""".formatted(invites.get(0).getCode());
        try {
            kicked.getUser().openPrivateChannel().complete().sendMessage(DMtext).complete();
        } catch (final ErrorResponseException e) { /* can't DM to this user */ }
        author.getGuild().kick(kicked).queue();
        channel.sendMessage("Выгнан %s за `%s`".formatted(kicked.getAsMention(), reason)).queue();
        Objects.requireNonNull(author.getGuild().getSystemChannel()).sendMessage("%s выгоняет %s за `%s`".formatted(author.getAsMention(), kicked.getAsMention(), reason)).queue();
    }

    public static final HashMap<Long, HashMap<Long, Thread>> MUTES = new HashMap<>();

    public static void muteMember(final MessageChannel channel, final Role role, final Member author, final Member muted, final String reason, final int duration, final String durationString) {
        final Guild guild = author.getGuild();
        final TextChannel logChannel = guild.getSystemChannel();
        final String replyText = muted.getRoles().contains(role) ?
            "Заглушен %s на%s за `%s`".formatted(muted.getAsMention(), durationString, reason) :
            "Теперь %s заглушен на%s за `%s`".formatted(muted.getAsMention(), durationString, reason);
        guild.addRoleToMember(muted, role).queue();
        final HashMap<Long, Thread> guildMutes = MUTES.getOrDefault(guild.getIdLong(), new HashMap<>());
        final Thread existingMute = guildMutes.get(muted.getIdLong());
        if (existingMute != null)
            existingMute.interrupt();
        if (duration > 0) {
            final Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(duration * 1000L);
                    final Member unmuted = muted.getGuild().retrieveMemberById(muted.getIdLong()).complete();
                    if (unmuted != null)
                        unmuteMember(null, role, guild.getSelfMember(), unmuted, "Время наказания истекло");
                } catch (final InterruptedException e) {
                    if (logChannel != null)
                        logChannel.sendMessage("[!] Прерван таймер размута для %s".formatted(muted.getAsMention())).queue();
                } catch (final ErrorResponseException e) { /* Unknown member */ }
            }, "Mute timer " + muted.getId());
            guildMutes.put(muted.getIdLong(), thread);
            MUTES.put(guild.getIdLong(), guildMutes);
            thread.start();
        }
        channel.sendMessage(replyText).queue();
        if (logChannel != null)
            logChannel.sendMessage("%s глушит %s на%s за `%s`".formatted(author.getAsMention(), muted.getAsMention(), durationString, reason)).queue();
    }

    public static void unmuteMember(final MessageChannel channel, final Role role, final Member author, final Member unmuted, final String reason) {
        if (!unmuted.getRoles().contains(role)) return;
        author.getGuild().removeRoleFromMember(unmuted, role).queue();
        final Thread existingMute = MUTES.getOrDefault(unmuted.getGuild().getIdLong(), new HashMap<>()).remove(unmuted.getIdLong());
        if (channel != null) {
            if (existingMute != null)
                existingMute.interrupt();
            channel.sendMessage("Возвращён из карцера %s за `%s`".formatted(unmuted.getAsMention(), reason)).queue();
        }
        Objects.requireNonNull(author.getGuild().getSystemChannel()).sendMessage("%s возвращает из карцера %s: `%s`".formatted(author.getAsMention(), unmuted.getAsMention(), reason)).queue();
    }
}
