package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class Truth extends Command {

    public Truth() {
        super("truth", "Расскажет всю правду");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) {
        event.getChannel().sendMessage("Страйнарт - это батя Кейва").queue();
    }
}
