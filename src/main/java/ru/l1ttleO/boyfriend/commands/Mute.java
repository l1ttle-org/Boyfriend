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
import ru.l1ttleO.boyfriend.exceptions.NumberOverflowException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Mute extends Command {

    public Mute() {
        super("mute", "Глушит участника", "mute <@упоминание или ID> [<продолжительность>] [-s] <причина>");
    }

    public static final String @NotNull [] ROLE_NAMES = {"заключённый", "заключённые", "muted"};

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws NumberOverflowException, InvalidAuthorException, NoPermissionException, WrongUsageException {
        boolean silent = false;
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member muted = Utils.getMember(args[1], event.getGuild(), channel);
        if (args.length < 3)
            throw new WrongUsageException("Требуется указать причину!");
        if (author == null)
            throw new InvalidAuthorException();
        if (!author.hasPermission(Permission.MESSAGE_MANAGE))
            throw new NoPermissionException(false, false);
        if (muted == null)
            return;
        final boolean selfInteract = guild.getSelfMember().canInteract(muted);
        final boolean authorInteract = author.canInteract(muted);
        if (!selfInteract || !authorInteract)
            throw new NoPermissionException(!selfInteract, !authorInteract);
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
        long duration = 0;
        try {
            duration = Math.max(Utils.parseDuration(args[2], 0), 0);
        } catch (final @NotNull NumberFormatException ignored) {
        }
        int reasonIndex = 2;
        if (duration > 0) {
            if (args.length < 4)
                throw new WrongUsageException("Требуется указать причину!");
            reasonIndex++;
        } else if (duration < 0)
            throw new NumberOverflowException("Введена слишком большая продолжительность, из-за чего она стала отрицательной");
        else duration = 3600_000;
        final String durationString = " " + Utils.getDurationText(duration, 0, true);
        if ("-s".equals(args[reasonIndex])) {
            silent = true;
            reasonIndex++;
        }
        if (silent)
            event.getMessage().delete().queue();
        final String reason = StringUtils.join(args, ' ', reasonIndex, args.length);
        Actions.muteMember(channel, role, author, muted, reason, duration, durationString, silent);
    }
}
