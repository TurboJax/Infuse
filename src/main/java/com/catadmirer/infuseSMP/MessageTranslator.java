package com.catadmirer.infuseSMP;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Set;

@NullMarked
public class MessageTranslator implements Translator {
    // Mostly here just for looks
    // Doesn't really do anything
    public static final Set<Locale> SUPPORTED_LOCALES = Set.of(Locale.US);

    private final Infuse plugin;

    public MessageTranslator(Infuse plugin) {
        this.plugin = plugin;

        GlobalTranslator.translator().addSource(this);
    }

    @Override
    public Key name() {
        return Key.key("infuse:main");
    }

    @Override
    @Nullable
    public MessageFormat translate(String key, Locale locale) {
        // Defaulting to the en_US locale
        if (!SUPPORTED_LOCALES.contains(locale)) locale = Locale.US;

        FileConfiguration config = getLocale(locale);
        String fmt = config.getString(key.toLowerCase());

        if (fmt == null) return null;

        return new MessageFormat(fmt, locale);
    }

    public void loadAll() {
        SUPPORTED_LOCALES.forEach(this::loadLocale);
    }

    public void loadLocale(Locale locale) {
        plugin.saveResource("lang/base/" + locale + ".yml", true);
    }

    public FileConfiguration getLocale(Locale locale) {
        File baseLocaleFile = new File(plugin.getDataFolder(), "lang/base/" + locale + ".yml");
        File customLocaleFile = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");

        // Loading base translations
        FileConfiguration translations = YamlConfiguration.loadConfiguration(baseLocaleFile);

        // Loading custom translations
        if (!customLocaleFile.exists()) return translations;

        FileConfiguration custom = YamlConfiguration.loadConfiguration(customLocaleFile);

        for (String key : custom.getKeys(true)) {
            translations.set(key, custom.get(key));
        }

        return translations;
    }
}
