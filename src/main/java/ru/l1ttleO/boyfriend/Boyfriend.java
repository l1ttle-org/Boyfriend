package ru.l1ttleO.boyfriend;

import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Boyfriend {
    public static final MemberActions memberActions = new MemberActions();

    public static void main(String[] args) throws LoginException, InterruptedException {
        JDABuilder builder = JDABuilder.createDefault("no");

        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES);
        builder.setActivity(Activity.listening("VS Tabi - Genocide"));
        builder.addEventListeners(new EventListener());

        builder.build().awaitReady();
    }
}
