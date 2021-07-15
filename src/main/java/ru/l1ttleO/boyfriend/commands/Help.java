package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help {
    public void run(final MessageReceivedEvent event) {
        event.getChannel().sendMessage("""
                            Справка по командам:
                            `!ban` - Банит пользователя. Использование: %s;
                            `!clear` - Удаляет указанное количество сообщений в канале. Использование: %s;
                            `!help` - Показывает эту справку;
                            `!kick` - Выгоняет участника. Использование: %s;
                            `!mute` - Глушит участника. Использование: %s;
                            `!ping` - Измеряет время обработки REST-запроса;
                            `!unban` - Возвращает пользователя из бана. Использование: %s
                            `!unmute` - Возвращает участника из карцера. Использование: %s"""
                           .formatted(Ban.usage, Clear.usage, Kick.usage, Mute.usage, Unban.usage, Unmute.usage)).queue();
    }
}

