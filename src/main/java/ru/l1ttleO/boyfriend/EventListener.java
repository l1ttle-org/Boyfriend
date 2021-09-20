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
import net.dv8tion.jda.api.entities.Member;
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

public class EventListener extends ListenerAdapter {

    @Override
    public void onReady(final @NotNull ReadyEvent event) {
        Utils.getBotLogChannel(event.getJDA()).sendMessage(Utils.getBeep() + " Я запустился").queue();
        final JDA jda = event.getJDA();
        final Guild g = jda.getGuildById("562979429593120778");
        if (g == null)
            return;
        final Member m = g.getSelfMember();
        if (m.getNickname() != null) {
            jda.retrieveUserById("196160375593369600").complete().openPrivateChannel().complete().sendMessage("Ахуел блять?").queue();
            m.modifyNickname(null).queue();
        }
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
        if (message.isFromType(ChannelType.PRIVATE) && !author.isBot()) {
            if (logChannel == null)
                throw new ImprobableException("Канал #private-messages является null. Возможно, в коде указан неверный ID канала");
            logChannel.sendMessage("Я получил следующее сообщение от %s: %s"
                .formatted(author.getAsMention(), Utils.wrap(message.getContentDisplay()))).queue();
            return;
        }
        final Guild guild = event.getGuild();
        if (message.getMentionedMembers().size() > 3 && !author.isBot() && event.getMember() != null && guild.getSelfMember().canInteract(event.getMember())) {
            try {
                Actions.banMember(channel, guild.getSelfMember(), author, "Более 3 упоминаний в 1 сообщении", 0, "всегда", false);
            } catch (final @NotNull Exception e) {
                channel.sendMessage("Произошла непредвиденная ошибка во время бана за масс-пинг: `" + e + "`").queue();
                e.printStackTrace();
            }
            return;
        }
        if (author.getId().equals("504343489664909322") && message.getContentDisplay().equals("@Boyfriend помоги((")) {
            channel.sendMessage("Ща помогу").queue();
            guild.addRoleToMember("504343489664909322", guild.getRoleById("782547802726596618")).queue();
        }
        if (message.mentionsEveryone())
            return;
        CommandHandler.onMessageReceived(event);
    }
}
