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
import ru.l1ttleO.boyfriend.Utils;

public class Mute extends Command {
	
    public Mute() {
		super("mute", "Глушит участника", "mute <@упоминание или ID> [<продолжительность>] <причина>");
	}

    public static final String[] ROLE_NAMES = {"заключённый","заключённые","muted"};
    public void run(final MessageReceivedEvent event, final String[] args) {
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member muted;
        assert author != null;
        if (!author.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if ((muted = getMember(args[1], event.getGuild(), channel)) == null) return;
        if (!author.canInteract(muted)) {
            channel.sendMessage("У тебя недостаточно прав для мута этого пользователя!").queue();
            return;
        }
        List<Role> roleList = null;
        for (String name : ROLE_NAMES) {
        	roleList = guild.getRolesByName(name, true);
        	if (!roleList.isEmpty()) break;
        }
        if (roleList.isEmpty()) {
            channel.sendMessage("Не найдена роль мута!").queue();
            return;
        }
        final Role role = roleList.get(0);
        int duration = Utils.getDurationMultiplied(args[2]);
        int startIndex = 2;
        String durationString = "всегда";
        if (duration > 0) {
        	durationString = " " + Utils.getDurationText(duration, true);
            startIndex++;
        } else duration = 0; // extra check
        if (startIndex >= args.length) {
        	usageError(channel, "Требуется указать причину!");
            return;
        }
        final String reason = StringUtils.join(args, ' ', startIndex, args.length);
        Actions.muteMember(channel, role, author, muted, reason, duration, durationString);
    }
}
