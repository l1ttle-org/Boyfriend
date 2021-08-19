package ru.l1ttleO.boyfriend;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import ru.l1ttleO.boyfriend.exceptions.ImprobableException;

public class Utils {
    public static final @NotNull Random RANDOM = new Random();

    public static <T> T randomElement(final T @NotNull [] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    public static String getBeep() {
        final String[] letters = {"а", "о", "и"};
        return "Б%sп!".formatted(randomElement(letters));
    }

    public static <T> T plural(long amount, final T one, final T two_to_four, final T five_to_zero) {
        if (amount < 0)
            amount *= -1;
        if (amount % 10 == 0 || amount % 10 > 4 || amount % 100 / 10 == 1)
            return five_to_zero;
        if (amount % 10 == 1)
            return one;
        return two_to_four;
    }

    public static final LinkedHashMap<@NotNull Integer, Pair<@NotNull String, @NotNull String[]>> DURATION_TYPES = new LinkedHashMap<>();
    static {
        DURATION_TYPES.put(Calendar.YEAR, Pair.of("y", new String[]{"год", "год", "года", "лет"}));
        DURATION_TYPES.put(Calendar.MONTH, Pair.of("M", new String[]{"месяц", "месяц", "месяца", "месяцев"}));
        DURATION_TYPES.put(Calendar.WEEK_OF_YEAR, Pair.of("w", new String[]{"неделя", "неделю", "недели", "недель"}));
        DURATION_TYPES.put(Calendar.DAY_OF_YEAR, Pair.of("d", new String[]{"день", "день", "дня", "дней"}));
        DURATION_TYPES.put(Calendar.HOUR_OF_DAY, Pair.of("h", new String[]{"час", "час", "часа", "часов"}));
        DURATION_TYPES.put(Calendar.MINUTE, Pair.of("m", new String[]{"минута", "минуту", "минуты", "минут"}));
        DURATION_TYPES.put(Calendar.SECOND, Pair.of("s", new String[]{"секунда", "секунду", "секунды", "секунд"}));
    }
    public static final int[] CHECK_ORDER = {Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_YEAR,
        Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR_OF_DAY};

    public static @NotNull String getDurationText(long millis, long from, final boolean accusative) {
        if (from == 0)
            from = System.currentTimeMillis();
        final LinkedHashMap<Integer, Integer> durations = new LinkedHashMap<>();
        boolean negative = millis < 0;
        if (negative)
            millis *= -1;

        // magic
        int millisOfTheDay = (int) (millis%(1000*60*60*24));
        millis -= millisOfTheDay;
        final GregorianCalendar dateFrom = new GregorianCalendar();
        dateFrom.setTimeInMillis(negative ? from - millis : from);
        final GregorianCalendar dateTo = new GregorianCalendar();
        dateTo.setTimeInMillis(negative ? from : from + millis);
        millisOfTheDay = Math.round(((float)millisOfTheDay) / 1000);
        for (final int unit : CHECK_ORDER) {
            int amount, max;
            if (unit >= Calendar.HOUR_OF_DAY) {
                max = dateFrom.getMaximum(unit) + 1;
                amount = millisOfTheDay % max;
                millisOfTheDay /= max;
            } else {
                if (dateFrom.get(unit) == dateTo.get(unit))
                    continue;
                switch (unit) {
                    case Calendar.YEAR:
                        amount = dateTo.get(unit) - dateFrom.get(unit);
                        break;
                    case Calendar.MONTH:
                        amount = dateTo.get(Calendar.MONTH) - dateFrom.get(Calendar.MONTH);
                        if (amount < 0) {
                            amount += 12;
                            durations.put(Calendar.YEAR, durations.get(Calendar.YEAR)-1);
                        }
                        break;
                    default: // DAY_OF_YEAR
                        amount = dateTo.get(Calendar.DAY_OF_MONTH) - dateFrom.get(Calendar.DAY_OF_MONTH);
                        if (amount < 0) {
                            amount = durations.getOrDefault(Calendar.MONTH, 12);
                            final int years = durations.getOrDefault(Calendar.YEAR, 0);
                            if (amount == 12)
                                durations.put(Calendar.YEAR, years - 1);
                            durations.put(Calendar.MONTH, amount - 1);
                            amount = dateTo.get(Calendar.DAY_OF_MONTH);
                            dateTo.add(Calendar.MONTH, -1);
                            max = dateTo.getActualMaximum(Calendar.DAY_OF_MONTH);
                            amount += max - Math.min(max, dateFrom.get(Calendar.DAY_OF_MONTH));
                        }
                        durations.put(Calendar.WEEK_OF_YEAR, amount / 7);
                        amount %= 7;
                        break;
                }
            }
            durations.put(unit, amount);
        }

        // filter and sort
        for (final int unit : DURATION_TYPES.keySet()) {
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
            out.append((negative ? "-" : "") + entry.getValue() + " " + plural(entry.getValue(), names[accusative ? 1 : 0], names[2], names[3]));
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
     * @throws NumberFormatException
     */
    public static long parseDuration(final @NotNull String duration, long from) throws NumberFormatException {
        if (from == 0)
            from = System.currentTimeMillis();
        try {
            return Long.parseLong(duration) * 1000;
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
        
        final Calendar date = new GregorianCalendar();
        date.setTimeInMillis(from);
        for (final var type : DURATION_TYPES.entrySet()) {
            if (durations.containsKey(type.getValue().getLeft()))
                date.add(type.getKey(), durations.remove(type.getValue().getLeft()));
        }
        if (!durations.isEmpty())
            throw new NumberFormatException("Unknown time unit \"%s\"".formatted(durations.keySet().toArray()[0]));
        return date.getTimeInMillis() - from;
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
}
