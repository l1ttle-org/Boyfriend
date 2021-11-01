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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
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

import static ru.l1ttleO.boyfriend.Boyfriend.getGuildSettings;
import static ru.l1ttleO.boyfriend.I18n.tl;

public class EventListener extends ListenerAdapter {

    @Override
    public void onReady(final @NotNull ReadyEvent event) {
        Utils.getBotLogChannel(event.getJDA()).sendMessage(Utils.getBeep() + " " + tl("common.ready")).queue();
    }

    @Override
    public void onGuildMemberJoin(final @NotNull GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final TextChannel systemChannel = guild.getSystemChannel();
        I18n.activeLocale = getGuildSettings(guild).getLocale();
        if (systemChannel != null)
            systemChannel.sendMessage(event.getMember().getAsMention() + tl("common.welcome") + " " + guild.getName()).queue();
    }

    @Override
    public void onMessageReceived(final @NotNull MessageReceivedEvent event) {
        final JDA jda = event.getJDA();
        final Member member = event.getMember();
        final Message message = event.getMessage();
        final MessageChannel channel = event.getChannel();
        final TextChannel logChannel = jda.getTextChannelById("860066351430238248");
        final User author = event.getAuthor();
        if (message.isFromType(ChannelType.PRIVATE) && !author.isBot()) {
            if (logChannel == null)
                throw new ImprobableException(); // TODO: rework private messages
            logChannel.sendMessage(tl("private.received", author.getAsMention(), Utils.wrap(message.getContentDisplay()))).queue();
            return;
        }
        final Guild guild = event.getGuild();
        I18n.activeLocale = getGuildSettings(guild).getLocale();
        if ((message.getMentionedMembers().size() > 3 || message.getMentionedRoles().size() > 2) && !author.isBot() && member != null && guild.getSelfMember().canInteract(member) && !member.hasPermission((GuildChannel) channel, Permission.MESSAGE_MENTION_EVERYONE)) {
            try {
                Actions.banMember(channel, guild.getSelfMember(), author, tl("autoban.reason"), 0, tl("ever"), false);
            } catch (final Exception e) {
                channel.sendMessage(tl("autoban.failed")).queue();
                e.printStackTrace();
            }
            return;
        }
        if (message.mentionsEveryone())
            return;
        CommandHandler.onMessageReceived(event);
    }
}
