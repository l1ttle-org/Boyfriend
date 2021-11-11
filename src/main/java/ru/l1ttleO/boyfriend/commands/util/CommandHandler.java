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

package ru.l1ttleO.boyfriend.commands.util;

import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.commands.Ban;
import ru.l1ttleO.boyfriend.commands.Clear;
import ru.l1ttleO.boyfriend.commands.Command;
import ru.l1ttleO.boyfriend.commands.Grant;
import ru.l1ttleO.boyfriend.commands.Help;
import ru.l1ttleO.boyfriend.commands.IChatCommand;
import ru.l1ttleO.boyfriend.commands.IConsoleCommand;
import ru.l1ttleO.boyfriend.commands.Kick;
import ru.l1ttleO.boyfriend.commands.Mute;
import ru.l1ttleO.boyfriend.commands.Ping;
import ru.l1ttleO.boyfriend.commands.Remind;
import ru.l1ttleO.boyfriend.commands.Send;
import ru.l1ttleO.boyfriend.commands.Settings;
import ru.l1ttleO.boyfriend.commands.Shutdown;
import ru.l1ttleO.boyfriend.commands.Unban;
import ru.l1ttleO.boyfriend.commands.Ungrant;
import ru.l1ttleO.boyfriend.commands.Unmute;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;
import ru.l1ttleO.boyfriend.commands.util.Sender.MessageSender;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;
import ru.l1ttleO.boyfriend.settings.GuildSettings;

public class CommandHandler {
    public static final @NotNull HashMap<String, Command> COMMANDS_ALL = new HashMap<>();
    public static final @NotNull HashMap<String, IChatCommand> COMMANDS_CHAT = new HashMap<>();
    public static final @NotNull HashMap<String, IConsoleCommand> COMMANDS_CONSOLE = new HashMap<>();

    static {
        register(
                new Ban(), new Clear(), new Grant(), new Help(), new Kick(), new Mute(), new Ping(),
                new Remind(), new Send(), new Settings(), new Shutdown(), new Unban(), new Ungrant(), new Unmute());
    }

    public static void register(final Command @NotNull ... commands) {
        for (final Command command : commands) {
            for (final String alias : command.aliases) {
                COMMANDS_ALL.put(alias, command);
                if (command instanceof IChatCommand)
                    COMMANDS_CHAT.put(alias, (IChatCommand) command);
                if (command instanceof IConsoleCommand)
                    COMMANDS_CONSOLE.put(alias, (IConsoleCommand) command);
            }
        }
    }

    public static @Nullable Command handleCommand(final @Nullable MessageReceivedEvent event, final @NotNull String content, final @NotNull Sender sender) {
        final String prefix = event == null ? "" : GuildSettings.PREFIX.get(event.getGuild());
        if (!content.startsWith(prefix)) return null;
        CommandReader reader = new CommandReader(prefix, content, sender);
        if (reader.alias.isEmpty()) reader = new CommandReader(prefix, content + "help", sender);
        final String alias = reader.alias;
        final Command command = COMMANDS_ALL.get(alias);
        if (command == null || event != null && !COMMANDS_CHAT.containsKey(alias) || event == null && !COMMANDS_CONSOLE.containsKey(alias)) {
            sender.replyTl("command.unknown", prefix);
            return null;
        }
        final BotLocale locale = sender.getLocale();
        try {
            if (event != null && sender instanceof MessageSender) {
                final Member self = event.getGuild().getSelfMember();
                final Member author = event.getMember();
                if (author == null)
                    throw new InvalidAuthorException(locale);
                final boolean selfPermission = Utils.hasPermission(self, command.botPermissions);
                final boolean authorPermission = Utils.hasPermission(author, command.userPermissions);
                if (!selfPermission || !authorPermission)
                    throw new NoPermissionException(!selfPermission, !authorPermission, "action", locale);
                final List<Message> history = event.getChannel().getHistory().retrievePast(3).complete();
                final String echoMessage = history.get(1).getContentRaw();
                final String echoMessageFailsafe = history.get(2).getContentRaw();
                if (event.getAuthor().isBot() && (echoMessage.startsWith(".echo") || echoMessageFailsafe.startsWith(".echo")
                                                  || echoMessage.endsWith(content) || echoMessageFailsafe.endsWith(content)))
                    return null;
                COMMANDS_CHAT.get(alias).run(event, reader, (MessageSender) sender);
            } else if (sender instanceof ConsoleSender) {
                COMMANDS_CONSOLE.get(alias).run(reader, (ConsoleSender) sender);
            }
        } catch (final WrongUsageException e) {
            sender.reply(e.getMessage() + " " + command.getUsages(prefix, locale, sender instanceof ConsoleSender, true));
        } catch (final NoPermissionException e) {
            sender.reply(e.getMessage());
        } catch (final Exception e) {
            sender.replyTl("command.error", e);
            e.printStackTrace();
        }
        return command;
    }
}
