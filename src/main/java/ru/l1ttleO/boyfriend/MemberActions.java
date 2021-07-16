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

import java.util.Objects;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class MemberActions {

    public void banMember(final MessageChannel channel, final Member author, final User banned, final String reason, final int duration, final String durationString) {
        final Guild guild = author.getGuild();
        final TextChannel logChannel = guild.getSystemChannel();
        banned.openPrivateChannel().complete().sendMessage("""
                                                           Тебя забанил %s на%s за `%s`.

                                                           Если твой бан временный, по его окончанию ты сможешь перезайти по этой ссылке:
                                                           https://discord.gg/7AErwavhvx""".formatted(author.getAsMention(), durationString, reason)).complete();
        guild.ban(banned, 0, "(%s: на%s) %s".formatted(author.getUser().getName() + "#" + author.getUser().getDiscriminator(), durationString, reason)).complete();
        channel.sendMessage("Забанен %s на%s за `%s`".formatted(banned.getAsMention(), durationString, reason)).queue();
        assert logChannel != null;
        logChannel.sendMessage("%s банит %s на%s за `%s`".formatted(author.getAsMention(), banned.getAsMention(), durationString, reason)).queue();
        final String oldBanEntry = guild.retrieveBan(banned).complete().getReason();
        if (duration != 0) {
            final Runnable runnable = () -> {
                try {
                    Thread.sleep(duration * 1000L);
                    assert oldBanEntry != null;
                    if (!oldBanEntry.equals(guild.retrieveBan(banned).complete().getReason())) {
                        return;
                    }
                    guild.unban(banned).queue();
                    logChannel.sendMessage("%s возвращает из бана %s: Время наказания истекло".formatted(channel.getJDA().getSelfUser().getAsMention(), banned.getAsMention())).queue();
                } catch (final InterruptedException e) {
                    logChannel.sendMessage("[!] Прерван таймер разбана для %s".formatted(banned.getAsMention())).queue();
                }
            };
            final Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public void unbanMember(final MessageChannel channel, final Member author, final User unbanned, final String reason) {
        author.getGuild().unban(unbanned).queue();
        channel.sendMessage("Возвращён из бана %s за `%s`".formatted(unbanned.getAsMention(), reason)).queue();
        Objects.requireNonNull(author.getGuild().getSystemChannel()).sendMessage("%s возвращает из бана %s: `%s`".formatted(author.getAsMention(), unbanned.getAsMention(), reason)).queue();
    }

    public void kickMember(final MessageChannel channel, final Member author, final Member kicked, final String reason) {
        kicked.getUser().openPrivateChannel().complete().sendMessage("""
                                                                     Тебя выгнал %s за `%s`.

                                                                     Ты можешь перезайти по этой ссылке:
                                                                     https://discord.gg/7AErwavhvx""".formatted(author.getAsMention(), reason)).complete();
        author.getGuild().kick(kicked).queue();
        channel.sendMessage("Выгнан %s за `%s`".formatted(kicked.getAsMention(), reason)).queue();
        Objects.requireNonNull(author.getGuild().getSystemChannel()).sendMessage("%s выгоняет %s за `%s`".formatted(author.getAsMention(), kicked.getAsMention(), reason)).queue();
    }

    public void muteMember(final MessageChannel channel, final Role role, final Member author, final Member muted, final String reason, final int duration, final String durationString) {
        final Guild guild = author.getGuild();
        final TextChannel logChannel = guild.getSystemChannel();
        guild.addRoleToMember(muted, role).queue();
        channel.sendMessage("Заглушен %s на%s за `%s`".formatted(muted.getAsMention(), durationString, reason)).queue();
        assert logChannel != null;
        logChannel.sendMessage("%s глушит %s на%s за `%s`".formatted(author.getAsMention(), muted.getAsMention(), durationString, reason)).queue();
        if (duration != 0) {
            final Runnable runnable = () -> {
                try {
                    Thread.sleep(duration * 1000L);
                    if (muted.getRoles().contains(role)) {
                        guild.removeRoleFromMember(muted, role).queue();
                        logChannel.sendMessage("%s возвращает из карцера %s: Время наказания истекло".formatted(channel.getJDA().getSelfUser().getAsMention(), muted.getAsMention())).queue();
                    }
                } catch (final InterruptedException e) {
                    logChannel.sendMessage("[!] Прерван таймер размута для %s".formatted(muted.getAsMention())).queue();
                }
            };
            final Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public void unmuteMember(final MessageChannel channel, final Role role, final Member author, final Member unmuted, final String reason) {
        author.getGuild().removeRoleFromMember(unmuted, role).queue();
        channel.sendMessage("Возвращён из карцера %s за `%s`".formatted(unmuted.getAsMention(), reason)).queue();
        Objects.requireNonNull(author.getGuild().getSystemChannel()).sendMessage("%s возвращает из карцера %s: `%s`".formatted(author.getAsMention(), unmuted.getAsMention(), reason)).queue();
    }
}
