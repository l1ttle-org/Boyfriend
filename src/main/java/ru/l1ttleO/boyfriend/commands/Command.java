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

import java.util.ArrayList;
import java.util.stream.Stream;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;

import static ru.l1ttleO.boyfriend.I18n.tl;

public abstract class Command {
    public final @NotNull String @NotNull [] aliases;
    public final @NotNull Permission[] userPermissions;
    public final @NotNull Permission[] botPermissions;

    public Command(final @NotNull String name, final @NotNull Permission @NotNull [] userPermissions, final @NotNull Permission @NotNull [] botPermissions, final @NotNull String @NotNull ... aliases) {
        this.aliases = Stream.concat(Stream.of(name), Stream.of(aliases)).toArray(String[]::new);
        this.botPermissions = botPermissions;
        this.userPermissions = userPermissions;
    }

    public Command(final @NotNull String name, final @NotNull Permission @NotNull [] permissions, final @NotNull String... aliases) {
        this(name, permissions, permissions, aliases);
    }

    public Command(final @NotNull String name, final @NotNull Permission userPermission, final @NotNull Permission botPermission, final @NotNull String... aliases) {
        this(name, new Permission[]{userPermission}, new Permission[]{botPermission}, aliases);
    }

    public Command(final @NotNull String name, final @NotNull Permission permission, final @NotNull String... aliases) {
        this(name, permission, permission, aliases);
    }

    public Command(final @NotNull String name, final @NotNull String... aliases) {
        this(name, new Permission[]{}, aliases);
    }

    public @Nullable String getUsages(final @NotNull String prefix, final @NotNull BotLocale locale, final boolean isConsole, final boolean useFallback) {
        final ArrayList<String> usages = new ArrayList<>();
        int currentUsage = 1;
        String usage = tl("command." + this.aliases[0] + ".usage" + (isConsole ? "_console" : ""), locale, this.aliases[0]);
        if (usage == null) {
            if (!useFallback)
                return null;
            usage = this.aliases[0];
        }
        while (usage != null) {
            usages.add(usage);
            currentUsage++;
            usage = tl("command." + this.aliases[0] + ".usage" + (isConsole ? "_console" : "") + currentUsage, locale, this.aliases[0]);
        }
        return tl("command.usage", locale, prefix + StringUtils.join(usages, tl("command.usage.or", locale) + prefix));
    }

    public void checkSelfPermissions(final @NotNull Guild guild, final @NotNull BotLocale locale) throws NoPermissionException {
        if (!Utils.hasPermission(guild.getSelfMember(), this.botPermissions))
            throw new NoPermissionException(true, false, "action", locale);
    }
}
