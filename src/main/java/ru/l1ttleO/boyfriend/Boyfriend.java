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

package ru.l1ttleO.boyfriend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.commands.util.CommandHandler;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;

public class Boyfriend {
    private static JDA jda;
    public static BotLocale consoleLocale = BotLocale.EN;

    public static void main(final String[] args) throws LoginException, InterruptedException, IOException {
        final JDABuilder builder = JDABuilder.createDefault(Files.readString(Paths.get("token.txt")).trim());

        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES);
        builder.setActivity(Activity.listening("VS Retrospecter - Ectospasm"));
        builder.addEventListeners(new EventListener());

        jda = builder.build().awaitReady();
        final ConsoleSender sender = new ConsoleSender(jda);
        final Scanner scanner = new Scanner(System.in);

        while (isRunning()) {
            try {
                final String input = scanner.nextLine();
                if (!isRunning()) break;
                CommandHandler.handleCommand(null, input, sender);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        scanner.close();
    }

    public static boolean isRunning() {
        final JDA.Status status = jda.getStatus();
        return status != Status.SHUTDOWN && status != Status.SHUTTING_DOWN;
    }
}
