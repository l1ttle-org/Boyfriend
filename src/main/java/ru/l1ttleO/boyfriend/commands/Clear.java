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

import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;
import ru.l1ttleO.boyfriend.commands.util.Sender.MessageSender;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;
import ru.l1ttleO.boyfriend.settings.GuildSettings;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class Clear extends Command implements IChatCommand, IConsoleCommand {

    public Clear() {
        super("clear", Permission.MESSAGE_MANAGE);
    }

    @Override
    public void run(final @NotNull MessageReceivedEvent event, final @NotNull CommandReader reader, final @NotNull MessageSender sender)
        throws NoPermissionException, WrongUsageException {
        run(event.getTextChannel(), event.getMessage(), reader);
    }

    @Override
    public void run(final @NotNull CommandReader reader, final @NotNull ConsoleSender sender)
        throws NoPermissionException, WrongUsageException {
        final TextChannel channel = IConsoleCommand.readChannel(this, reader, sender);
        run(channel, null, reader);
    }

    public static void run(final @NotNull TextChannel channel, final @Nullable Message message,
        final @NotNull CommandReader reader) throws WrongUsageException {

        final Member author = message == null ? channel.getGuild().getSelfMember() : message.getMember();
        final int requested = reader.nextInt() + (message == null ? 0 : 1);
        if (requested < 2)
            throw reader.badArgumentException("amount.less_than", 1);
        else if (requested > 100)
            throw reader.badArgumentException("amount.more_than", 99);
        final List<Message> messages = channel.getHistory().retrievePast(requested).complete();
        final int amount = messages.size();
        final BotLocale locale = GuildSettings.LOCALE.get(channel.getGuild());
        final String plural = tl("common.message" + Utils.plural(amount, ".accusative", ".parentive", "s.parentive"), locale);
        channel.purgeMessages(messages);
        Actions.sendNotification(channel.getGuild(), tl("actions.delete_messages.audit", locale, author.getAsMention(), amount, plural, channel.getAsMention()), true);
    }
}
