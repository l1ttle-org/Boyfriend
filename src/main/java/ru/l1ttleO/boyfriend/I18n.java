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
import java.util.ResourceBundle;

public class I18n {
    public static Locale activeLocale = new Locale("en"); // This will work perfectly while the bot is synchronous

    public static String tl(final String key, final Object... args) {
        String s = ResourceBundle.getBundle("messages", activeLocale).getString(key).formatted(args);
        if (key.startsWith("console"))
            s += "\n";
        return s;
    }
}
