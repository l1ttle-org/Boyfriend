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
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Mute extends Command {

    public Mute() {
        super("mute", "Глушит участника", "mute <@упоминание или ID> [<продолжительность>] <причина>");
    }

    public static final String @NotNull [] ROLE_NAMES = {"заключённый", "заключённые", "muted"};

    public void run(final @NotNull MessageReceivedEvent event, final String @NotNull [] args) throws InvalidAuthorException, NoPermissionException, WrongUsageException {
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member muted = getMember(args[1], event.getGuild(), channel);
        if (args.length < 3)
            throw new WrongUsageException("Требуется указать причину!", channel, this.getUsages());
        if (author == null)
            throw new InvalidAuthorException();
        if (!author.hasPermission(Permission.MESSAGE_MANAGE))
            throw new NoPermissionException(channel, false, false);
        if (muted == null)
            return;
        if (!author.canInteract(muted))
            throw new NoPermissionException(channel, false, true);
        if (!guild.getSelfMember().canInteract(muted))
            throw new NoPermissionException(channel, true, true);
        List<Role> roleList = new ArrayList<>();
        for (final String name : ROLE_NAMES) {
            roleList = guild.getRolesByName(name, true);
            if (!roleList.isEmpty()) break;
        }
        if (roleList.isEmpty()) {
            channel.sendMessage("Не найдена роль мута!").queue();
            return;
        }
        final Role role = roleList.get(0);
        int duration = Utils.getDurationMultiplied(args[2]);
        int startIndex = 2;
        String durationString = "всегда";
        if (duration > 0) {
            if (args.length < 4)
                throw new WrongUsageException("Требуется указать причину!", channel, this.getUsages());
            durationString = " " + Utils.getDurationText(duration, true);
            startIndex++;
        } else duration = 0; // extra check
        final String reason = StringUtils.join(args, ' ', startIndex, args.length);
        Actions.muteMember(channel, role, author, muted, reason, duration, durationString);
    }
}
