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

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.lang3.StringUtils;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class Boyfriend {
    private static final Map<Guild, GuildSettings> settingsMap = new HashMap<>();

    public static void main(final String[] args) throws LoginException, InterruptedException, IOException {
        final JDABuilder builder = JDABuilder.createDefault(Files.readString(Paths.get("token.txt")).trim());
        final Console console = System.console();

        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES);
        builder.setActivity(Activity.listening("VS Retrospecter - Ectospasm"));
        builder.addEventListeners(new EventListener());

        final JDA jda = builder.build().awaitReady();

        for (final Guild g : jda.getGuilds()) {
            final GuildSettings settings = new GuildSettings(g);
            settingsMap.put(g, settings);
        }

        while (true) {
            try {
                final String[] s = console.readLine().split(" ");
                if ("shutdown".equals(s[0])) {
                    console.printf(tl("console.shutting_down"));
                    jda.shutdownNow();
                    break;
                }
                if ("grant".equals(s[0])) {
                    final Guild guild = jda.getGuildById(s[1]);
                    if (guild == null) {
                        console.printf(tl("console.no_guild"));
                        continue;
                    }
                    final Role role = guild.getRoleById(s[2]);
                    if (role == null) {
                        console.printf(tl("console.no_role"));
                        continue;
                    }
                    guild.addRoleToMember(s[3], role).complete();
                    console.printf(tl("console.role_granted"));
                    continue;
                }
                final TextChannel tc;
                try {
                    tc = jda.getTextChannelById(s[0]);
                } catch (final NumberFormatException e) {
                    console.printf(tl("console.id_required"));
                    continue;
                }
                if (tc == null) {
                    console.printf(tl("console.no_channel"));
                    continue;
                }
                tc.sendMessage(StringUtils.join(s, ' ', 1, s.length)).queue();
                console.printf(tl("console.message_sent", tc.getName()));
            } catch (final Exception e) {
                console.printf(tl("console.error"));
                e.printStackTrace();
            }
        }
    }

    public static GuildSettings getGuildSettings(final Guild guild) {
        return settingsMap.get(guild);
    }
}
