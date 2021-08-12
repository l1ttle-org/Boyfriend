package ru.l1ttleO.boyfriend;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final @NotNull Random RANDOM = new Random();

    public static <T> T randomElement(final T @NotNull [] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    public static String getBeep() {
        final String[] letters = {"а", "о", "и"};
        return "Б%sп!".formatted(randomElement(letters));
    }

    public static <T> T plural(final long amount, final T first, final T second, final T five) {
        if (amount % 10 == 0 || amount % 10 > 4 || amount % 100 / 10 == 1)
            return five;
        if (amount % 10 == 1)
            return first;
        return second;
    }

    public static final int @NotNull [] DURATION_MULTIPLIERS = {60, 60, 24, 7, 1};
    public static final String @NotNull [] @NotNull [] DURATION_TEXTS =
        {{"s", "секунда", "секунду", "секунды", "секунд"}, {"m", "минута", "минуту", "минуты", "минут"},
        {"h", "час", "час", "часа", "часов"}, {"d", "день", "день", "дня", "дней"},
        {"w", "неделя", "неделю", "недели", "недель"}};

    public static @NotNull String getDurationText(int seconds, final boolean vin) {
        final StringBuilder out = new StringBuilder();
        int concatLength = 0;
        int amount;
        for (int i = 0; i < DURATION_TEXTS.length; i++) {
            amount = i < DURATION_TEXTS.length - 1 ? seconds % DURATION_MULTIPLIERS[i] : seconds;
            if (amount != 0) {
                switch (concatLength) {
                    case 0:
                        break;
                    case 1:
                        out.insert(0, " и ");
                        break;
                    default:
                        out.insert(0, ", ");
                        break;
                }
                concatLength++;
                out.insert(0, amount + " " + plural(amount, DURATION_TEXTS[i][vin ? 2 : 1], DURATION_TEXTS[i][3], DURATION_TEXTS[i][4]));
                seconds -= amount;
            }
            seconds /= DURATION_MULTIPLIERS[i];
        }
        return out.toString();
    }

    public static int getDurationMultiplied(@NotNull String toParse) throws NumberFormatException {
        try {
            return Integer.parseInt(toParse);
        } catch (final @NotNull NumberFormatException ignored) {
        }
        
        int result = 0;
        Set<String> used = Collections.emptySet();
        String[] buffer;
        StringBuilder input = new StringBuilder(toParse);
        while (!input.isEmpty()) {
            buffer = input.toString().split("\\d+$");
            if (buffer.length < 2 || used.contains(buffer[1]))
                throw new NumberFormatException();
            used.add(buffer[1]);
            input.setLength(input.length()-buffer[1].length());
            
            int multiplier = 1;
            int i = 0;
            for (; i < DURATION_TEXTS.length; i++) {
                if (buffer[0].equals(DURATION_TEXTS[i][0]))
                    break;
                multiplier *= DURATION_MULTIPLIERS[i];
            }
            if (i == DURATION_TEXTS.length) // modifier not found
                throw new NumberFormatException();

            buffer = input.toString().split("\\D+$");
            input.setLength(input.length()-buffer[1].length());
            result += Integer.parseInt(buffer[1])*multiplier;
        }
        return result;
    }
    
    public static String wrap(final @NotNull String text) {
        return "```" + text.replaceAll("```", "​`​`​`​") + " ```";
    }
}
