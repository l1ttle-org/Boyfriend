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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

public class GuildSettings {
    private Locale locale;
    final Properties properties = new Properties();
    final @NotNull File file;

    public GuildSettings(final @NotNull Guild guild) {
        final Path path = Paths.get("settings_" + guild.getId() + ".properties");
        this.file = path.toFile();
        try {
            if (!this.file.exists() || this.file.isDirectory())
                //noinspection ResultOfMethodCallIgnored
                this.file.createNewFile();
            final FileReader r = new FileReader(this.file);
            this.properties.load(r);
        } catch (final IOException e) {
            e.printStackTrace();
            guild.getJDA().shutdown();
        }
        if (this.properties.getProperty("locale") == null)
            this.properties.setProperty("locale", "en");
        this.locale = new Locale(this.properties.getProperty("locale"));
        try {
            this.saveSettings();
        } catch (final IOException e) {
            e.printStackTrace();
            guild.getJDA().shutdown();
        }
    }

    public void saveSettings() throws IOException {
        this.properties.store(new FileWriter(this.file), "Guild configuration file for Boyfriend");
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(final Locale locale) throws IOException {
        this.locale = locale;
        this.properties.setProperty("locale", this.locale.toString());
        this.saveSettings();
        I18n.activeLocale = locale;
    }
}
