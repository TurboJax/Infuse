package com.catadmirer.infuseSMP;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.Set;

@NullMarked
public class MessageTranslator {
    public static final Set<Locale> SUPPORTED_LOCALES = Set.of(Locale.US, Locale.of("es"));

    private final Infuse plugin = Infuse.getInstance();

    @Nullable
    public String translate(String key) {
        // Getting the locale from the config
        Locale locale = plugin.getMainConfig().lang();

        // Defaulting to the en_US locale
        if (!SUPPORTED_LOCALES.contains(locale)) {
            Infuse.LOGGER.warn("Locale \"{}\" not recognized.  Falling back to en_US.", locale);
            locale = Locale.US;
        }

        // Getting the translation
        return getLocale(locale).getString(key.toLowerCase());
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
