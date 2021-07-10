package ru.l1ttleO.boyfriend.commands;

import java.util.Random;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Ping {
    public void run(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        JDA jda = channel.getJDA();
        Random random = new Random();
        String[] letters = {"а", "о", "и"};
        int number = random.nextInt(letters.length);
        jda.getRestPing().queue( (time) ->
            channel.sendMessage("Б%sп! %sмс".formatted(letters[number], time)).queue()
        );
    }
}
