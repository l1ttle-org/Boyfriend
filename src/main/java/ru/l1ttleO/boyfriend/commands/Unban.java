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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Unban extends Command {

    public Unban() {
        super("unban", "Возвращает пользователя из бана", "unban <@упоминание или ID> <причина>");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws InvalidAuthorException, NoPermissionException, WrongUsageException {
        final Guild guild = event.getGuild();
        final JDA jda = guild.getJDA();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final User unbanned;
        if (args.length < 3)
            throw new WrongUsageException("Требуется указать причину!", channel, this.getUsages());
        if (author == null)
            throw new InvalidAuthorException();
        if (!author.hasPermission(Permission.BAN_MEMBERS))
            throw new NoPermissionException(channel, false, false);
        unbanned = getUser(args[1], jda, channel);
        if (unbanned == null) return;
        try {
            guild.retrieveBan(unbanned).complete();
        } catch (final @NotNull ErrorResponseException e) {
            channel.sendMessage("Пользователь не забанен!").queue();
            return;
        }
        final String reason = StringUtils.join(args, ' ', 2, args.length);
        Actions.unbanMember(channel, author, unbanned, reason);
    }
}
