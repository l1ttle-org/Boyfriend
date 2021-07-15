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
import ru.l1ttleO.boyfriend.commands.Mute;
import ru.l1ttleO.boyfriend.commands.Ping;
import ru.l1ttleO.boyfriend.commands.Unban;
import ru.l1ttleO.boyfriend.commands.Unmute;

public class EventListener extends ListenerAdapter {

    @Override
    public void onReady(final ReadyEvent event) {
        final Random random = new Random();
        final String[] letters = {"а", "о", "и"};
        final int number = random.nextInt(letters.length);
        Objects.requireNonNull(event.getJDA().getTextChannelById("618044439939645444")).sendMessage("Б%sп! Я запустился".formatted(letters[number])).queue();
    }

    @Override
    public void onGuildMemberJoin(final GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        Objects.requireNonNull(guild.getSystemChannel()).sendMessage(event.getMember().getAsMention() + ", добро пожаловать на сервер " + guild.getName()).queue();
    }

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        final JDA jda = event.getJDA();
        final Message message = event.getMessage();
        final MessageChannel channel = event.getChannel();
        final String str = message.getContentRaw();
        String[] args = str.split(" ");
        final TextChannel logChannel = jda.getTextChannelById("860066351430238248");
        final User author = event.getAuthor();
        final List<String> argsList = new ArrayList<>(Arrays.asList(args));
        if (message.mentionsEveryone())
            return;
        if (author.isBot()) {
            return;
        }
        if (message.isFromType(ChannelType.PRIVATE)) {
            assert logChannel != null;
            logChannel.sendMessage(("Я получил следующее сообщение от %s:" +
                                    "```%s```").formatted(author.getAsMention(), message.getContentDisplay().replaceAll("```", ""))).queue();
            return;
        }
        final Guild guild = event.getGuild();
        if (message.getMentionedMembers().size() > 3) {
            try {
                Boyfriend.memberActions.banMember(channel, guild.retrieveMemberById(jda.getSelfUser().getId()).complete(), author, "Более 3 упоминаний в 1 сообщении", 0, "всегда");
            } catch (final Exception e) {
                channel.sendMessage("Произошла непредвиденная ошибка во время бана за масс-пинг: " + e.getMessage()).queue();
                e.printStackTrace();
            }
            return;
        }
        final Ban ban = new Ban();
        final Clear clear = new Clear();
        final Help help = new Help();
        final Kick kick = new Kick();
        final Mute mute = new Mute();
        final Ping ping = new Ping();
        final Unban unban = new Unban();
        final Unmute unmute = new Unmute();
        try {
            final String command = argsList.get(0);
            argsList.remove(0);
            args = argsList.toArray(new String[0]);
            switch (command.toLowerCase()) {
                case "!ban" -> ban.run(event, args);
                case "!clear" -> clear.run(event, args);
                case "!help", "!" -> help.run(event);
                case "!kick" -> kick.run(event, args);
                case "!mute" -> mute.run(event, args);
                case "!ping" -> ping.run(event);
                case "!unban" -> unban.run(event, args);
                case "!unmute" -> unmute.run(event, args);
                default -> channel.sendMessage("Неизвестная команда! Попробуй `!help`").queue();
            }
        } catch (final Exception e) {
            channel.sendMessage("Произошла непредвиденная ошибка во время выполнения команд: " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }
}
