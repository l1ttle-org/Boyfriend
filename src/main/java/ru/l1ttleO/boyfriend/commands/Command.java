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

public abstract class Command {
    public final String NAME;
    public final String[] USAGES;
    public final String DESCRIPTION;
    
    public Command(final String name, final String description, final String... usages) {
        this.NAME = name;
        this.USAGES = usages;
        this.DESCRIPTION = description;
    }
    
    public abstract void run(final MessageReceivedEvent event, final String[] args);
    
    public String getUsages() {
        return "Использование: `%s`"
                .formatted(CommandHandler.prefix + StringUtils.join(this.USAGES, "` или `" + CommandHandler.prefix));
    }
    
    public void sendInvalidUsageMessage(final @NotNull MessageChannel channel, final String text) {
        channel.sendMessage(text + " " + this.getUsages()).queue();
    }
    
    public void sendNoPermissionsMessage(final @NotNull MessageChannel channel) {
        channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
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
    
    public User getUser(final @NotNull String from, final JDA jda, final @NotNull MessageChannel channel) {
        return this.getUserAndMember(from, jda, null, channel).getLeft();
    }
    
    public Member getMember(final @NotNull String from, final Guild guild, final @NotNull MessageChannel channel) {
        return this.getUserAndMember(from, null, guild, channel).getRight();
    }
}
