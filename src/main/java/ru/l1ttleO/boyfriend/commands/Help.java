package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help {
    public void run(final MessageReceivedEvent event) {
        final MessageChannel channel = event.getChannel();
        channel.sendMessage("""
                            Справка по командам:
                            `!ban` - Банит участника. Использование: %s;
                            `!clear` - Удаляет указанное количество сообщений в канале. Использование: %s;
                            `!help` - Показывает эту справку;
                            `!kick` - Выгоняет пользователя. Использование: %s;
                            `!ping` - Измеряет время обработки REST-запроса;
                            `!unban` - Возвращает пользователя из бана. Использование: %s"""
                           .formatted(Ban.usage, Clear.usage, Kick.usage, Unban.usage)).queue();
    }
}

