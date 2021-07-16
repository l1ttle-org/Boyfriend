package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ru.l1ttleO.boyfriend.Utils;

public class Ping extends Command {
	
    public Ping() {
		super("ping", "Измеряет время обработки REST-запроса");
	}
    public void run(final MessageReceivedEvent event, String[] args) {
        final MessageChannel channel = event.getChannel();
        channel.getJDA().getRestPing().queue(time ->
            channel.sendMessage("%s %sмс".formatted(Utils.getBeep(), time)).queue()
        );
    }
}
