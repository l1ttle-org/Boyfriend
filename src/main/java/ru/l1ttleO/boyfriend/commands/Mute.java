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
import ru.l1ttleO.boyfriend.I18n;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

import static ru.l1ttleO.boyfriend.Boyfriend.getGuildSettings;
import static ru.l1ttleO.boyfriend.I18n.tl;

public class Mute extends Command {

    public Mute() {
        super("mute", "mute.description", "mute.usage");
    }

    public static final String @NotNull [] ROLE_NAMES = {"заключённый", "заключённые", "muted"};

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws InvalidAuthorException, NoPermissionException, WrongUsageException {
        boolean silent = false;
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member muted = Utils.getMember(args[1], event.getGuild(), channel);
        I18n.activeLocale = getGuildSettings(event.getGuild()).getLocale();
        if (args.length < 3)
            throw new WrongUsageException(tl("common.reason_required"));
        if (author == null)
            throw new InvalidAuthorException();
        if (!author.hasPermission(Permission.MESSAGE_MANAGE))
            throw new NoPermissionException(false, false);
        if (muted == null)
            return;
        Utils.checkInteractions(guild, author, muted);
        List<Role> roleList = new ArrayList<>();
        for (final String name : ROLE_NAMES) {
            roleList = guild.getRolesByName(name, true);
            if (!roleList.isEmpty()) break;
        }
        if (roleList.isEmpty()) {
            channel.sendMessage(tl("common.no_mute_role")).queue();
            return;
        }
        final Role role = roleList.get(0);
        long duration = 0;
        try {
            duration = Math.max(Utils.parseDuration(args[2], 0), 0);
        } catch (final NumberFormatException | ArithmeticException ignored) {
        }
        int reasonIndex = 2;
        if (duration > 0) {
            if (args.length < 4)
                throw new WrongUsageException(tl("common.reason_required"));
            reasonIndex++;
        } else duration = 3600_000;
        final String durationString = " " + Utils.getDurationText(duration, 0, true);
        if ("-s".equals(args[reasonIndex])) {
            silent = true;
            reasonIndex++;
        }
        if (silent)
            channel.purgeMessages(event.getMessage()); // We don't use 'Message.delete()' to make sure alfred doesn't get mad
        final String reason = StringUtils.join(args, ' ', reasonIndex, args.length);
        Actions.muteMember(channel, role, author, muted, reason, duration, durationString, silent);
    }
}
