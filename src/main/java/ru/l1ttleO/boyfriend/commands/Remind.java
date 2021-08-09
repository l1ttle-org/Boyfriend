package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Utils;

public class Remind extends Command {

    public Remind() {
        super("remind", "Создаёт напоминание", "remind <время напоминания> <текст напоминания>");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) {
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final String text;
        final int duration;
        if (args.length < 3) {
            sendInvalidUsageMessage(channel, "Требуется указать текст напоминания!");
            return;
        }
        if (author == null)
            throw new IllegalStateException("Автор является null");
        duration = Utils.getDurationMultiplied(args[1]);
        if (duration < 1) {
            sendInvalidUsageMessage(channel, "Требуется указать продолжительность");
            return;
        }
        text = StringUtils.join(args, ' ', 2, args.length);
        channel.sendMessage("Напоминание успешно установлено").queue();
        final Thread thread = new Thread(() -> {
            try {
                Thread.sleep(duration * 1000L);
                channel.sendTyping().complete();
                channel.sendMessage(author.getAsMention() + "" + text).queue();
            } catch (final @NotNull InterruptedException ignored) {
            }
        }, "Remind timer " + author.getId());
        thread.start();
    }
}
