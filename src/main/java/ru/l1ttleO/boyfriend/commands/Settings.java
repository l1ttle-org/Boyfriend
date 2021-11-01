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

import java.io.IOException;
import java.util.Locale;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.I18n;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

import static ru.l1ttleO.boyfriend.Boyfriend.getGuildSettings;
import static ru.l1ttleO.boyfriend.I18n.tl;

public class Settings extends Command {
    public Settings() {
        super("settings", "settings.description", "settings.usage");
    }

    public void run(final @NotNull MessageReceivedEvent event, final @NotNull String @NotNull [] args) throws InvalidAuthorException, NoPermissionException, WrongUsageException {
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Guild guild = event.getGuild();
        I18n.activeLocale = getGuildSettings(guild).getLocale();
        if (args.length < 2)
            throw new WrongUsageException(tl("settings.setting_required"));
        if (author == null)
            throw new InvalidAuthorException();
        if (!author.hasPermission(Permission.MANAGE_SERVER))
            throw new NoPermissionException(false, false);
        if ("locale".equals(args[1])) {
            if (args.length < 3) {
                channel.sendMessage(tl("settings.current_locale", getGuildSettings(guild).getLocale().toString())).queue();
                return;
            }
            if (!"ru".equals(args[2])) {
                channel.sendMessage(tl("settings.locale_not_available")).queue();
                return;
            }
            final Locale l = new Locale(args[2]);
            try {
                getGuildSettings(guild).setLocale(l);
            } catch (final IOException e) {
                channel.sendMessage(tl("settings.locale_change_failed")).queue();
                return;
            }
            channel.sendMessage(tl("settings.locale_changed", getGuildSettings(guild).getLocale().toString())).queue();
        } else {
            channel.sendMessage(tl("settings.no_setting")).queue();
        }
    }
}
