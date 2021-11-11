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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Grant extends Command implements IConsoleCommand {

    public Grant() {
        super("grant", Permission.MANAGE_ROLES);
    }

    @Override
    public void run(final @NotNull CommandReader reader, final @NotNull ConsoleSender sender) throws NoPermissionException, WrongUsageException {
        final Guild guild = IConsoleCommand.readGuild(this, reader, sender);
        final Role role = reader.nextRole(guild);
        final Member member = reader.nextMember(guild);
        guild.addRoleToMember(member, role).complete();
        sender.replyTl("command.grant.done", role.getName(), member.getUser().getAsTag(), guild.getName());
    }
}
