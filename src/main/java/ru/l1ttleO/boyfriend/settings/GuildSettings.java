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

package ru.l1ttleO.boyfriend.settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.I18n.BotLocale;

public class GuildSettings extends Settings {
    private static final Map<Guild, GuildSettings> GUILD_MAP = new HashMap<>();

    public static final BotLocaleEntry<Guild> LOCALE = new BotLocaleEntry<>(GuildSettings::get, "locale", null) {
        public BotLocale get(final Guild key) {
            return has(key) ? super.get(key) : BotLocale.detect(key);
        }
    };
    public static final StringEntry<Guild> PREFIX = new StringEntry<>(GuildSettings::get, "prefix", "t!");

    public GuildSettings(final @NotNull Guild guild) {
        super(guild.getId(), "Guild configuration file for Boyfriend");

        loadEntry(guild, LOCALE);
        loadEntry(guild, PREFIX);
    }

    @Override
    protected @NotNull File getConfigDir(final @NotNull File baseDir) throws IOException, SecurityException {
        final File configDir = baseDir.toPath().resolve("guild").toFile();
        //noinspection ResultOfMethodCallIgnored
        configDir.mkdir();
        return configDir;
    }

    public static GuildSettings get(final Guild guild) {
        if (!GUILD_MAP.containsKey(guild))
            GUILD_MAP.put(guild, new GuildSettings(guild));
        return GUILD_MAP.get(guild);
    }
}
