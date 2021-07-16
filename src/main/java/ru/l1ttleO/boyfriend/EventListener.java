package ru.l1ttleO.boyfriend;

import java.util.Objects;
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

public class EventListener extends ListenerAdapter {

    @Override
    public void onReady(final ReadyEvent event) {
        Objects.requireNonNull(event.getJDA().getTextChannelById("618044439939645444")).sendMessage("%s Я запустился".formatted(Utils.getBeep())).queue();
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
        final TextChannel logChannel = jda.getTextChannelById("860066351430238248");
        final User author = event.getAuthor();
        if (message.mentionsEveryone())
            return;
        if (author.isBot()) {
            return;
        }
        if (message.isFromType(ChannelType.PRIVATE)) {
            assert logChannel != null;
            logChannel.sendMessage(("Я получил следующее сообщение от %s:" +
                                    "```%s ```").formatted(author.getAsMention(), message.getContentDisplay().replaceAll("```", "`​`​`"))).queue();
            return;
        }
        final Guild guild = event.getGuild();
        if (message.getMentionedMembers().size() > 3) {
            try {
                Actions.banMember(channel, guild.getSelfMember(), author, "Более 3 упоминаний в 1 сообщении", 0, "всегда");
            } catch (final Exception e) {
                channel.sendMessage("Произошла непредвиденная ошибка во время бана за масс-пинг: " + e.getMessage()).queue();
                e.printStackTrace();
            }
            return;
        }
        CommandHandler.onMessageReceived(event);
    }
}
