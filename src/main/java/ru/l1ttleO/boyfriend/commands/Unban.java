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
import ru.l1ttleO.boyfriend.I18n;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

import static ru.l1ttleO.boyfriend.Boyfriend.getGuildSettings;
import static ru.l1ttleO.boyfriend.I18n.tl;

public class Unban extends Command {

    public Unban() {
        super("unban", "unban.description", "unban.usage");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws InvalidAuthorException, NoPermissionException, WrongUsageException {
        boolean silent = false;
        final Guild guild = event.getGuild();
        final JDA jda = guild.getJDA();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final User unbanned;
        int reasonIndex = 2;
        I18n.activeLocale = getGuildSettings(guild).getLocale();
        if (args.length < 3)
            throw new WrongUsageException(tl("common.reason_required"));
        if (author == null)
            throw new InvalidAuthorException();
        if (!author.hasPermission(Permission.BAN_MEMBERS))
            throw new NoPermissionException(false, false);
        unbanned = Utils.getUser(args[1], jda, channel);
        if (unbanned == null) return;
        try {
            guild.retrieveBan(unbanned).complete();
        } catch (final @NotNull ErrorResponseException e) {
            channel.sendMessage(tl("unban.user_not_banned")).queue();
            return;
        }
        if ("-s".equals(args[reasonIndex])) {
            silent = true;
            reasonIndex++;
        }
        if (silent)
            channel.purgeMessages(event.getMessage()); // We don't use 'Message.delete()' to make sure alfred doesn't get mad
        final String reason = StringUtils.join(args, ' ', reasonIndex, args.length);
        Actions.unbanMember(channel, author, unbanned, reason, silent);
    }
}
