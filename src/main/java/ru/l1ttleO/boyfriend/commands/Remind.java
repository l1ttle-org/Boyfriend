package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Actions;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class Remind extends Command {

    public Remind() {
        super("remind", "Создаёт напоминание", "remind <время напоминания> <текст напоминания>");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws InvalidAuthorException, WrongUsageException {
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final String text;
        final int duration;
        if (args.length < 3)
            throw new WrongUsageException("Требуется указать текст напоминания!", channel, this.getUsages());
        if (author == null)
            throw new InvalidAuthorException();
        duration = Utils.getDurationMultiplied(args[1]);
        if (duration < 1)
            throw new WrongUsageException("Требуется указать продолжительность!", channel, this.getUsages());
        text = " ```" + StringUtils.join(args, ' ', 2, args.length).replaceAll("```", "`​`​`") + "```";
        channel.sendMessage("Напоминание успешно установлено. Через %s будет отправлено данное сообщение:%s".formatted(Utils.getDurationText(duration, true), text)).queue();
        final Thread thread = new Thread(() -> {
            try {
                Thread.sleep(duration * 1000L);
                channel.sendTyping().complete();
                channel.sendMessage(author.getAsMention() + text).queue();
            } catch (final InterruptedException e) {
                Actions.sendNotification(event.getGuild(), "Прерван таймер напоминания для %s: %s".formatted(author.getAsMention(), text), false);
            }
        }, "Remind timer " + author.getId());
        thread.start();
    }
}
