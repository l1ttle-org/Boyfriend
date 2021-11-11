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

import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Send extends Command implements IConsoleCommand {

    public Send() {
        super("send");
    }

    @Override
    public void run(final @NotNull CommandReader reader, final @NotNull ConsoleSender sender) throws NoPermissionException, WrongUsageException {
        final TextChannel channel = IConsoleCommand.readChannel(this, reader, sender);
        final String text = reader.getRemaining();
        if (text.isBlank())
            throw reader.noArgumentException("message");
        channel.sendMessage(text).queue();
        sender.replyTl("command.send.done", channel.getName());
    }
}
