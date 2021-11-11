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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;
import ru.l1ttleO.boyfriend.commands.util.Sender.MessageSender;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Unmute extends Command implements IChatCommand, IConsoleCommand {

    public Unmute() {
        super("unmute", Permission.MESSAGE_MANAGE, Permission.MANAGE_ROLES);
    }

    @Override
    public void run(final @NotNull MessageReceivedEvent event, final @NotNull CommandReader reader, final @NotNull MessageSender sender)
        throws NoPermissionException, WrongUsageException {
        run(event.getGuild(), event.getMessage(), reader, sender);
    }

    @Override
    public void run(final @NotNull CommandReader reader, final @NotNull ConsoleSender sender)
        throws NoPermissionException, WrongUsageException {
        run(IConsoleCommand.readGuild(this, reader, sender), null, reader, sender);
    }

    public static void run(final @NotNull Guild guild, final @Nullable Message message, final @NotNull CommandReader reader,
        final @NotNull Sender sender) throws WrongUsageException, NoPermissionException {

        boolean silent = false;
        final BotLocale locale = sender.getLocale();
        final Member author = message == null ? guild.getSelfMember() : message.getMember();
        final Member unmuted = reader.nextMember(guild);
        Utils.checkInteractions(message == null ? null : author, unmuted, locale);
        List<Role> roleList = new ArrayList<>();
        for (final String name : Mute.ROLE_NAMES) {
            roleList = guild.getRolesByName(name, true);
            if (!roleList.isEmpty()) break;
        }
        if (roleList.isEmpty()) {
            sender.replyTl("actions.mute.no_role");
            return;
        }
        final Role role = roleList.get(0);
        if (!unmuted.getRoles().contains(role)) {
            sender.replyTl("actions.unmute.not_muted");
            return;
        }
        String reason = reader.next("reason");
        if ("-s".equals(reason)) {
            silent = true;
            reason = "";
        }
        reason = reader.getRemaining(reason);
        if (reason.isEmpty())
            throw reader.noArgumentException("reason");
        if (silent) Utils.purge(message);
        Actions.unmuteMember(silent ? null : sender, role, author, unmuted, reason, silent);
    }
}
