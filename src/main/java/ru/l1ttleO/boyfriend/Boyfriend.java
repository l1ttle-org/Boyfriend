package ru.l1ttleO.boyfriend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Boyfriend {
    public static final MemberActions memberActions = new MemberActions();

    public static void main(final String[] args) throws LoginException, InterruptedException, IOException {
        final JDABuilder builder = JDABuilder.createDefault(Files.readString(Paths.get("token.txt")));

        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES);
        builder.setActivity(Activity.listening("VS Tabi - Genocide"));
        builder.addEventListeners(new EventListener());

        builder.build().awaitReady();
    }
}
