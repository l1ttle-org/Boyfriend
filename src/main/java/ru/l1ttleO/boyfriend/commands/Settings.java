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

package ru.l1ttleO.boyfriend.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.commands.util.CommandReader;
import ru.l1ttleO.boyfriend.commands.util.Sender.MessageSender;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;
import ru.l1ttleO.boyfriend.settings.GuildSettings;
import ru.l1ttleO.boyfriend.settings.Settings.Entry;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class Settings extends Command implements IChatCommand {
    public Settings() {
        super("settings", Permission.MANAGE_SERVER, "config");
    }

    @Override
    public void run(final @NotNull MessageReceivedEvent event, final @NotNull CommandReader reader, final @NotNull MessageSender sender) throws WrongUsageException {
        final Guild guild = event.getGuild();
        final BotLocale locale = sender.getLocale();
        final List<Entry<Guild, ?>> availableSettings = Arrays.asList(
            GuildSettings.LOCALE, GuildSettings.PREFIX, GuildSettings.MUTE_ROLE, GuildSettings.ADMIN_LOG_CHANNEL, GuildSettings.BOT_LOG_CHANNEL);
        
        if (!reader.hasNext()) {
            final StringBuilder listText = new StringBuilder(tl("command.settings.list", locale, guild.getName()));
            for (final Entry<Guild, ?> entry : availableSettings) {
                listText.append("\n`").append(entry.name).append("`: ").append(getEntryValue(entry, guild, locale));
            }
            sender.reply(listText);
            return;
        }
        String settingStr = reader.next("setting");
        final boolean isReset = "reset".equals(settingStr);
        if (isReset) settingStr = reader.next("setting");
        final Entry<Guild, ?> entry = availableSettings.stream().collect(Collectors.toMap(e -> e.name, e -> e)).get(settingStr);
        if (entry == null)
            throw reader.badArgumentException("setting");
        if (isReset) {
            entry.reset(guild);
            sender.update();
            sender.replyTl("settings." + entry.name + ".reset");
            return;
        }
        if (!reader.hasNext()) {
            sender.replyTl("settings." + entry.name + ".current", getEntryValue(entry, guild, locale));
            return;
        }
        try {
            entry.setFormatted(guild, reader.next(entry.type));
        } catch (final IllegalArgumentException e) {
            throw reader.badArgumentException(entry.type);
        }
        sender.update();
        sender.replyTl("settings." + entry.name + ".changed", getEntryValue(entry, guild, locale));
    }
    
    private @NotNull String getEntryValue(final @NotNull Entry<Guild, ?> entry, final @NotNull Guild guild, final @NotNull BotLocale locale) {
        return Optional.ofNullable(entry.formatted(guild)).orElse(tl("settings." + entry.name + ".not_set", locale));
    }
}
