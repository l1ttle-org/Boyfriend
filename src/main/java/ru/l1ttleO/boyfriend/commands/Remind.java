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

package ru.l1ttleO.boyfriend.commands;

import java.util.HashMap;
import java.util.Map.Entry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.DelayedRunnable;
import ru.l1ttleO.boyfriend.I18n;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

import static ru.l1ttleO.boyfriend.Boyfriend.getGuildSettings;
import static ru.l1ttleO.boyfriend.I18n.tl;

public class Remind extends Command {

    public Remind() {
        super("remind", "remind.description", "remind.usage1", "remind.usage2");
    }

    public static final @NotNull ThreadGroup REMINDERS_THREAD_GROUP = new ThreadGroup("Reminders");
    public static final @NotNull HashMap<Long, HashMap<DelayedRunnable, Pair<String, Long>>> REMINDERS = new HashMap<>();

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws InvalidAuthorException, WrongUsageException {
        final long duration;
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final String text;
        I18n.activeLocale = getGuildSettings(event.getGuild()).getLocale();
        if (author == null)
            throw new InvalidAuthorException();
        if (args.length < 2) {
            final var userReminders = REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>());
            if (userReminders.isEmpty())
                channel.sendMessage(tl("remind.no_active_reminders")).queue();
            else {
                final StringBuilder listText = new StringBuilder(tl("remind.active_reminders"));
                String append;
                for (final Entry<DelayedRunnable, Pair<String, Long>> reminder : userReminders.entrySet()) {
                    append = """

                        <t:%s:R> %s <#%s>
                        %s""".formatted((reminder.getKey().startedAt + reminder.getKey().duration) / 1000, tl("in"), reminder.getValue().getRight(), reminder.getValue().getLeft());
                    if (listText.isEmpty())
                        append = append.strip();
                    if (listText.length() + append.length() > 2000) {
                        channel.sendMessage(listText).queue();
                        listText.setLength(0); // clear
                        if (append.length() > 2000)
                            append = append.substring(0, 1994) + "..." + append.substring(append.length() - 3);
                    }
                    listText.append(append);
                }
                channel.sendMessage(listText).queue();
            }
            return;
        } else if (args.length < 3)
            throw new WrongUsageException(tl("remind.text_required"));
        try {
            duration = Utils.parseDuration(args[1], 0);
        } catch (final NumberFormatException e) {
            throw new WrongUsageException(tl("remind.duration_invalid"));
        } catch (final ArithmeticException e) {
            throw new WrongUsageException(tl("remind.duration_too_big"));
        }
        if (duration <= 0) {
            throw new WrongUsageException(tl("remind.duration_negative"));
        }
        text = Utils.wrap(StringUtils.join(args, ' ', 2, args.length));
        channel.sendMessage(tl("remind.reminder_active", Utils.getDurationText(duration, 0, true), text)).queue();

        final DelayedRunnable runnable = new DelayedRunnable(REMINDERS_THREAD_GROUP, (final @NotNull DelayedRunnable dr) -> {
            channel.sendMessage(author.getAsMention() + " " + text).queue();
            REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>()).remove(dr);
        }, "Remind timer " + author.getId(), duration, (final @NotNull DelayedRunnable dr) ->
                Actions.sendNotification(event.getGuild(), tl("remind.reminder_interrupted", author.getAsMention(), text), false));
        final var userReminders = REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>());
        userReminders.put(runnable, Pair.of(text, channel.getIdLong()));
        REMINDERS.put(author.getIdLong(), userReminders);
    }
}
