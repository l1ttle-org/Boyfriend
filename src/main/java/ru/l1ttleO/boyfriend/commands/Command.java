package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.CommandHandler;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.NumberOverflowException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public abstract class Command {
    public final String name;
    public final String[] usages;
    public final String description;

    public Command(final String name, final String description, final String... usages) {
        this.name = name;
        this.usages = usages;
        this.description = description;
    }

    public abstract void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws NumberOverflowException, InvalidAuthorException, NoPermissionException, WrongUsageException;

    public String getUsages() {
        return "Использование: `%s`"
                .formatted(CommandHandler.prefix + StringUtils.join(this.usages, "` или `" + CommandHandler.prefix));
    }

}
