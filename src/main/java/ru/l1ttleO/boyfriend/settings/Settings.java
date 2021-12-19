package ru.l1ttleO.boyfriend.settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.l1ttleO.boyfriend.Boyfriend;
import ru.l1ttleO.boyfriend.I18n.BotLocale;
import ru.l1ttleO.boyfriend.Utils;

public abstract class Settings {
    protected final Properties properties = new Properties(); // what will be saved and what was loaded from disk
    protected final String filename;
    protected final String comments;

    public Settings(final @NotNull String filename, final @NotNull String comments) {
        this.filename = filename;
        this.comments = comments;
        try {
            final File settingsFile = this.getConfigFile();
            if (settingsFile.exists() && !settingsFile.isDirectory())
                this.properties.load(new FileReader(settingsFile));
        } catch (final IOException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public <K> boolean save(final K key, final @NotNull Entry<K, ?> entry) {
        final String value = entry.getString(key);
        if (value == null)
            this.properties.remove(entry.name);
        else
            this.properties.setProperty(entry.name, value);
        try {
            this.properties.store(new FileWriter(this.getConfigFile()), this.comments);
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    protected @NotNull File getConfigFile() throws IOException, SecurityException {
        final File configDir = new File("settings");
        //noinspection ResultOfMethodCallIgnored
        configDir.mkdir();
        return this.getConfigDir(configDir).toPath().resolve(this.filename + ".properties").toFile();
    }

    protected abstract @NotNull File getConfigDir(final @NotNull File baseDir) throws IOException, SecurityException;

    protected boolean hasProperty(final @NotNull String key) {
        return this.properties.containsKey(key);
    }

    protected String getProperty(final @NotNull String key) {
        return this.properties.getProperty(key);
    }

    protected <K> void loadEntry(final K key, final @NotNull Entry<K, ?> entry) {
        try {
            if (this.hasProperty(entry.name))
                entry.putFromString(key, this.getProperty(entry.name));
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
    }

    public static abstract class Entry<K, V> {
        protected final Map<K, V> VALUES = new HashMap<>(); // what is storing actual values
        protected final Function<K, Settings> settingsGetter;
        public final String name;
        public final String type;
        public final V defaultValue;

        public Entry(final @NotNull Function<K, Settings> settingsGetter, final @NotNull String name, final @NotNull String type, final V defaultValue) {
            this.settingsGetter = settingsGetter;
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        protected boolean save(final K key) {
            return this.settingsGetter.apply(key).save(key, this);
        }

        public boolean has(final K key) {
            this.settingsGetter.apply(key); // make sure settings are initialized and loaded
            return this.VALUES.containsKey(key);
        }

        public V get(final K key) {
            this.settingsGetter.apply(key); // make sure settings are initialized and loaded
            return this.VALUES.getOrDefault(key, this.defaultValue);
        }

        protected void put(final K key, final V value) {
            this.VALUES.put(key, value);
        }

        public void set(final K key, final V value) {
            this.put(key, value);
            this.save(key);
        }

        public void reset(final K key) {
            this.VALUES.remove(key);
            this.save(key);
        }

        public @Nullable String getString(final K key) {
            final V value = this.get(key);
            return value == null ? null : value.toString();
        }

        public abstract V putFromString(final K key, final @NotNull String str);

        public @Nullable String formatted(final K key) {
            final String str = this.getString(key);
            return str == null ? null : "`"+str+"`";
        }

        public V setFormatted(final K key, final String str) throws IllegalArgumentException {
            final V value = this.putFromString(key, str);
            this.save(key);
            return value;
        }
    }

    public static class StringEntry<K> extends Entry<K, String> {
        public StringEntry(final @NotNull Function<K, Settings> keyGetter, final @NotNull String name, final String defaultValue) {
            super(keyGetter, name, "string", defaultValue);
        }

        @Override
        public String putFromString(final K key, final String str) throws IllegalArgumentException {
            put(key, str);
            return str;
        }
    }

    public static class BotLocaleEntry<K> extends Entry<K, BotLocale> {
        public BotLocaleEntry(final @NotNull Function<K, Settings> keyGetter, final @NotNull String name, final BotLocale defaultValue) {
            super(keyGetter, name, "locale", defaultValue);
        }

        @Override
        public String getString(final K key) {
            return this.get(key).name().toLowerCase();
        }

        @Override
        public BotLocale putFromString(final K key, final String str) throws IllegalArgumentException {
            BotLocale locale;
            try {
                locale = BotLocale.valueOf(str.toUpperCase());
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException("Locale is unavailable: " + str);
            }
            put(key, locale);
            return locale;
        }

        @Override
        public BotLocale setFormatted(final K key, final @NotNull String str) throws IllegalArgumentException {
            final BotLocale locale = BotLocale.valueOf(str.toUpperCase());
            put(key, locale);
            save(key);
            return locale;
        }
    }

    public static class TextChannelEntry<K> extends Entry<K, TextChannel> {
        public TextChannelEntry(final @NotNull Function<K, Settings> keyGetter, final @NotNull String name) {
            super(keyGetter, name, "channel", null);
        }

        @Override
        public @Nullable String getString(final K key) {
            final TextChannel channel = this.get(key);
            return channel == null ? null : channel.getId();
        }

        @Override
        public TextChannel putFromString(final K key, final String str) throws IllegalArgumentException {
            TextChannel channel;
            try {
                channel = Boyfriend.getJDA().getTextChannelById(str);
                if (channel == null)
                    throw new IllegalArgumentException("Unable to find channel with ID " + str);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Invalid channel ID: " + str);
            }
            put(key, channel);
            return channel;
        }

        public @Nullable String formatted(final K key) {
            TextChannel channel = this.get(key);
            return channel == null ? null : channel.getAsMention();
        }

        @Override
        public TextChannel setFormatted(final K key, final @NotNull String str) throws IllegalArgumentException {
            final TextChannel channel = this.putFromString(key, Utils.stripChannelID(str));
            this.save(key);
            return channel;
        }
    }

    public static class RoleEntry<K> extends Entry<K, Role> {
        public RoleEntry(final @NotNull Function<K, Settings> keyGetter, final @NotNull String name) {
            super(keyGetter, name, "role", null);
        }

        @Override
        public @Nullable String getString(final K key) {
            final Role role = this.get(key);
            return role == null ? null : role.getId();
        }

        @Override
        public Role putFromString(final K key, final String str) throws IllegalArgumentException {
            Role role;
            try {
                role = Boyfriend.getJDA().getRoleById(str);
                if (role == null)
                    throw new IllegalArgumentException("Unable to find role with ID " + str);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Invalid role ID: " + str);
            }
            put(key, role);
            return role;
        }

        public @Nullable String formatted(final K key) {
            final Role role = this.get(key);
            return role == null ? null : role.getAsMention();
        }

        @Override
        public Role setFormatted(final K key, final @NotNull String str) throws IllegalArgumentException {
            final Role role = this.putFromString(key, Utils.stripRoleID(str));
            this.save(key);
            return role;
        }
    }
}
