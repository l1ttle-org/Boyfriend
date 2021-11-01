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

package ru.l1ttleO.boyfriend;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.commands.Ban;
import ru.l1ttleO.boyfriend.commands.Clear;
import ru.l1ttleO.boyfriend.commands.Command;
import ru.l1ttleO.boyfriend.commands.Help;
import ru.l1ttleO.boyfriend.commands.Kick;
import ru.l1ttleO.boyfriend.commands.Mute;
import ru.l1ttleO.boyfriend.commands.Ping;
import ru.l1ttleO.boyfriend.commands.Remind;
import ru.l1ttleO.boyfriend.commands.Settings;
import ru.l1ttleO.boyfriend.commands.Unban;
import ru.l1ttleO.boyfriend.commands.Unmute;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class CommandHandler {
    public static final @NotNull String prefix = "!";
    public static final @NotNull HashMap<String, Command> COMMAND_LIST = new HashMap<>();

    static {
        register(
                new Ban(), new Clear(), new Help(), new Kick(), new Mute(), new Ping(), new Remind(), new Settings(), new Unban(), new Unmute());
    }

    public static void register(final Command @NotNull ... commands) {
        for (final Command command : commands)
            COMMAND_LIST.put(command.name, command);
    }

    public static void onMessageReceived(final @NotNull MessageReceivedEvent event) {
        final Message message = event.getMessage();
        final String content = message.getContentRaw();
        if (!content.startsWith(prefix)) return;
        final MessageChannel channel = event.getChannel();
        final String[] args = content.split(" ");
        String name = args[0].substring(prefix.length()).toLowerCase();
        if (name.isEmpty()) name = "help";
        if (!COMMAND_LIST.containsKey(name)) {
            channel.sendMessage(tl("command.unknown", prefix)).queue();
            return;
        }
        final Command command = COMMAND_LIST.get(name);
        final List<Message> history = channel.getHistory().retrievePast(3).complete();
        final String echoMessage = history.get(1).getContentRaw();
        final String echoMessageFailsafe = history.get(2).getContentRaw();
        if (event.getAuthor().isBot() && (echoMessage.startsWith(".echo") || echoMessageFailsafe.startsWith(".echo")
                                          || echoMessage.endsWith(content) || echoMessageFailsafe.endsWith(content)))
            return;
        try {
            if (command.usages.length > 0 && args.length == 1 && Arrays.stream(command.usages).noneMatch(name::equals))
                throw new WrongUsageException(tl("command.no_arguments"));
            command.run(event, args);
        } catch (final WrongUsageException e) {
            channel.sendMessage(e.getMessage() + " " + command.getUsages()).queue();
        } catch (final NoPermissionException e) {
            channel.sendMessage(e.getMessage()).queue();
        } catch (final Exception e) {
            channel.sendMessage(tl("command.error", e)).queue();
            e.printStackTrace();
        }
    }
}
