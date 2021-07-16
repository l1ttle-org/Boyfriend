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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Ping {
    public void run(final MessageReceivedEvent event) {
        final MessageChannel channel = event.getChannel();
        final JDA jda = channel.getJDA();
        final Random random = new Random();
        final String[] letters = {"а", "о", "и"};
        final int number = random.nextInt(letters.length);
        jda.getRestPing().queue(time ->
            channel.sendMessage("Б%sп! %sмс".formatted(letters[number], time)).queue()
        );
    }
}
