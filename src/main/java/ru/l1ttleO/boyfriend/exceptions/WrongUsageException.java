package ru.l1ttleO.boyfriend.exceptions;

import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

public class WrongUsageException extends Exception {

    public WrongUsageException(final String message, final @NotNull MessageChannel channel, final String usages) {
        super(message);
        channel.sendMessage(message + " " + usages).queue();
    }
}
