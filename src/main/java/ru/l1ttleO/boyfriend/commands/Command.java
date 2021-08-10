package ru.l1ttleO.boyfriend.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.CommandHandler;
import ru.l1ttleO.boyfriend.exceptions.InvalidAuthorException;
import ru.l1ttleO.boyfriend.exceptions.NoPermissionException;
import ru.l1ttleO.boyfriend.exceptions.WrongUsageException;

public abstract class Command {
    public final String name;
    public final String[] usages;
    public final String description;

    public Command(final String name, final String description, final String... usages) {
        this.name = name;
        this.usages = usages;
        this.description = description;
    }

    public abstract void run(final MessageReceivedEvent event, final String[] args) throws InvalidAuthorException, NoPermissionException, WrongUsageException;

    public String getUsages() {
        return "Использование: `%s`"
                .formatted(CommandHandler.prefix + StringUtils.join(this.usages, "` или `" + CommandHandler.prefix));
    }

    public @NotNull Pair<User, Member> getUserAndMember(final @NotNull String from, final @Nullable JDA jda, final @Nullable Guild guild, final @NotNull MessageChannel channel) {
        User user = null;
        Member member = null;
        try {
            final String id = from.replaceAll("[^0-9]", "");
            if (jda != null)
                user = jda.retrieveUserById(id).complete();
            if (guild != null)
                member = guild.retrieveMemberById(id).complete();
        } catch (final @NotNull IllegalArgumentException e) {
            channel.sendMessage("Неправильно указан пользователь!").queue();
        } catch (final @NotNull ErrorResponseException e) {
            channel.sendMessage("Указан недействительный пользователь!").queue();
        }
        return Pair.of(user, member);
    }

    public User getUser(final @NotNull String from, final @Nullable JDA jda, final @NotNull MessageChannel channel) {
        return this.getUserAndMember(from, jda, null, channel).getLeft();
    }

    public Member getMember(final @NotNull String from, final @Nullable Guild guild, final @NotNull MessageChannel channel) {
        return this.getUserAndMember(from, null, guild, channel).getRight();
    }

}
