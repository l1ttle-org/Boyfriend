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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.exceptions.ImprobableException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;

public class Utils {
    public static final @NotNull Random RANDOM = new Random();

    public static <T> T randomElement(final T @NotNull [] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    public static String getBeep() {
        final String[] letters = {"а", "о", "и"};
        return "Б%sп!".formatted(randomElement(letters));
    }

    public static <T> T plural(long amount, final T one, final T twoToFour, final T fiveToZero) {
        if (amount < 0)
            amount *= -1;
        if (amount % 10 == 0 || amount % 10 > 4 || amount % 100 / 10 == 1)
            return fiveToZero;
        if (amount % 10 == 1)
            return one;
        return twoToFour;
    }

    public static final LinkedHashMap<@NotNull ChronoField, Pair<@NotNull String, @NotNull String[]>> DURATION_TYPES = new LinkedHashMap<>();

    static {
        DURATION_TYPES.put(ChronoField.YEAR, Pair.of("y", new String[]{"год", "год", "года", "лет"}));
        DURATION_TYPES.put(ChronoField.MONTH_OF_YEAR, Pair.of("M", new String[]{"месяц", "месяц", "месяца", "месяцев"}));
        DURATION_TYPES.put(ChronoField.ALIGNED_WEEK_OF_YEAR, Pair.of("w", new String[]{"неделя", "неделю", "недели", "недель"}));
        DURATION_TYPES.put(ChronoField.DAY_OF_YEAR, Pair.of("d", new String[]{"день", "день", "дня", "дней"}));
        DURATION_TYPES.put(ChronoField.HOUR_OF_DAY, Pair.of("h", new String[]{"час", "час", "часа", "часов"}));
        DURATION_TYPES.put(ChronoField.MINUTE_OF_HOUR, Pair.of("m", new String[]{"минута", "минуту", "минуты", "минут"}));
        DURATION_TYPES.put(ChronoField.SECOND_OF_MINUTE, Pair.of("s", new String[]{"секунда", "секунду", "секунды", "секунд"}));
    }

    public static @NotNull String getDurationText(long millis, long from, final boolean accusative) {
        if (from == 0)
            from = System.currentTimeMillis();
        final LinkedHashMap<ChronoField, Integer> durations = new LinkedHashMap<>();
        final boolean negative = millis < 0;
        if (negative)
            millis *= -1;

        // magic
        int millisOfTheDay = (int) (millis % (1000 * 60 * 60 * 24));
        millis -= millisOfTheDay;
        final LocalDate dateFrom = LocalDate.ofInstant(Instant.ofEpochMilli(negative ? from - millis : from), ZoneOffset.UTC);
        LocalDate dateTo = LocalDate.ofInstant(Instant.ofEpochMilli(negative ? from : from + millis), ZoneOffset.UTC);
        for (final ChronoField unit : DURATION_TYPES.keySet()) {
            if (unit == ChronoField.ALIGNED_WEEK_OF_YEAR) continue;
            int amount;
            final int max;
            if (unit.ordinal() <= ChronoField.HOUR_OF_DAY.ordinal()) {
                max = Math.toIntExact(unit.getBaseUnit().getDuration().toMillis());
                amount = millisOfTheDay / max;
                millisOfTheDay -= amount * max;
            } else {
                if (dateFrom.get(unit) == dateTo.get(unit))
                    continue;
                switch (unit) {
                    case YEAR -> amount = dateTo.get(unit) - dateFrom.get(unit);
                    case MONTH_OF_YEAR -> {
                        amount = dateTo.get(unit) - dateFrom.get(unit);
                        if (amount < 0) {
                            amount += 12;
                            durations.put(ChronoField.YEAR, durations.get(ChronoField.YEAR) - 1);
                        }
                    }
                    default -> { // DAY_OF_YEAR
                        amount = dateTo.get(ChronoField.DAY_OF_MONTH) - dateFrom.get(ChronoField.DAY_OF_MONTH);
                        if (amount < 0) {
                            amount = durations.getOrDefault(ChronoField.MONTH_OF_YEAR, 12);
                            final int years = durations.getOrDefault(ChronoField.YEAR, 0);
                            if (amount == 12)
                                durations.put(ChronoField.YEAR, years - 1);
                            durations.put(ChronoField.MONTH_OF_YEAR, amount - 1);
                            amount = dateTo.get(ChronoField.DAY_OF_MONTH);
                            dateTo = dateTo.plus(-1, ChronoField.MONTH_OF_YEAR.getBaseUnit());
                            max = dateTo.lengthOfMonth();
                            amount += max - Math.min(max, dateFrom.get(ChronoField.DAY_OF_MONTH));
                        }
                        durations.put(ChronoField.ALIGNED_WEEK_OF_YEAR, amount / 7);
                        amount %= 7;
                    }
                }
            }
            durations.put(unit, amount);
        }

        // filter and sort
        for (final @NotNull ChronoField unit : DURATION_TYPES.keySet()) {
            final int amount = durations.getOrDefault(unit, 0);
            durations.remove(unit);
            if (amount != 0)
                durations.put(unit, amount);
        }

        // build a string
        final StringBuilder out = new StringBuilder();
        int i = 0;
        String[] names;
        for (final var entry : durations.entrySet()) {
            names = DURATION_TYPES.get(entry.getKey()).getRight();
            out.append(negative && entry.getValue() > 0 ? "-" : "").append(entry.getValue()).append(" ").append(plural(entry.getValue(), names[accusative ? 1 : 0], names[2], names[3]));
            if (i < durations.size() - 2) {
                out.append(", ");
            } else if (i == durations.size() - 2) {
                out.append(" и ");
            }
            i++;
        }
        if (out.isEmpty())
            return "несколько мгновений";
        return out.toString();
    }

    /**
     * Converts the string representation of the duration to milliseconds.

     * @param duration string to parse. If plain number, treated as seconds. Can be in Discord time format.
     * @param from timestamp used to calculate months & etc. properly. If 0, current timestamp is used.
     * @return milliseconds
     * @throws NumberFormatException if {@code duration} doesn't match any of the used formats
     * @throws ArithmeticException if {@code duration} is too big to fit in {@code long}
     */
    public static long parseDuration(final @NotNull String duration, long from) throws NumberFormatException, ArithmeticException {
        if (from == 0)
            from = System.currentTimeMillis();
        try {
            final long result = Long.parseLong(duration);
            if (result >= 0 ^ result * 1000 >= 0)
                throw new ArithmeticException("Введена слишком большая продолжительность, из-за чего она стала отрицательной");
            return result * 1000;
        } catch (final @NotNull NumberFormatException ignored) {
            if (duration.matches("<t:(\\d+)(:.)?>"))
                return Long.parseLong(duration.split("\\D+$")[0].substring(3)) * 1000 - from;
        }

        final StringBuilder input = new StringBuilder(duration);
        final HashMap<String, Integer> durations = new HashMap<>();
        String[] buffer;
        String unit;
        while (!input.isEmpty()) {
            buffer = input.toString().split("-?\\d+(?=[^\\d-]+$)");
            if (buffer.length < 2)
                throw new NumberFormatException("Incorrect time format");
            unit = buffer[1];
            if (durations.containsKey(unit))
                throw new NumberFormatException("Duplicate time unit");
            input.setLength(input.length() - buffer[1].length());

            buffer = (" " + input).split("[^\\d-]+?(?=-?\\d+$)");
            input.setLength(input.length() - buffer[1].length());
            durations.put(unit, Integer.parseInt(buffer[1]));
        }

        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(from), ZoneOffset.UTC);
        for (final var type : DURATION_TYPES.entrySet()) {
            if (durations.containsKey(type.getValue().getLeft())) {
                final int amount = durations.remove(type.getValue().getLeft());
                final TemporalUnit baseUnit = type.getKey().getBaseUnit();
                date = switch(baseUnit.toString()) {
                    case "Years" -> date.plusYears(amount);
                    case "Months" -> date.plusMonths(amount);
                    case "Weeks" -> date.plusWeeks(amount);
                    default -> date.plus(Duration.of(amount, baseUnit));
                };
            }
        }
        if (!durations.isEmpty())
            throw new NumberFormatException("Unknown time unit \"%s\"".formatted(durations.keySet().toArray()[0]));
        return date.toInstant(ZoneOffset.UTC).toEpochMilli() - from;
    }

    public static @NotNull String wrap(final @NotNull String text) {
        return "```" + text.replaceAll("```", "​`​`​`​") + " ```";
    }

    public static @NotNull Pair<User, Member> getUserAndMember(final @NotNull String from, final @Nullable JDA jda, final @Nullable Guild guild, final @NotNull MessageChannel channel) {
        User user = null;
        Member member = null;
        try {
            final String id = from.replaceAll("[^0-9]", "");
            if (jda != null)
                user = jda.retrieveUserById(id).complete();
            if (guild != null)
                member = guild.retrieveMemberById(id).complete();
        } catch (final IllegalArgumentException e) {
            channel.sendMessage("Неправильно указан пользователь!").queue();
        } catch (final ErrorResponseException e) {
            channel.sendMessage("Указан недействительный пользователь!").queue();
        }
        return Pair.of(user, member);
    }

    public static User getUser(final @NotNull String from, final @Nullable JDA jda, final @NotNull MessageChannel channel) {
        return getUserAndMember(from, jda, null, channel).getLeft();
    }

    public static Member getMember(final @NotNull String from, final @Nullable Guild guild, final @NotNull MessageChannel channel) {
        return getUserAndMember(from, null, guild, channel).getRight();
    }

    public static @NotNull MessageChannel getBotLogChannel(final @NotNull JDA jda) {
        final MessageChannel botLogChannel = jda.getTextChannelById("618044439939645444");
        if (botLogChannel == null)
            throw new ImprobableException("Канал #бот-лог является null. Возможно, в коде указан неверный ID канала");
        return botLogChannel;
    }

    public static void checkInteractions(final @NotNull Guild guild, final @NotNull Member author,
            final @NotNull Member subject) throws NoPermissionException {
        final boolean selfInteract = guild.getSelfMember().canInteract(subject);
        final boolean authorInteract = author.canInteract(subject);
        if (!selfInteract || !authorInteract)
            throw new NoPermissionException(!selfInteract, !authorInteract);
    }
}
