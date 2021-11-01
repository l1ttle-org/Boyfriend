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
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.CommandHandler;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class Help extends Command {

    public Help() {
        super("help", "help.description");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) {
        final StringBuilder text = new StringBuilder(tl("help.help"));
        final ArrayList<Command> commands = new ArrayList<>(CommandHandler.COMMAND_LIST.values());
        commands.sort(Comparator.comparing(command -> command.name));
        for (final Command command : commands) {
            text.append("\n`%s%s` - %s".formatted(CommandHandler.prefix, command.name, tl(command.description)));
            if (command.usages.length > 0)
                text.append(". ").append(tl(command.getUsages()));
            text.append(";");
        }
        text.deleteCharAt(text.length() - 1);
        event.getChannel().sendMessage(text).queue();
    }
}
