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
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;

public class Shutdown extends Command implements IConsoleCommand {

    public Shutdown() {
        super("shutdown", new Permission[0], "stop");
    }

    @Override
    public void run(final @NotNull CommandReader reader, final @NotNull ConsoleSender sender) {
        sender.replyTl("common.shutting_down");
        for (final var reminders : Remind.REMINDERS.values()) {
            for (final var reminder : reminders.keySet())
                reminder.thread.interrupt();
        }
        for (final var mutes : Actions.MUTES.values()) {
            for (final var mute : mutes.values())
                mute.interrupt();
        }
        for (final var bans : Actions.BANS.values()) {
            for (final var ban : bans.values())
                ban.interrupt();
        }
        sender.jda.shutdownNow();
    }
}
