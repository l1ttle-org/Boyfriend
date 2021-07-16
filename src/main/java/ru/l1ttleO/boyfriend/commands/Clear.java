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

import java.util.Objects;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Clear {
    public static final String usage = "`!clear <количество, не менее 1 и не больше 99>`";

    public void run(final MessageReceivedEvent event, final String[] args) {
        final MessageChannel channel = event.getChannel();
        final int requested;
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
        } catch (final NumberFormatException e) {
            channel.sendMessage("Неправильно указано количество! " + usage).queue();
            return;
        }
        if (requested < 2) {
            channel.sendMessage("Количество меньше 1! " + usage).queue();
            return;
        } else if (requested > 100) {
            channel.sendMessage("Количество больше 99! " + usage).queue();
            return;
        }
        channel.purgeMessages(channel.getHistory().retrievePast(requested).complete());
        channel.sendMessage("Успешно удалено %s сообщений".formatted(requested)).queue();
    }
}
