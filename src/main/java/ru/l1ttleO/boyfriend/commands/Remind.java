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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.DelayedRunnable;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender.MessageSender;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;
import ru.l1ttleO.boyfriend.settings.GuildSettings;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class Remind extends Command implements IChatCommand {

    public Remind() {
        super("remind");
    }

    public static final @NotNull ThreadGroup REMINDERS_THREAD_GROUP = new ThreadGroup("Reminders");
    public static final @NotNull HashMap<Long, HashMap<DelayedRunnable, Pair<String, Long>>> REMINDERS = new HashMap<>();

    @Override
    public void run(final @NotNull MessageReceivedEvent event, final @NotNull CommandReader reader, final @NotNull MessageSender sender) throws WrongUsageException {
        final long duration;
        final Member author = event.getMember();
        final String text;
        final BotLocale locale = sender.getLocale();
        if (!reader.hasNext()) {
            final var userReminders = REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>());
            if (userReminders.isEmpty())
                sender.replyTl("command.remind.no_active_reminders");
            else {
                final StringBuilder listText = new StringBuilder(tl("command.remind.active_reminders", locale));
                String append;
                for (final Entry<DelayedRunnable, Pair<String, Long>> reminder : userReminders.entrySet()) {
                    append = """

                        <t:%s:R> %s <#%s>
                        %s""".formatted((reminder.getKey().startedAt + reminder.getKey().duration) / 1000, tl("common.in_channel", locale), reminder.getValue().getRight(), reminder.getValue().getLeft());
                    if (listText.isEmpty())
                        append = append.strip();
                    if (listText.length() + append.length() > 2000) {
                        sender.reply(listText);
                        listText.setLength(0); // clear
                        if (append.length() > 2000)
                            append = append.substring(0, 1994) + "..." + append.substring(append.length() - 3);
                    }
                    listText.append(append);
                }
                sender.reply(listText);
            }
            return;
        }
        try {
            duration = Utils.parseDuration(reader.next("duration"));
        } catch (final NumberFormatException e) {
            throw reader.badArgumentException("duration");
        } catch (final ArithmeticException e) {
            throw reader.badArgumentException("duration.too_big");
        }
        if (duration <= 0) {
            throw reader.badArgumentException("duration.not_positive");
        }
        if (!reader.hasNext())
            throw reader.noArgumentException("remind_text");
        text = Utils.wrap(reader.getRemaining());
        sender.replyTl("command.remind.reminder_set", Utils.getDurationText(duration, true, locale), text);

        final Guild guild = event.getGuild();
        final DelayedRunnable runnable = new DelayedRunnable(REMINDERS_THREAD_GROUP, (final @NotNull DelayedRunnable dr) -> {
            sender.reply(author.getAsMention() + " " + text);
            REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>()).remove(dr);
        }, "Remind timer " + author.getId(), duration, (final @NotNull DelayedRunnable dr) ->
                Actions.sendNotification(guild, tl("common.reminder_interrupted", GuildSettings.LOCALE.get(guild), author.getAsMention(), text), false));
        final var userReminders = REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>());
        userReminders.put(runnable, Pair.of(text, event.getChannel().getIdLong()));
        REMINDERS.put(author.getIdLong(), userReminders);
    }
}
