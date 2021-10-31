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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import net.dv8tion.jda.api.entities.Guild;

public class ServerSettings {
    private Locale locale;

    public ServerSettings(final Guild guild) {
        try {
            final Path path = Paths.get("settings_" + guild.getId());
            final Properties properties = new Properties();
            final File file = path.toFile();
            if (!file.exists() || file.isDirectory())
                file.createNewFile();
            final FileReader r = new FileReader(file);
            properties.load(r);
            this.locale = new Locale(properties.getProperty("locale"), "en");

        } catch (final IOException e) {
            
        }
        this.locale = new Locale("ru");
    }

    public Locale getLocale() {
        return this.locale;
    }
}
