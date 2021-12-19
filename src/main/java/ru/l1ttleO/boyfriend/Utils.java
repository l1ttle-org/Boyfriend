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
import java.util.Set;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Guild.Ban;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.settings.GuildSettings;
import static ru.l1ttleO.boyfriend.I18n.tl;

public class Utils {
    public static final @NotNull Random RANDOM = new Random();
    public static final @NotNull Pattern USER_PATTERN = getMentionPattern("@!?");
    public static final @NotNull Pattern CHANNEL_PATTERN = getMentionPattern("#");
    public static final @NotNull Pattern ROLE_PATTERN = getMentionPattern("@&");

    @SafeVarargs
    public static <T> T[] toArray(final @NotNull T... array) {
        return array;
    }

    @SafeVarargs
    public static <T> T randomElement(final @NotNull T... array) {
        return array[RANDOM.nextInt(array.length)];
    }

    public static String getBeep(final BotLocale locale) {
        return tl("common.beep." + randomElement("a", "b", "c"), locale);
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

    public static final LinkedHashMap<@NotNull ChronoField, Pair<@NotNull String, @NotNull String>> DURATION_TYPES = new LinkedHashMap<>();

    static {
        DURATION_TYPES.put(ChronoField.YEAR, Pair.of("y", "year"));
        DURATION_TYPES.put(ChronoField.MONTH_OF_YEAR, Pair.of("M", "month"));
        DURATION_TYPES.put(ChronoField.ALIGNED_WEEK_OF_YEAR, Pair.of("w", "week"));
        DURATION_TYPES.put(ChronoField.DAY_OF_YEAR, Pair.of("d", "day"));
        DURATION_TYPES.put(ChronoField.HOUR_OF_DAY, Pair.of("h", "hour"));
        DURATION_TYPES.put(ChronoField.MINUTE_OF_HOUR, Pair.of("m", "minute"));
        DURATION_TYPES.put(ChronoField.SECOND_OF_MINUTE, Pair.of("s", "second"));
    }

    public static @NotNull String getDurationText(long millis, final long from, final boolean accusative, final BotLocale locale) {
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
        String name;
        for (final var entry : durations.entrySet()) {
            name = "duration." + DURATION_TYPES.get(entry.getKey()).getRight() + plural(entry.getValue(), accusative ? ".accusative" : "", ".parentive", "s.parentive");
            out.append(negative && entry.getValue() > 0 ? "-" : "").append(entry.getValue()).append(" ").append(tl(name, locale));
            if (i < durations.size() - 2) {
                out.append(", ");
            } else if (i == durations.size() - 2) {
                out.append(" и ");
            }
            i++;
        }
        if (out.isEmpty())
            return tl("duration.few_moments", locale);
        return out.toString();
    }

    public static @NotNull String getDurationText(final long millis, final boolean accusative, final BotLocale locale) {
        return getDurationText(millis, System.currentTimeMillis(), accusative, locale);
    }

    public static @NotNull String getDurationSuffix(final long millis, final BotLocale locale) {
        return millis > 0 ? " " + getDurationText(millis, true, locale) : tl("duration.ever", locale);
    }

    /**
     * Converts the string representation of the duration to milliseconds.

     * @param duration string to parse. If plain number, treated as seconds. Can be in Discord time format.
     * @param from timestamp used to calculate months & etc. properly.
     * @return milliseconds
     * @throws NumberFormatException if {@code duration} doesn't match any of the used formats
     * @throws ArithmeticException if {@code duration} is too big to fit in {@code long}
     */
    public static long parseDuration(final @NotNull String duration, final long from) throws NumberFormatException, ArithmeticException {
        try {
            return Math.multiplyExact(Long.parseLong(duration), 1000);
        } catch (final NumberFormatException e) {
            if (duration.matches("<t:(\\d+)(:.)?>")) {
                try {
                    final long result = Long.parseLong(duration.split("\\D+$")[0].substring(3));
                    return Math.multiplyExact(result - from / 1000, 1000) - from % 1000; // result * 1000 - from, but more overflow-safe
                } catch (final NumberFormatException e2) {
                    throw new ArithmeticException("long overflow");
                }
            }
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
    
    public static long parseDuration(final @NotNull String duration) throws NumberFormatException, ArithmeticException {
        return parseDuration(duration, System.currentTimeMillis());
    }

    public static @NotNull String wrap(@NotNull String text) {
        text = text.replaceAll("```", "​`​`​`​");
        return "```" + text + (text.endsWith("`") || text.isEmpty() ? " " : "") + "```";
    }

    public static void purge(final @Nullable Message message) {
        if (message != null)
            message.getChannel().purgeMessages(message); // We don't use 'Message.delete()' to make sure alfred doesn't get mad
    }

    public static @NotNull Pair<@Nullable User, @Nullable Member> getUserAndMember(final @NotNull String from,
        final @Nullable JDA jda, final @Nullable Guild guild) throws IllegalArgumentException {
        User user = null;
        Member member = null;
        try {
            final String id = from.replaceAll("[^0-9]", "");
            if (jda != null)
                user = jda.retrieveUserById(id).complete();
            if (guild != null)
                member = guild.retrieveMemberById(id).complete();
        } catch (final ErrorResponseException e) { // unknown user or member, remains null
        } 
        return Pair.of(user, member);
    }

    public static @Nullable User getUser(final @NotNull String from, final @NotNull JDA jda) throws IllegalArgumentException {
        return getUserAndMember(from, jda, null).getLeft();
    }

    public static @Nullable Member getMember(final @NotNull String from, final @NotNull Guild guild) throws IllegalArgumentException {
        return getUserAndMember(from, null, guild).getRight();
    }

    public static void sendBotLog(final @NotNull JDA jda, final @NotNull CharSequence message) {
        for (Guild guild : jda.getGuilds()) {
            final MessageChannel channel = GuildSettings.BOT_LOG_CHANNEL.get(guild);
            if (channel != null)
                channel.sendMessage(message).queue();
        }
    }

    public static boolean isCreator(final long userID) {
        return Set.of(504343489664909322L, 459726660359553025L).contains(userID);
    }

    public static boolean hasPermission(final @NotNull Member member, final @NotNull Permission... permissions) {
        return member.hasPermission(permissions) || member.equals(member.getGuild().getOwner());
    }

    public static void checkInteractions(final @Nullable Member author, final @NotNull Member subject,
        final @NotNull BotLocale locale) throws NoPermissionException {
        if (author != null && isCreator(author.getIdLong()))
            return;
        if (subject.equals(author))
            throw new NoPermissionException(false, true, "interact_yourself", locale);
        final boolean selfInteract = subject.getGuild().getSelfMember().canInteract(subject);
        final boolean authorInteract = author == null || author.canInteract(subject);
        if (!selfInteract || !authorInteract)
            throw new NoPermissionException(!selfInteract, !authorInteract, "interact_user", locale);
    }

    public static @Nullable Ban getBan(final @NotNull Guild guild, final @NotNull User user) {
        try {
            return guild.retrieveBan(user).complete();
        } catch (final ErrorResponseException e) {
            return null;
        }
    }
    
    public static Pattern getMentionPattern(@NotNull String symbols) {
        return Pattern.compile("<" + symbols + "([0-9]+)>");
    }
    
    public static @Nullable String stripID(@NotNull String from, @NotNull String begin, @NotNull String end) {
        if (from.startsWith(begin) && from.endsWith(end))
            from = from.substring(begin.length(), from.length() - end.length());
        return from.matches("[0-9]+") ? from : null;
    }

    public static @Nullable String stripChannelID(final @NotNull String from) {
        return stripID(from, "<#", ">");
    }

    public static @Nullable String stripRoleID(final @NotNull String from) {
        return stripID(from, "<@&", ">");
    }

    public static @NotNull String toPlainText(@NotNull CharSequence from, @NotNull JDA jda) {
        from = USER_PATTERN.matcher(from).replaceAll(result -> {
            User user = jda.retrieveUserById(result.group(1)).complete();
            return user == null ? result.group() : "@" + user.getAsTag();
            });
        from = CHANNEL_PATTERN.matcher(from).replaceAll(result -> {
            AbstractChannel channel = jda.getGuildChannelById(result.group(1));
            return channel == null ? result.group() : "#" + channel.getName();
            });
        from = ROLE_PATTERN.matcher(from).replaceAll(result -> {
            Role role = jda.getRoleById(result.group(1));
            return role == null ? result.group() : "@" + role.getName();
            });
        return MarkdownSanitizer.sanitize(from.toString());
    }
}
