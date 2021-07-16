package ru.l1ttleO.boyfriend.commands;

import java.util.ArrayList;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ru.l1ttleO.boyfriend.CommandHandler;

public class Help extends Command {
	
    public Help() {
		super("help", "Показывает эту справку");
	}

	public void run(final MessageReceivedEvent event, String[] args) {
		String text = "Справка по командам:";
		final ArrayList<Command> commands = new ArrayList<>(CommandHandler.COMMAND_LIST.values());
		commands.sort((c1, c2) -> {return c1.NAME.compareTo(c2.NAME);});
		for (Command command : commands) {
			text += "\n`%s%s` - %s".formatted(CommandHandler.prefix, command.NAME, command.DESCRIPTION);
			if (command.USAGES.length > 0)
				text += ". "+command.getUsages();
			text += ";";
		}
		text = text.substring(0, text.length()-1);
        event.getChannel().sendMessage(text).queue();
    }
}

