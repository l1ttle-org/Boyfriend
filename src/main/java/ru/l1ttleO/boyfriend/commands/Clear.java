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
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class Clear extends Command {

    public Clear() {
        super("clear", "clear.description", "clear.usage");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws InvalidAuthorException, NoPermissionException, WrongUsageException {
        final MessageChannel channel = event.getChannel();
        final int requested;
        if (event.getMember() == null)
            throw new InvalidAuthorException();
        if (!event.getMember().hasPermission((GuildChannel) event.getChannel(), Permission.MESSAGE_MANAGE))
            throw new NoPermissionException(false, false);
        try {
            requested = Integer.parseInt(args[1]) + 1;
        } catch (final NumberFormatException e) {
            throw new WrongUsageException(tl("clear.amount.invalid"));
        }
        if (requested < 2)
            throw new WrongUsageException(tl("clear.amount.less_than_1"));
        else if (requested > 100)
            throw new WrongUsageException(tl("clear.amount.more_than_99"));
        final List<Message> messages = channel.getHistory().retrievePast(requested).complete();
        final int amount = messages.size();
        final String plural = Utils.plural(amount, tl("clear.message"), tl("clear.parentive.message"),
                tl("clear.parentive.messages"));
        channel.purgeMessages(messages);
        Actions.sendNotification(event.getGuild(), tl("audit.messages_deleted", event.getAuthor().getAsMention(), amount, plural, channel.getId()), true);
    }
}
