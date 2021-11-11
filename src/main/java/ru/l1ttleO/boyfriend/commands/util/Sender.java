package ru.l1ttleO.boyfriend.commands.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import ru.l1ttleO.boyfriend.Boyfriend;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.settings.GuildSettings;

import static ru.l1ttleO.boyfriend.I18n.tl;

public abstract class Sender {
    public abstract void reply(final @NotNull CharSequence text);

    public void replyTl(final @NotNull String key, final Object... args) {
        this.reply(tl(key, this.getLocale(), args));
    }

    public abstract BotLocale getLocale();

    public void update() {
    }

    public static class MessageSender extends Sender {
        public final @NotNull Message message;
        private @NotNull BotLocale locale;

        public MessageSender(final @NotNull Message message, final BotLocale locale) {
            this.message = message;
            this.locale = locale;
        }

        @Override
        public void reply(final @NotNull CharSequence text) {
            this.message.getChannel().sendMessage(text).queue();
        }

        @Override
        public BotLocale getLocale() {
            return this.locale;
        }

        public void update() {
            this.locale = GuildSettings.LOCALE.get(this.message.getGuild());
        }
    }

    public static class ConsoleSender extends Sender {
        public final @NotNull JDA jda;

        public ConsoleSender(final @NotNull JDA jda) {
            this.jda = jda;
        }

        @Override
        public void reply(final @NotNull CharSequence text) {
            System.out.println(text);
        }

        @Override
        public BotLocale getLocale() {
            return Boyfriend.consoleLocale;
        }
    }
}
