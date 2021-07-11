package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import ru.l1ttleO.boyfriend.Boyfriend;

public class Kick {
    public static final String usage = "`!kick <@упоминание или ID> <причина>`";
    public void run(MessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        Member author = event.getMember();
        Member kicked;
        MessageChannel channel = event.getChannel();
        assert author != null;
        if (!author.hasPermission(Permission.KICK_MEMBERS)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length == 0) {
            channel.sendMessage("Нету аргументов! " + usage).queue();
            return;
        }
        try {
            String id = args[0].replaceAll("[^0-9]", "").replace("!", "").replace(">", "");
            kicked = guild.retrieveMemberById(id).complete();
        } catch (NumberFormatException e) {
            channel.sendMessage("Неправильно указан пользователь! " + usage).queue();
            return;
        }
        if (kicked == null) {
            channel.sendMessage("Указан недействительный пользователь!").queue();
            return;
        }
        if (!author.canInteract(kicked)) {
            channel.sendMessage("У тебя недостаточно прав для бана этого пользователя!").queue();
            return;
        }
        String reason = StringUtils.join(args, ' ', 1, args.length);
        Boyfriend.memberActions.kickMember(channel, author, kicked, reason);
    }
}
