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

public class Duration {
    public static String getDurationMultiplier(final String toParse) {
        if (toParse.endsWith("s")) return " секунд";
        if (toParse.endsWith("m")) return " минут";
        if (toParse.endsWith("h")) return " часов";
        if (toParse.endsWith("d")) return " дней";
        return " секунд";
    }

    public static int getDurationMultiplied(final String toParse) {
        try {
            if (toParse.endsWith("s")) return Integer.parseInt(toParse.replace('s', ' ').trim());
            if (toParse.endsWith("m")) return Integer.parseInt(toParse.replace('m', ' ').trim()) * 60;
            if (toParse.endsWith("h")) return Integer.parseInt(toParse.replace('h', ' ').trim()) * 60 * 60;
            if (toParse.endsWith("d")) return Integer.parseInt(toParse.replace('d', ' ').trim()) * 60 * 60 * 24;
            return Integer.parseInt(toParse);
        } catch (final NumberFormatException ignored) {
            return 0;
        }
    }
}
