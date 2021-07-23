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
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.Utils;

public class Mute extends Command {
    
    public Mute() {
        super("mute", "Глушит участника", "mute <@упоминание или ID> [<продолжительность>] <причина>");
    }

    public static final String[] ROLE_NAMES = {"заключённый", "заключённые", "muted"};
    
    public void run(final MessageReceivedEvent event, final String[] args) {
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member muted;
        if (author == null)
            throw new IllegalStateException("Автор является null");
        if (!author.hasPermission(Permission.MESSAGE_MANAGE)) {
            sendNoPermissionsMessage(channel);
            return;
        }
        if ((muted = getMember(args[1], event.getGuild(), channel)) == null) return;
        if (!author.canInteract(muted)) {
            channel.sendMessage("У тебя недостаточно прав для мута этого пользователя!").queue();
            return;
        }
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
            durationString = " " + Utils.getDurationText(duration, true);
            startIndex++;
        } else duration = 0; // extra check
        if (startIndex >= args.length) {
            sendInvalidUsageMessage(channel, "Требуется указать причину!");
            return;
        }
        final String reason = StringUtils.join(args, ' ', startIndex, args.length);
        Actions.muteMember(channel, role, author, muted, reason, duration, durationString);
    }
}
