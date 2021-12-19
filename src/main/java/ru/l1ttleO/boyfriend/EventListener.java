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
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.commands.util.CommandHandler;
import ru.l1ttleO.boyfriend.commands.util.Sender.MessageSender;
import ru.l1ttleO.boyfriend.settings.GuildSettings;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class EventListener extends ListenerAdapter {

    @Override
    public void onReady(final @NotNull ReadyEvent event) {
        new Thread(() ->
            Utils.sendBotLog(event.getJDA(), Utils.getBeep(BotLocale.RU) + " " + tl("common.ready", BotLocale.RU))
            ).start();
    }

    @Override
    public void onGuildMemberJoin(final @NotNull GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final TextChannel systemChannel = guild.getSystemChannel();
        final BotLocale locale = GuildSettings.LOCALE.get(guild);
        if (systemChannel != null)
            systemChannel.sendMessage(tl("common.welcome", locale, event.getMember().getAsMention(), guild.getName())).queue();
    }

    @Override
    public void onMessageReceived(final @NotNull MessageReceivedEvent event) {
        final JDA jda = event.getJDA();
        final Member member = event.getMember();
        final Message message = event.getMessage();
        final MessageChannel channel = event.getChannel();
        final TextChannel logChannel = jda.getTextChannelById("860066351430238248");
        final User author = event.getAuthor();
        if (message.isFromType(ChannelType.PRIVATE) && !author.isBot()) { // TODO: rework private messages
            if (logChannel != null)
                logChannel.sendMessage(tl("common.private_received", BotLocale.RU, author.getAsMention(), Utils.wrap(message.getContentDisplay()))).queue();
            return;
        }
        final Member selfMember = event.getGuild().getSelfMember();
        final BotLocale locale = GuildSettings.LOCALE.get(event.getGuild());
        final MessageSender sender = new MessageSender(message, locale);
        if ((message.getMentionedMembers().size() > 3 || message.getMentionedRoles().size() > 2) && !author.isBot() && member != null && selfMember.canInteract(member) && !member.hasPermission((GuildChannel) channel, Permission.MESSAGE_MENTION_EVERYONE)) {
            try {
                Actions.banMember(sender, selfMember, author, tl("actions.ban.massping.reason", locale), 0);
            } catch (final Exception e) {
                channel.sendMessage(tl("actions.ban.massping.failed", locale)).queue();
                e.printStackTrace();
            }
            return;
        }
        if (message.mentionsEveryone())
            return;
        CommandHandler.handleCommand(event, message.getContentRaw(), sender);
    }
}
