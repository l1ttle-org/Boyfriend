package ru.l1ttleO.boyfriend.commands.util;

import java.util.Iterator;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.Utils;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;
import ru.l1ttleO.boyfriend.settings.Settings;

import static ru.l1ttleO.boyfriend.I18n.tl;

public class CommandReader {
    public final @NotNull String prefix;
    public final @NotNull String alias;
    @Deprecated
    public final @NotNull String[] args;
    private final @NotNull Iterator<String> iter;
    public final @NotNull Sender sender;

    public CommandReader(final @NotNull String prefix, final @NotNull String input, final @NotNull Sender sender) {
        this.prefix = prefix;
        this.args = input.split(" ");
        this.iter = List.of(this.args).iterator();
        this.alias = this.iter.next().substring(prefix.length());
        this.sender = sender;
    }

    public WrongUsageException noArgumentException(final @NotNull String argumentName) throws WrongUsageException {
        final BotLocale locale = this.sender.getLocale();
        return new WrongUsageException(tl("command.no_argument", locale, tl("command.no_argument." + argumentName, locale)));
    }

    public WrongUsageException badArgumentException(final @NotNull String argumentName, final Object... args) {
        return new WrongUsageException(tl("command.bad_argument." + argumentName, this.sender.getLocale(), args));
    }

    public boolean hasNext() {
        return this.iter.hasNext();
    }

    public String next(final @NotNull String argumentName) throws WrongUsageException {
        if (!this.hasNext())
            throw this.noArgumentException(argumentName);
        return this.iter.next();
    }

    public int nextInt() throws WrongUsageException {
        try {
            return Integer.parseInt(this.next("amount"));
        } catch (final NumberFormatException e) {
            throw badArgumentException("amount");
        }
    }

    public @NotNull User nextUser(final @NotNull JDA jda) throws WrongUsageException {
        final User user;
        try {
            user = Utils.getUser(this.next("user"), jda);
        } catch (final IllegalArgumentException e) {
            throw badArgumentException("user.unknown");
        }
        if (user == null)
            throw badArgumentException("user");
        return user;
    }

    public @NotNull Member nextMember(final @NotNull Guild guild) throws WrongUsageException {
        final Member member;
        try {
            member = Utils.getMember(this.next("member"), guild);
        } catch (final IllegalArgumentException e) {
            throw badArgumentException("member.unknown");
        }
        if (member == null)
            throw badArgumentException("member");
        return member;
    }

    public @NotNull Role nextRole(final @NotNull Guild guild) throws WrongUsageException {
        final Role role;
        try {
            role = guild.getRoleById(this.next("role"));
        } catch (final NumberFormatException e) {
            throw badArgumentException("role");
        }
        if (role == null) {
            throw badArgumentException("role.unknown");
        }
        return role;
    }

    public @NotNull String getRemaining() {
        final StringBuilder str = new StringBuilder();
        while (this.iter.hasNext())
            str.append(" ").append(this.iter.next());
        if (str.isEmpty())
            str.append(" ");
        return str.substring(1);
    }

    public @NotNull String getRemaining(final @NotNull String start) {
        final String remaining = this.getRemaining();
        return start + (start.isEmpty() || remaining.isEmpty() ? "" : " ") + remaining;
    }

    public <K, V> @NotNull V requireSetting(final K key, final Settings.Entry<K, V> entry) throws WrongUsageException {
        final V value = entry.get(key);
        if (value == null)
            throw new WrongUsageException(tl("common.setting_required", this.sender.getLocale(), entry.name)); // TODO rework exception
        return value;
    }
}
