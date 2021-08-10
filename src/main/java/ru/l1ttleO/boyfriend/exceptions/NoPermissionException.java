package ru.l1ttleO.boyfriend.exceptions;

import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

public class NoPermissionException extends Exception {
    public NoPermissionException(final @NotNull MessageChannel channel, final boolean self, final boolean interact) {
        super("Недостаточно прав");
        if (self)
            channel.sendMessage("У меня недостаточно прав для взаимодействия с данным пользователем!").queue();
        else {
            if (interact)
                channel.sendMessage("У тебя недостаточно прав для взаимодействия с данным пользователем!").queue();
            else
                channel.sendMessage("У тебя недостаточно прав для использования этой команды!").queue();
        }
    }
}
