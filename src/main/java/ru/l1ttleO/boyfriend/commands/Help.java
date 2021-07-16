/*
    This file is part of Boyfriend
    Copyright (C) 2021  l1ttleO

    Boyfriend is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Boyfriend is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Boyfriend.  If not, see <https://www.gnu.org/licenses/>.
*/

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

