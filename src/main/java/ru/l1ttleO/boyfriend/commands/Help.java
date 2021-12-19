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
import java.util.HashSet;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.commands.util.CommandHandler;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;
import ru.l1ttleO.boyfriend.commands.util.Sender.MessageSender;
import ru.l1ttleO.boyfriend.settings.GuildSettings;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class Help extends Command implements IChatCommand, IConsoleCommand {

    public Help() {
        super("help", "?");
    }

    @Override
    public void run(final @NotNull MessageReceivedEvent event, final @NotNull CommandReader reader, final @NotNull MessageSender sender) {
        help(GuildSettings.PREFIX.get(event.getGuild()), sender);
    }

    @Override
    public void run(final @NotNull CommandReader reader, final @NotNull ConsoleSender sender) {
        help("", sender);
    }

    public static void help(final @NotNull String prefix, final @NotNull Sender sender) {
        final boolean isConsole = sender instanceof ConsoleSender;
        final ArrayList<Command> commands = new ArrayList<>(new HashSet<>(
            (isConsole ? CommandHandler.COMMANDS_CONSOLE : CommandHandler.COMMANDS_CHAT)
            .keySet().stream().map(name -> CommandHandler.COMMANDS_ALL.get(name)).toList()));
        final BotLocale locale = sender.getLocale();
        final StringBuilder text = new StringBuilder(tl("command.help.title", locale));
        commands.sort(Comparator.comparing(command -> command.aliases[0]));
        for (final Command command : commands) {
            if (command == null) continue;
            text.append("\n`%s%s` - %s".formatted(prefix, command.aliases[0], tl("command." + command.aliases[0] + ".description", locale)));
            final String usages = command.getUsages(prefix, locale, isConsole, false);
            if (usages != null)
                text.append(". ").append(usages);
            text.append(";");
        }
        sender.reply(text.deleteCharAt(text.length() - 1));
    }
}
