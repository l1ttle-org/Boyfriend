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

import java.util.ArrayList;
import java.util.Comparator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ru.l1ttleO.boyfriend.CommandHandler;

public class Help extends Command {
    
    public Help() {
        super("help", "Показывает эту справку");
    }

    public void run(final MessageReceivedEvent event, final String[] args) {
        final StringBuilder text = new StringBuilder("Справка по командам:");
        final ArrayList<Command> commands = new ArrayList<>(CommandHandler.COMMAND_LIST.values());
        commands.sort(Comparator.comparing(c -> c.NAME));
        for (final Command command : commands) {
            text.append("\n`%s%s` - %s".formatted(CommandHandler.prefix, command.NAME, command.DESCRIPTION));
            if (command.USAGES.length > 0)
                text.append(". ").append(command.getUsages());
            text.append(";");
        }
        text.deleteCharAt(text.length() - 1);
        event.getChannel().sendMessage(text).queue();
    }
}
