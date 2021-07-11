package ru.l1ttleO.boyfriend.commands;

import java.util.Random;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import ru.l1ttleO.boyfriend.Boyfriend;
import ru.l1ttleO.boyfriend.Duration;

public class Ban {
    public static final String usage = "`!ban <@упоминание или ID> [<продолжительность>] <причина>`";
    public void run(MessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        JDA jda = guild.getJDA();
        Member author = event.getMember();
        MessageChannel channel = event.getChannel();
        Random random = new Random();
        User banned;
        assert author != null;
        if (!author.hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendMessage("У тебя недостаточно прав для выполнения данной команды!").queue();
            return;
        }
        if (args.length == 0) {
            channel.sendMessage("Нету аргументов! " + usage).queue();
            return;
        }
        try {
            String id = args[0].replaceAll("[^0-9]", "").replace("!", "").replace(">", "");
            banned = jda.retrieveUserById(id).complete();
        } catch (NumberFormatException e) {
            channel.sendMessage("Неправильно указан пользователь! " + usage).queue();
            return;
        }
        if (banned == null) {
            channel.sendMessage("Указан недействительный пользователь!").queue();
            return;
        }
        if (!author.canInteract(guild.retrieveMember(banned).complete())) {
            channel.sendMessage("У тебя недостаточно прав для бана этого пользователя!").queue();
            return;
        }
        int duration = Duration.getDurationMultiplied(args[1]);
        int startIndex = 1;
        String durationString;
        durationString = "всегда";
        if (duration != 0) {
            String multiplier = Duration.getDurationMultiplier(args[1]);
            durationString = " " + args[1].replaceAll("[A-z]", "" + multiplier);
            startIndex = 2;
        }
        String reason = StringUtils.join(args, ' ', startIndex, args.length);
        if (reason == null || reason.equals("")) {
            channel.sendMessage("Требуется указать причину!").queue();
            return;
        }
        if (random.nextInt(101) == 100)
            channel.sendMessage("Я кастую бан!").queue();
        Boyfriend.memberActions.banMember(jda, channel, author, banned, reason, duration, durationString);
    }
}
