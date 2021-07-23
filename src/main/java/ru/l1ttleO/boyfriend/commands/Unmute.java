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

public class Unmute extends Command {
    
    public Unmute() {
        super("unmute", "Возвращает участника из мута", "unmute <@упоминание или ID> <причина>");
    }

    public void run(final MessageReceivedEvent event, final String[] args) {
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member unmuted;
        if (author == null)
            throw new IllegalStateException("Автор является null");
        if (!author.hasPermission(Permission.MESSAGE_MANAGE)) {
            sendNoPermissionsMessage(channel);
            return;
        }
        if (args.length < 3) {
            channel.sendMessage("Требуется указать причину!").queue();
            return;
        }
        if ((unmuted = getMember(args[1], event.getGuild(), channel)) == null) return;
        List<Role> roleList = new ArrayList<>();
        for (final String name : Mute.ROLE_NAMES) {
            roleList = guild.getRolesByName(name, true);
            if (!roleList.isEmpty()) break;
        }
        if (roleList.isEmpty()) {
            channel.sendMessage("Не найдена роль мута!").queue();
            return;
        }
        final Role role = roleList.get(0);
        if (!unmuted.getRoles().contains(role)) {
            channel.sendMessage("Участник не заглушен!").queue();
            return;
        }
        final String reason = StringUtils.join(args, ' ', 2, args.length);
        Actions.unmuteMember(channel, role, author, unmuted, reason);
    }
}
