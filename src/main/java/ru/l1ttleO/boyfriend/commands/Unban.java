package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import ru.l1ttleO.boyfriend.Boyfriend;

public class Unban {
    public static String usage = "`!unban <@упоминание или ID> <причина>`";
    public void run(MessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        JDA jda = guild.getJDA();
        Member author = event.getMember();
        MessageChannel channel = event.getChannel();
        User unbanned;
        assert author != null;
        if (!author.hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length == 0) {
            channel.sendMessage("Нету аргументов! " + usage).queue();
            return;
        }
        try {
            String id = args[0].replaceAll("[^0-9]", "").replace("!", "").replace(">", "");
            unbanned = jda.retrieveUserById(id).complete();
        } catch (NumberFormatException e) {
            channel.sendMessage("Неправильно указан пользователь! " + usage).queue();
            return;
        }
        if (unbanned == null) {
            channel.sendMessage("Указан недействительный пользователь!").queue();
            return;
        }
        String reason = StringUtils.join(args, ' ', 1, args.length);
        if (reason == null || reason.equals("")) {
            channel.sendMessage("Требуется указать причину!").queue();
            return;
        }
        Boyfriend.memberActions.unbanMember(channel, author, unbanned, reason);
    }
}
