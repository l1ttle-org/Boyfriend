package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.apache.commons.lang3.StringUtils;

import ru.l1ttleO.boyfriend.Actions;

public class Unban extends Command {
	
    public Unban() {
		super("unban", "Возвращает пользователя из бана", "unban <@упоминание или ID> <причина>");
	}

    public void run(final MessageReceivedEvent event, final String[] args) {
        final Guild guild = event.getGuild();
        final JDA jda = guild.getJDA();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final User unbanned;
        assert author != null;
        if (!author.hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length < 3) {
            channel.sendMessage("Требуется указать причину!").queue();
            return;
        }
        if ((unbanned = getUser(args[1], jda, channel)) == null) return;
        try {
            guild.retrieveBan(unbanned).complete();
        } catch (final ErrorResponseException e) {
            channel.sendMessage("Пользователь не забанен!").queue();
            return;
        }
        final String reason = StringUtils.join(args, ' ', 2, args.length);
        Actions.unbanMember(channel, author, unbanned, reason);
    }
}
