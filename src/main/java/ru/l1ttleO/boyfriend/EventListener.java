package ru.l1ttleO.boyfriend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.l1ttleO.boyfriend.commands.Ban;
import ru.l1ttleO.boyfriend.commands.Clear;
import ru.l1ttleO.boyfriend.commands.Help;
import ru.l1ttleO.boyfriend.commands.Kick;
import ru.l1ttleO.boyfriend.commands.Ping;
import ru.l1ttleO.boyfriend.commands.Unban;

public class EventListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        Random random = new Random();
        String[] letters = { "а", "о", "и" };
        int number = random.nextInt(letters.length);
        Objects.requireNonNull(event.getJDA().getTextChannelById("618044439939645444")).sendMessage("Б%sп! Я запустился".formatted(letters[number])).queue();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Objects.requireNonNull(guild.getSystemChannel()).sendMessage(event.getMember().getAsMention() + ", добро пожаловать на сервер " + guild.getName()).queue();
    }

    public static final String[] commands = { "!ban", "!clear", "!help", "!kick", "!ping", "!unban" };
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        JDA jda = event.getJDA();
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        String str = message.getContentRaw();
        String[] args = str.split(" ");
        TextChannel logChannel = jda.getTextChannelById("860066351430238248");
        User author = event.getAuthor();
        List<String> list = new ArrayList<>(Arrays.asList(args));
        if (message.mentionsEveryone())
            return;
        if (author.isBot()) {
            if (str.startsWith("!"))
                channel.sendMessage("Что тебе от меня надо?").queue();
            return;
        }
        if (message.isFromType(ChannelType.PRIVATE)) {
            assert logChannel != null;
            logChannel.sendMessage(("Я получил следующее сообщение от %s:" +
                                    "```%s```").formatted(author.getAsMention(), message.getContentDisplay().replaceAll("```", ""))).queue();
            return;
        }
        Guild guild = event.getGuild();
        if (message.getMentionedMembers().size() > 3) {
            try {
                Boyfriend.memberActions.banMember(jda, channel, guild.retrieveMemberById(jda.getSelfUser().getId()).complete(), author, "Более 3 упоминаний в 1 сообщении", 0, "всегда");
            } catch (Exception e) {
                channel.sendMessage("Произошла непредвиденная ошибка во время бана за масс-пинг: `%s: %s`".formatted(e, e.getMessage())).queue();
                e.printStackTrace();
            }
            return;
        }
        final Ban ban = new Ban();
        final Clear clear = new Clear();
        final Help help = new Help();
        final Kick kick = new Kick();
        final Ping ping = new Ping();
        final Unban unban = new Unban();
        try {
            if (str.startsWith("!") && !Arrays.stream(commands).toList().contains(args[0])) {
                channel.sendMessage("Неизвестная команда! Попробуй `!help`").queue();
                return;
            }
            list.remove(0);
            args = list.toArray(new String[0]);
            if (str.startsWith("!ban"))
                ban.run(event, args);
            if (str.startsWith("!clear"))
                clear.run(event, args);
            if (str.startsWith("!help"))
                help.run(event);
            if (str.startsWith("!kick"))
                kick.run(event, args);
            if (str.startsWith("!ping"))
                ping.run(event);
            if (str.startsWith("!unban"))
                unban.run(event, args);
        } catch (Exception e) {
            channel.sendMessage("Произошла непредвиденная ошибка во время выполнения команд: `%s: %s`".formatted(e, e.getMessage())).queue();
            e.printStackTrace();
        }
    }
}
