package ru.l1ttleO.boyfriend.commands;

import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import ru.l1ttleO.boyfriend.Actions;

public class Unmute extends Command {
	
    public Unmute() {
		super("unmute", "Возвращает участника из мута", "unmute <@упоминание или ID> <причина>");
	}

    public void run(final MessageReceivedEvent event, final String[] args) {
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member unmuted;
        assert author != null;
        if (!author.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length < 3) {
            channel.sendMessage("Требуется указать причину!").queue();
            return;
        }
        if ((unmuted = getMember(args[1], event.getGuild(), channel)) == null) return;
        List<Role> roleList = null;
        for (String name : Mute.ROLE_NAMES) {
        	roleList = guild.getRolesByName(name, true);
        	if (!roleList.isEmpty()) break;
        }
        if (roleList.isEmpty()) {
            channel.sendMessage("Не найдена роль мута!").queue();
            return;
        }
        final Role role = roleList.get(0);
        if (!unmuted.getRoles().contains(role)) {
            channel.sendMessage("Участник не заглушен!").queue();
            return;
        }
        final String reason = StringUtils.join(args, ' ', 2, args.length);
        Actions.unmuteMember(channel, role, author, unmuted, reason);
    }
}
