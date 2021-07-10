package ru.l1ttleO.boyfriend;

public class Duration {
    public static String getDurationMultiplier(String toParse) {
        if (toParse.endsWith("s")) return " секунд";
        if (toParse.endsWith("m")) return " минут";
        if (toParse.endsWith("h")) return " часов";
        if (toParse.endsWith("d")) return " дней";
        return "секунд";
    }

    public static int getDurationMultiplied(String toParse) {
        try {
            if (toParse.endsWith("s")) return Integer.parseInt(toParse.replace('s', ' ').trim());
            if (toParse.endsWith("m")) return Integer.parseInt(toParse.replace('m', ' ').trim()) * 60;
            if (toParse.endsWith("h")) return Integer.parseInt(toParse.replace('h', ' ').trim()) * 60 * 60;
            if (toParse.endsWith("d")) return Integer.parseInt(toParse.replace('d', ' ').trim()) * 60 * 60 * 24;
        } catch (NumberFormatException e) {
            return 0;
        }
        int i = 0;
        try {
            i = Integer.parseInt(toParse);
        } catch (NumberFormatException ignored) {}
        return i;
    }
}
