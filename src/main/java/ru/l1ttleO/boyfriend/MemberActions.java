package ru.l1ttleO.boyfriend;

import java.util.Objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class MemberActions {

    public void banMember(JDA jda, MessageChannel channel, Member author, User banned, String reason, int duration, String durationString) {
        Guild guild = author.getGuild();
        TextChannel logChannel = guild.getSystemChannel();
        banned.openPrivateChannel().complete().sendMessage("""
                                                           Тебя забанил %s на%s за `%s`.

                                                           Если твой бан временный, по его окончанию ты сможешь перезайти по этой ссылке:
                                                           https://discord.gg/7AErwavhvx""".formatted(author.getAsMention(), durationString, reason)).complete();
        guild.ban(banned, 0, "(%s: на%s) %s".formatted((author.getUser().getName() + "#" + author.getUser().getDiscriminator()), durationString, reason)).complete();
        channel.sendMessage("Забанен %s на%s за `%s`".formatted(banned.getAsMention(), durationString, reason)).queue();
        assert logChannel != null;
        logChannel.sendMessage("%s банит %s на%s за `%s`".formatted(author.getAsMention(), banned.getAsMention(), durationString, reason)).queue();
        String oldBanEntry = guild.retrieveBan(banned).complete().getReason();
        if (duration != 0) {
            Runnable runnable = () -> {
                try {
                    Thread.sleep(duration * 1000L);
                    assert oldBanEntry != null;
                    if (!oldBanEntry.equals(guild.retrieveBan(banned).complete().getReason())) {
                        return;
                    }
                    guild.unban(banned).queue();
                    logChannel.sendMessage("%s возвращает из бана %s: Время наказания истекло".formatted(jda.getSelfUser().getAsMention(), banned.getAsMention())).queue();
                } catch (InterruptedException e) {
                    logChannel.sendMessage("[!] Прерван таймер разбана для %s".formatted(banned.getAsMention())).queue();
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public void unbanMember(MessageChannel channel, Member author, User unbanned, String reason) {
        author.getGuild().unban(unbanned).queue();
        channel.sendMessage("Возвращён из бана %s за `%s`".formatted(unbanned.getAsMention(), reason)).queue();
        Objects.requireNonNull(author.getGuild().getSystemChannel()).sendMessage("%s возвращает из бана %s: `%s`".formatted(author.getAsMention(), unbanned.getAsMention(), reason)).queue();
    }

    public void kickMember(MessageChannel channel, Member author, Member kicked, String reason) {
        kicked.getUser().openPrivateChannel().complete().sendMessage("""
                                                           Тебя выгнал %s за `%s`.

                                                           Ты можешь перезайти по этой ссылке:
                                                           https://discord.gg/7AErwavhvx""".formatted(author.getAsMention(), reason)).complete();
        author.getGuild().kick(kicked).queue();
        channel.sendMessage("Выгнан %s за `%s`".formatted(kicked.getAsMention(), reason)).queue();
        Objects.requireNonNull(author.getGuild().getSystemChannel()).sendMessage("%s выгоняет %s за `%s`".formatted(author.getAsMention(), kicked.getAsMention(), reason)).queue();
    }
}
