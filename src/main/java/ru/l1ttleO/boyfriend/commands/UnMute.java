package ru.l1ttleO.boyfriend.commands;

import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import ru.l1ttleO.boyfriend.Boyfriend;

public class UnMute {
    public static final String usage = "`!unmute <@упоминание или ID> <причина>`";

    public void run(final MessageReceivedEvent event, final String[] args) {
        final Guild guild = event.getGuild();
        final Member author = event.getMember();
        final MessageChannel channel = event.getChannel();
        final Member unMuted;
        assert author != null;
        if (!author.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length == 0) {
            channel.sendMessage("Нету аргументов! " + usage).queue();
            return;
        }
        try {
            final String id = args[0].replaceAll("[^0-9]", "").replace("!", "").replace(">", "");
            unMuted = guild.retrieveMemberById(id).complete();
        } catch (final NumberFormatException e) {
            channel.sendMessage("Неправильно указан пользователь! " + usage).queue();
            return;
        }
        if (unMuted == null) {
            channel.sendMessage("Указан недействительный пользователь!").queue();
            return;
        }
        List<Role> roleList = guild.getRolesByName("заключённый", true);
        if (roleList.isEmpty())
            roleList = guild.getRolesByName("muted", true);
        if (roleList.isEmpty()) {
            channel.sendMessage("Не найдена роль мута!").queue();
            return;
        }
        final Role role = roleList.get(0);
        final String reason = StringUtils.join(args, ' ', 1, args.length);
        if (reason == null || reason.equals("")) {
            channel.sendMessage("Требуется указать причину!").queue();
            return;
        }
        Boyfriend.memberActions.unMuteMember(channel, role, author, unMuted, reason);
    }
}
