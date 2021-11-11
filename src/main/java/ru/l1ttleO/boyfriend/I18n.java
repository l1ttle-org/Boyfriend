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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class I18n {
    public static @Nullable String tl(final @NotNull String key, final @NotNull BotLocale locale, final Object... args) {
        try {
            return ResourceBundle.getBundle("lang.common", locale.locale).getString(key).formatted(args);
        } catch (final @NotNull MissingResourceException e) {
            return null;
        }
    }

    public enum BotLocale {
        EN("en"),
        RU("ru");

        public Locale locale;

        BotLocale(final String language) {
            this.locale = new Locale(language);
        }

        public static @NotNull BotLocale detect(final @Nullable Guild guild) {
            if (guild == null)
                return getDefault();
            final Locale locale = guild.getLocale();
            if (locale.getLanguage().equals(RU.locale.getLanguage()))
                return RU;
            return getDefault();
        }

        public static @NotNull BotLocale getDefault() {
            return EN;
        }
    }
}
