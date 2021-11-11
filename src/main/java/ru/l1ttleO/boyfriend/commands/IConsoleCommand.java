package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender.ConsoleSender;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public interface IConsoleCommand {
    public void run(final @NotNull CommandReader reader, final @NotNull ConsoleSender sender) throws InvalidAuthorException, NoPermissionException, WrongUsageException;

    public static TextChannel readChannel(final @NotNull Command command, final @NotNull CommandReader reader, final @NotNull ConsoleSender sender) throws NoPermissionException, WrongUsageException {
        TextChannel channel = null;
        try {
            channel = sender.jda.getTextChannelById(reader.next("channel"));
        } catch (final NumberFormatException e) {
        }
        if (channel == null)
            throw reader.badArgumentException("channel");
        command.checkSelfPermissions(channel.getGuild(), sender.getLocale());
        return channel;
    }

    public static Guild readGuild(final @NotNull Command command, final @NotNull CommandReader reader, final @NotNull ConsoleSender sender) throws NoPermissionException, WrongUsageException {
        Guild guild = null;
        try {
            guild = sender.jda.getGuildById(reader.next("guild"));
        } catch (final NumberFormatException e) {
        }
        if (guild == null)
            throw reader.badArgumentException("guild");
        command.checkSelfPermissions(guild, sender.getLocale());
        return guild;
    }
}
