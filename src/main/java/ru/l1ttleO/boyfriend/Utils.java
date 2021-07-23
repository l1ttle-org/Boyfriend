package ru.l1ttleO.boyfriend;

import java.util.Random;

public class Utils {
    public static final Random RANDOM = new Random();

    public static <T> T randomElement(final T[] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    public static String getBeep() {
        final String[] letters = {"а", "о", "и"};
        return "Б%sп!".formatted(randomElement(letters));
    }

    public static <T> T plural(final long amount, final T one, final T two, final T five) {
        if (amount % 10 == 0 || amount % 10 > 4 || amount % 100 / 10 == 1)
            return five;
        if (amount % 10 == 1)
            return one;
        return two;
    }

    public static final int[] DURATION_MULTIPLIERS = {60, 60, 24, 7, 1};
    public static final String[][] DURATION_TEXTS =
        {{"s", "секунда", "секунду", "секунды", "секунд"}, {"m", "минута", "минуту", "минуты", "минут"},
        {"h", "час", "час", "часа", "часов"}, {"d", "день", "день", "дня", "дней"},
        {"w", "неделя", "неделю", "недели", "недель"}};

    public static String getDurationText(int seconds, final boolean vin) {
        final StringBuilder out = new StringBuilder();
        int concat_length = 0;
        int amount;
        for (int i = 0; i < DURATION_TEXTS.length; i++) {
            amount = i < DURATION_TEXTS.length - 1 ? seconds % DURATION_MULTIPLIERS[i] : seconds;
            if (amount != 0) {
                switch (concat_length) {
                    case 0:
                        break;
                    case 1:
                        out.insert(0, " и ");
                        break;
                    default:
                        out.insert(0, ", ");
                        break;
                }
                concat_length++;
                out.insert(0, amount + " " + plural(amount, DURATION_TEXTS[i][vin ? 2 : 1], DURATION_TEXTS[i][3], DURATION_TEXTS[i][4]));
                seconds -= amount;
            }
            seconds /= DURATION_MULTIPLIERS[i];
        }
        return out.toString();
    }

    public static int getDurationMultiplied(final String toParse) {
        try {
            int multiplier = 1;
            for (int i = 0; i < DURATION_TEXTS.length; i++) {
                if (toParse.endsWith(DURATION_TEXTS[i][0])) return multiplier * Integer.parseInt(toParse.substring(0, toParse.length() - 1));
                multiplier *= DURATION_MULTIPLIERS[i];
            }
            return Integer.parseInt(toParse);
        } catch (final NumberFormatException ignored) {
            return 0;
        }
    }
}
