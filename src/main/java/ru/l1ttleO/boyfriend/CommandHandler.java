package ru.l1ttleO.boyfriend;

import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.commands.Ban;
import ru.l1ttleO.boyfriend.commands.Clear;
import ru.l1ttleO.boyfriend.commands.Command;
import ru.l1ttleO.boyfriend.commands.Help;
import ru.l1ttleO.boyfriend.commands.Kick;
import ru.l1ttleO.boyfriend.commands.Mute;
import ru.l1ttleO.boyfriend.commands.Ping;
import ru.l1ttleO.boyfriend.commands.Remind;
import ru.l1ttleO.boyfriend.commands.Truth;
import ru.l1ttleO.boyfriend.commands.Unban;
import ru.l1ttleO.boyfriend.commands.Unmute;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class CommandHandler {
    public static final @NotNull String prefix = "!";
    public static final @NotNull HashMap<String, Command> COMMAND_LIST = new HashMap<>();

    static {
        register(
                new Ban(), new Clear(), new Help(), new Kick(), new Mute(), new Ping(), new Remind(), new Truth(), new Unban(), new Unmute());
    }

    public static void register(final Command @NotNull ... commands) {
        for (final Command command : commands)
            COMMAND_LIST.put(command.name, command);
    }

    public static void onMessageReceived(final @NotNull MessageReceivedEvent event) throws WrongUsageException {
        final Message message = event.getMessage();
        final MessageChannel channel = event.getChannel();
        final String content = message.getContentRaw();
        if (!content.startsWith(prefix)) return;
        final String[] args = content.split(" ");
        String name = args[0].substring(prefix.length()).toLowerCase();
        if (name.isEmpty()) name = "help";
        if (!COMMAND_LIST.containsKey(name)) {
            channel.sendMessage("Неизвестная команда! Попробуй `%shelp`".formatted(prefix)).queue();
            return;
        }
        final Command command = COMMAND_LIST.get(name);
        if (command.usages.length > 0 && args.length == 1)
            throw new WrongUsageException("Нету аргументов!", channel, command.getUsages());
        final List<Message> history = channel.getHistory().retrievePast(3).complete();
        final String echoMessage = history.get(1).getContentRaw();
        final String echoMessageFailsafe = history.get(2).getContentRaw();
        if (event.getAuthor().isBot() && (echoMessage.startsWith(".echo") || echoMessageFailsafe.startsWith(".echo")
                                          || echoMessage.equals(content) || echoMessageFailsafe.equals(content)))
            return;
        try {
            command.run(event, args);
        } catch (final @NotNull Exception e) {
            channel.sendMessage("Идентификатор ошибки: `" + e + "`").queue();
            e.printStackTrace();
        }
    }
}
