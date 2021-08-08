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

import java.util.Random;
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
import ru.l1ttleO.boyfriend.Utils;

public class Ban extends Command {

    public Ban() {
        super("ban", "Банит участника", "ban <@упоминание или ID> [<продолжительность>] <причина>");
    }

    public void run(final @NotNull MessageReceivedEvent event, final String @NotNull [] args) {
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Random random = new Random();
        final User banned;
        if (args.length < 3) {
            sendInvalidUsageMessage(channel, "Требуется указать причину!");
            return;
        }
        if (author == null)
            throw new IllegalStateException("Автор является null");
        if (!author.hasPermission(Permission.BAN_MEMBERS)) {
            sendNoPermissionsMessage(channel);
            return;
        }
        banned = getUser(args[1], event.getJDA(), channel);
        if (banned == null)
            return;
        try {
            if (!author.canInteract(guild.retrieveMember(banned).complete())) {
                channel.sendMessage("У тебя недостаточно прав для бана этого пользователя!").queue();
                return;
            }
            if (!guild.getSelfMember().canInteract(guild.retrieveMember(banned).complete())) {
                channel.sendMessage("У меня недостаточно прав для бана этого пользователя!").queue();
                return;
            }
        } catch (final @NotNull ErrorResponseException e) { /* not on the server */ }
        int duration = Utils.getDurationMultiplied(args[2]);
        int startIndex = 2;
        String durationString = "всегда";
        if (duration > 0) {
            if (args.length < 4) {
                sendInvalidUsageMessage(channel, "Требуется указать причину!");
                return;
            }
            durationString = " " + Utils.getDurationText(duration, true);
            startIndex++;
        } else duration = 0; // extra check
        final String reason = StringUtils.join(args, ' ', startIndex, args.length);
        if (random.nextInt(101) == 100)
            channel.sendMessage("Я кастую бан!").queue();
        Actions.banMember(channel, author, banned, reason, duration, durationString);
    }
}
