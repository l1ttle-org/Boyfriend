/*
    This file is part of Boyfriend
    Copyright (C) 2021  l1ttleO

    Boyfriend is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Boyfriend is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Boyfriend.  If not, see <https://www.gnu.org/licenses/>.
*/

package ru.l1ttleO.boyfriend;

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
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.exceptions.ImprobableException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public class EventListener extends ListenerAdapter {

    @Override
    public void onReady(final @NotNull ReadyEvent event) {
        Actions.getBotLogChannel(event.getJDA()).sendMessage("%s Я запустился".formatted(Utils.getBeep())).queue();
    }

    @Override
    public void onGuildMemberJoin(final @NotNull GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final TextChannel systemChannel = guild.getSystemChannel();
        if (systemChannel != null)
            systemChannel.sendMessage(event.getMember().getAsMention() + ", добро пожаловать на сервер " + guild.getName()).queue();
    }

    @Override
    public void onMessageReceived(final @NotNull MessageReceivedEvent event) {
        final JDA jda = event.getJDA();
        final Message message = event.getMessage();
        final MessageChannel channel = event.getChannel();
        final TextChannel logChannel = jda.getTextChannelById("860066351430238248");
        final User author = event.getAuthor();
        if (message.mentionsEveryone())
            return;
        if (message.isFromType(ChannelType.PRIVATE) && !author.isBot()) {
            if (logChannel == null)
                throw new ImprobableException("Канал #private-messages является null. Возможно, в коде указан неверный ID канала");
            logChannel.sendMessage("Я получил следующее сообщение от %s:```%s ```"
                .formatted(author.getAsMention(), message.getContentDisplay().replaceAll("```", "`​`​`"))).queue();
            return;
        }
        final Guild guild = event.getGuild();
        if (message.getMentionedMembers().size() > 3 && !author.isBot()) {
            try {
                channel.sendTyping().complete();
                Actions.banMember(channel, guild.getSelfMember(), author, "Более 3 упоминаний в 1 сообщении", 0, "всегда");
            } catch (final @NotNull Exception e) {
                channel.sendMessage("Произошла непредвиденная ошибка во время бана за масс-пинг: " + e.getMessage()).queue();
                e.printStackTrace();
            }
            return;
        }
        try {
            CommandHandler.onMessageReceived(event);
        } catch (final @NotNull WrongUsageException ignored) {
        }
    }
}
