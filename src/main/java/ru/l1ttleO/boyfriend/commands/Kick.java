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

public class Kick extends Command {
    
    public Kick() {
        super("kick", "Выгоняет участника", "kick <@упоминание или ID> <причина>");
    }

    public void run(final @NotNull MessageReceivedEvent event, final String @NotNull [] args) {
        final Member author = event.getMember();
        final Member kicked;
        final MessageChannel channel = event.getChannel();
        if (args.length < 3) {
            sendInvalidUsageMessage(channel, "Требуется указать причину!");
            return;
        }
        if (author == null)
            throw new IllegalStateException("Автор является null");
        if (!author.hasPermission(Permission.KICK_MEMBERS)) {
            sendNoPermissionsMessage(channel);
            return;
        }
        if ((kicked = getMember(args[1], event.getGuild(), channel)) == null) return;
        if (!author.canInteract(kicked)) {
            channel.sendMessage("У тебя недостаточно прав для кика этого пользователя!").queue();
            return;
        }
        final String reason = StringUtils.join(args, ' ', 2, args.length);
        Actions.kickMember(channel, author, kicked, reason);
    }
}
