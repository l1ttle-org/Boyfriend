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
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.IntegerOverflowException;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Remind extends Command {

    public Remind() {
        super("remind", "Создаёт напоминание", "remind", "remind <время напоминания, не больше 2147483647> <текст напоминания>");
    }

    public static final @NotNull ThreadGroup REMINDERS_THREAD_GROUP = new ThreadGroup("Reminders");
    public static final @NotNull HashMap<Long, HashMap<DelayedRunnable, Pair<String, Long>>> REMINDERS = new HashMap<>();

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws IntegerOverflowException, InvalidAuthorException, WrongUsageException {
        final int duration;
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final String text;
        if (author == null)
            throw new InvalidAuthorException();
        if (args.length < 2) {
            final var userReminders = REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>());
            if (userReminders.isEmpty())
                channel.sendMessage("Нет активных напоминаний").queue();
            else {
                final StringBuilder listText = new StringBuilder("Активные напоминания:\n");
                String append;
                for (final Entry<DelayedRunnable, Pair<String, Long>> reminder : userReminders.entrySet()) {
                    append = """

                        <t:%s:R> в <#%s>
                        %s""".formatted((reminder.getKey().startedAt + reminder.getKey().duration) / 1000, reminder.getValue().getRight(), reminder.getValue().getLeft());
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
            throw new WrongUsageException("Требуется указать текст напоминания!");
        try {
            duration = Utils.getDurationMultiplied(args[1]);
        } catch (final @NotNull NumberFormatException e) {
            throw new WrongUsageException("Неверно указана продолжительность!");
        }
        if (duration < 0) {
            throw new IntegerOverflowException();
        }
        text = Utils.wrap(StringUtils.join(args, ' ', 2, args.length));
        channel.sendMessage("Напоминание успешно установлено. Через %s будет отправлено данное сообщение: %s".formatted(Utils.getDurationText(duration, true), text)).queue();

        final DelayedRunnable runnable = new DelayedRunnable(REMINDERS_THREAD_GROUP, (final @NotNull DelayedRunnable dr) -> {
            channel.sendMessage(author.getAsMention() + " " + text).queue();
            REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>()).remove(dr);
        }, "Remind timer " + author.getId(), duration * 1000L, (final @NotNull DelayedRunnable dr) ->
                Actions.sendNotification(event.getGuild(), "Прерван таймер напоминания для %s: %s".formatted(author.getAsMention(), text), false));
        final var userReminders = REMINDERS.getOrDefault(author.getIdLong(), new HashMap<>());
        userReminders.put(runnable, Pair.of(text, channel.getIdLong()));
        REMINDERS.put(author.getIdLong(), userReminders);
    }
}
