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

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.I18n;
import ru.l1ttleO.boyfriend.Utils;

import static ru.l1ttleO.boyfriend.Boyfriend.getServerSettings;
import static ru.l1ttleO.boyfriend.I18n.tl;

public class Ping extends Command {

    public Ping() {
        super("ping", "ping.description");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) {
        final MessageChannel channel = event.getChannel();
        I18n.activeLocale = getServerSettings(event.getGuild()).getLocale();
        channel.getJDA().getRestPing().queue(time ->
            channel.sendMessage("%s %s%s".formatted(Utils.getBeep(), time, tl("ms"))).queue()
        );
    }
}
