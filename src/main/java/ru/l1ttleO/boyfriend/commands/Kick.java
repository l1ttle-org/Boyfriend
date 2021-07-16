package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import ru.l1ttleO.boyfriend.Actions;

public class Kick extends Command {
	
    public Kick() {
		super("kick", "Выгоняет участника", "kick <@упоминание или ID> <причина>");
	}

    public void run(final MessageReceivedEvent event, final String[] args) {
        final Member author = event.getMember();
        final Member kicked;
        final MessageChannel channel = event.getChannel();
        assert author != null;
        if (!author.hasPermission(Permission.KICK_MEMBERS)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length < 3) {
            channel.sendMessage("Требуется указать причину!").queue();
            return;
        }
        if ((kicked = getMember(args[1], event.getGuild(), channel)) == null) return;
        if (!author.canInteract(kicked)) {
            channel.sendMessage("У тебя недостаточно прав для кика этого пользователя!").queue();
            return;
        }
        final String reason = StringUtils.join(args, ' ', 2, args.length);
        Actions.kickMember(channel, author, kicked, reason);
    }
}
