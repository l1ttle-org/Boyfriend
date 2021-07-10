package ru.l1ttleO.boyfriend.commands;

import java.util.Objects;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Clear {
    public static String usage = "`!clear <количество>`";
    public void run(MessageReceivedEvent event, String[] args) {
        MessageChannel channel = event.getChannel();
        int requested;
        if (!Objects.requireNonNull(event.getMember()).hasPermission((GuildChannel) event.getChannel(), Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length == 0) {
            channel.sendMessage("Нету аргументов! " + usage).queue();
            return;
        }
        try {
            requested = Integer.parseInt(args[0]) + 1;
        } catch (NumberFormatException e) {
            channel.sendMessage("Неправильно указано количество! " + usage).queue();
            return;
        }
        channel.purgeMessages(channel.getHistory().retrievePast(requested).complete());
        channel.sendMessage("Успешно удалено %s сообщений".formatted(requested)).queue();
    }
}
