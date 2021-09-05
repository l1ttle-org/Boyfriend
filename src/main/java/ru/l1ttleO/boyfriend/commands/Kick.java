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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Kick extends Command {

    public Kick() {
        super("kick", "Выгоняет участника", "kick <@упоминание или ID> [-s] <причина>");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws InvalidAuthorException, NoPermissionException, WrongUsageException {
        boolean silent = false;
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member kicked = Utils.getMember(args[1], event.getGuild(), channel);
        int reasonIndex = 2;
        if (args.length < 3)
            throw new WrongUsageException("Требуется указать причину!");
        if (author == null)
            throw new InvalidAuthorException();
        if ("-s".equals(args[reasonIndex])) {
            silent = true;
            reasonIndex++;
        }
        final String reason = StringUtils.join(args, ' ', reasonIndex, args.length);
        if (!author.hasPermission(Permission.KICK_MEMBERS))
            throw new NoPermissionException(false, false);
        if (kicked == null)
            return;
        Utils.checkInteractions(event.getGuild(), author, kicked);
        if (silent)
            event.getMessage().delete().queue();
        Actions.kickMember(channel, author, kicked, reason, silent);
    }
}
