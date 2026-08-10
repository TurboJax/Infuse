package com.catadmirer.infuseSMP.bukkit;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MainConfig implements com.catadmirer.infuseSMP.MainConfig {
    public final File file;
    public final FileConfiguration config;
    public final InfusePlugin plugin;

    public MainConfig(InfusePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Reloads the configuration.
     *
     * @return Whether the configuration was loaded successfully or not.
     */
    public boolean load() {
        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(file.getName(), true);
        }

        // Loading the config
        try {
            config.load(file);
            Infuse.LOGGER.info("Successfully loaded {}", file.getName());
            return true;
        } catch (InvalidConfigurationException e) {
            Infuse.LOGGER.warn("{} contains an invalid YAML configuration.  Verify the contents of the file.", file.getName());
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not find {}.  Check that it exists.", file.getName(), e);
        }

        return false;
    }

    /**
     * Writes the config to the file.
     *
     * @return Whether the config was successfully written or not.
     */
    public boolean save() {
        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        // Saving the config
        try {
            config.save(file);
            Infuse.LOGGER.info("Saved {}", file.getName());
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.warn("Could not save {}.  Make sure the user has write permissions.", file.getName());
        }

        return false;
    }

    @Override
    public List<Key> getBlacklistedWorlds(InfuseEffect effect) {
        return config.getStringList(effect.plainKey() + ".blacklisted-worlds")
            .stream()
            .filter(Objects::nonNull)
            .map(Key::key)
            .toList();
    }

    @Override
    public String lang() {
        return config.getString("lang", "en_US");
    }

    @Override
    public boolean allowInfiniteEffects() {
        return config.getBoolean("allow_infinite_effects");
    }

    public boolean emptyEffectIcon() {
        return config.getBoolean("empty_effect_icon");
    }

    @Override
    public boolean playerHeadDrops() {
        return config.getBoolean("player_head_drops");
    }

    @Override
    public int ritualDuration() {
        return config.getInt("rituals.duration", 600);
    }

    @Override
    public int ritualDurationEnder() {
        return config.getInt("rituals.ender_duration", 3600);
    }

    @Override
    public boolean regularBroadcast() {
        return config.getBoolean("rituals.broadcast_regular", true);
    }

    @Override
    public boolean enableDiscordBroadcasts() {
        return config.getBoolean("rituals.send_webhooks", false);
    }

    @Override
    public String discordWebhookUrl() {
        return config.getString("rituals.webhook_url", "");
    }

    @Override
    public boolean ritualBeacon() {
        return config.getBoolean("rituals.beacon", true);
    }

    @Override
    public boolean useImmortalBrewers() {
        return config.getBoolean("rituals.immortal_brewing_stands", true);
    }

    @Override
    public boolean brewingGui() {
        return config.getBoolean("brewing_gui");
    }

    @Override
    public String effectDrops() {
        return config.getString("effect_drops");
    }

    @Override
    public boolean joinEffectsEnabled() {
        return config.getBoolean("join_effects_enabled");
    }

    @Override
    public List<InfuseEffect> joinEffects() {
        return config.getStringList("join_effects").stream().map(plugin.getEffectRegistry()::fromKey).map(e -> (InfuseEffect) e).filter(Objects::nonNull).toList();
    }

    @Override
    public boolean enableApophis() {
        return config.getBoolean("extra_effects.Apophis");
    }

    @Override
    public boolean enableThief() {
        return config.getBoolean("extra_effects.Thief");
    }

    /**
     * Gets the amount of each effect that can be crafted
     *
     * @param effect The effect to check
     *
     * @return The number of effects that can be crafted of the specified {@link InfuseEffect}.
     */
    @Override
    public int getCraftLimit(InfuseEffect effect) {
        List<Integer> craftLimits = config.getIntegerList("craft_limits." + effect.plainKey());

        if (craftLimits.size() != 2) {
            Infuse.LOGGER.error("Craft limits are required to be a list of 2 integers.  Found {} entries for effect {}", craftLimits.size(), effect.plainKey());
            Infuse.LOGGER.error("Returning default limits");

            return effect.augmented() ? 1 : 3;
        }

        return craftLimits.get(effect.augmented() ? 0 : 1);
    }

    @Override
    public double emeraldLockDurationSeconds() {
        return config.getDouble("emerald.lock_duration_seconds", 10);
    }

    @Override
    public boolean invisHideKills() {
        return config.getBoolean("invis.hide_kills");
    }

    @Override
    public boolean invisHideDeaths() {
        return config.getBoolean("invis.hide_deaths");
    }

    @Override
    public long cooldown(InfuseEffect effect) {
        return config.getLong(effect.plainKey() + ".cooldown." + (effect.augmented() ? "augmented" : "default"));
    }

    @Override
    public long duration(InfuseEffect effect) {
        return config.getLong(effect.plainKey() + ".duration." + (effect.augmented() ? "augmented" : "default"));
    }

    @Override
    public int speedDashMultiplier() {
        return config.getInt("speed.dashMultiplier");
    }

    @Override
    public int speedPlayerVelocityMultiplier() {
        return config.getInt("speed.playerVelocityMultiplier");
    }

    @Override
    public int oceanPullInterval() {
        return config.getInt("ocean_pulling.pull.interval");
    }

    @Override
    public int oceanPullRadius() {
        return config.getInt("ocean_pulling.pull.radius");
    }

    @Override
    public double oceanPullStrength() {
        return config.getDouble("ocean_pulling.pull.strength");
    }

    @Override
    public int hitCounterDecaySeconds() {
        return config.getInt("hit_counter_decay_seconds");
    }

    @Override
    public int emeraldExpPerHit() {
        return config.getInt("emerald.xp_stolen_per_hit");
    }

    @Override
    public float emeraldExpPercent() {
        return Math.clamp((float) config.getDouble("emerald.xp_stolen_percent"), 0, 1);
    }

    @Override
    public float emeraldPercentExpToShare() {
        return Math.clamp((float) config.getDouble("emerald.percent_xp_to_share"), 0, 1);
    }

    @Override
    public int apophisExpPerHit() {
        return config.getInt("apophis.xp_stolen_per_hit");
    }

    @Override
    public float apophisExpPercent() {
        return Math.clamp((float) config.getDouble("apophis.xp_stolen_percent"), 0, 1);
    }

    @Override
    public float apophisPercentExpToShare() {
        return Math.clamp((float) config.getDouble("apophis.percent_xp_to_share"), 0, 1);
    }

    @Override
    public double apophisLockDurationSeconds() {
        return config.getDouble("apophis.lock_duration_seconds", 10);
    }

    @Override
    public int apophisLootingLevel() {
        return config.getInt("apophis.enchantment.looting_level");
    }

    @Override
    public double apophisSparkRadius() {
        return config.getDouble("apophis.spark.radius", 5);
    }

    @Override
    public double apophisSparkExplosionRadius() {
        return config.getDouble("apophis.spark.explosion-radius", 5);
    }

    @Override
    public double apophisLavaWalkSpeed() {
        return config.getDouble("apophis.passive.walk-speed", 0.6);
    }

    @Override
    public int apophisXpMultiplierStandard() {
        return config.getInt("apophis.multiplier-xp.standard", 2);
    }

    @Override
    public int apophisXpMultiplierSpark() {
        return config.getInt("apophis.multiplier-xp.use-effect", 4);
    }

    @Override
    public int emeraldLootingLevel() {
        return config.getInt("emerald.enchantment.looting_level");
    }

    @Override
    public int hasteFortuneLevel() {
        return config.getInt("haste.enchantment.fortune_level");
    }

    @Override
    public int hasteEfficiencyLevel() {
        return config.getInt("haste.enchantment.efficiency_level");
    }

    @Override
    public int hasteUnbreakingLevel() {
        return config.getInt("haste.enchantment.unbreaking_level");
    }

    @Override
    public double emeraldMultiplierStandard() {
        return config.getDouble("emerald.multiplier-xp.standard");
    }

    @Override
    public double emeraldMultiplierUseEffect() {
        return config.getDouble("emerald.multiplier-xp.use-effect");
    }

    @Override
    public double enderPassiveRadius() {
        return config.getDouble("ender.passive.radius");
    }

    @Override
    public int enderSparkMaxDistance() {
        return config.getInt("ender.spark.max-distance");
    }

    @Override
    public double featherLandRadius() {
        return config.getDouble("feather.land.radius");
    }

    @Override
    public double featherLandDamage() {
        return config.getDouble("feather.land.damage");
    }

    @Override
    public double firePassiveWalkSpeed() {
        return config.getDouble("fire.passive.walk-speed");
    }

    @Override
    public double fireSparkRadius() {
        return config.getDouble("fire.spark.radius");
    }

    @Override
    public double fireSparkExplosionRadius() {
        return config.getDouble("fire.spark.explosion-radius");
    }

    @Override
    public int frostPassiveSnowChangingRadius() {
        return config.getInt("frost.passive.snow-changing-radius");
    }

    @Override
    public double frostPassiveWalkSpeed() {
        return config.getDouble("frost.passive.walk-speed");
    }

    @Override
    public double frostSparkRadius() {
        return config.getDouble("frost.spark.radius");
    }

    @Override
    public int oceanPassiveDrownStrength() {
        return config.getInt("ocean.passive.drown-strength");
    }

    @Override
    public int oceanPassiveDrownDamage() {
        return config.getInt("ocean.passive.drown-damage");
    }

    @Override
    public int oceanSparkDrownStrength() {
        return config.getInt("ocean.spark.drown-strength");
    }

    @Override
    public int oceanSparkDrownDamage() {
        return config.getInt("ocean.spark.drown-damage");
    }

    @Override
    public double regenSparkHealTrustedRadius() {
        return config.getDouble("regen.spark.heal-trusted-radius");
    }

    @Override
    public double thunderSparkBaseRadius() {
        return config.getDouble("thunder.spark.base-radius");
    }

    @Override
    public double thunderSparkPerPlayerBoostRadius() {
        return config.getDouble("thunder.spark.per-player-boost-radius");
    }

    public void applyUpdates() {
        if (config.contains("ritual_duration")) {
            config.set("rituals.duration", config.get("ritual_duration"));
            config.set("rituals.ender_duration", config.get("ritual_duration_ender"));
            config.set("rituals.broadcast_regular", config.get("regular_effect_broadcast"));
            config.set("rituals.send_webhooks", config.get("enable_discord_broadcasts"));
            config.set("rituals.webhook_url", config.get("discord_webhook_url"));
            config.set("rituals.beacon", config.get("ritual_beacon"));
            config.set("rituals.immortal_brewing_stands", true);
        }

        if (!config.contains("invis_deaths")) config.set("invis_deaths", null);
        if (!config.contains("invis.hide_kills")) config.set("invis.hide_kills", false);
        if (!config.contains("invis.hide_deaths")) config.set("invis.hide_deaths", false);

        if (!config.contains("haste.enchantment.looting_level")) config.set("haste.enchantment.looting_level", 5);
        if (!config.contains("haste.enchantment.fortune_level")) config.set("haste.enchantment.fortune_level", 5);
        if (!config.contains("haste.enchantment.efficiency_level")) config.set("haste.enchantment.efficiency_level", 10);
        if (!config.contains("haste.enchantment.unbreaking_level")) config.set("haste.enchantment.unbreaking_level", 5);

        if (!config.contains("hit_counter_decay_seconds")) config.set("hit_counter_decay_seconds", 15);

        if (!config.contains("emerald.xp_stolen_per_hit")) config.set("emerald.xp_stolen_per_hit", 15);
        if (!config.contains("emerald.xp_stolen_percent")) config.set("emerald.xp_stolen_percent", 1);
        if (!config.contains("emerald.percent_xp_to_share")) config.set("emerald.percent_xp_to_share", 0.5);

        if (!config.contains("apophis.percent_xp_to_share")) config.set("apophis.percent_xp_to_share", 0.5);
        if (!config.contains("apophis.xp_stolen_per_hit")) config.set("apophis.xp_stolen_per_hit", 15);
        if (!config.contains("apophis.xp_stolen_percent")) config.set("apophis.xp_stolen_percent", 1);
        if (!config.contains("apophis.enchantment.looting_level")) config.set("apophis.enchantment.looting_level", 5);
        if (!config.contains("apophis.lock_duration_seconds")) config.set("apophis.lock_duration_seconds", 10);

        if (!config.contains("emerald.multiplier-xp.standard")) config.set("emerald.multiplier.standard", 2);
        if (!config.contains("emerald.multiplier-xp.use-effect")) config.set("emerald.multiplier.use-effect", 4);

        if (!config.contains("ender.passive.radius")) config.set("ender.passive.radius", 10);
        if (!config.contains("ender.spark.max-distance")) config.set("ender.spark.max-distance", 15);

        if (!config.contains("feather.land.radius")) config.set("feather.land.radius", 4);
        if (!config.contains("feather.land.damage")) config.set("feather.land.damage", 8);

        if (!config.contains("fire.passive.walk-speed")) config.set("fire.passive.walk-speed", 0.6);
        if (!config.contains("fire.spark.radius")) config.set("fire.spark.radius", 5);
        if (!config.contains("fire.spark.explosion-radius")) config.set("fire.spark.explosion-radius", 5);

        if (!config.contains("frost.passive.snow-changing-radius")) config.set("frost.passive.snow-changing-radius", 3);
        if (!config.contains("frost.passive.walk-speed")) config.set("frost.passive.walk-speed", 0.6);
        if (!config.contains("frost.spark.radius")) config.set("frost.spark.radius", 5);

        if (!config.contains("ocean.passive.drown-strength")) config.set("ocean.passive.drown-strength", 5);
        if (!config.contains("ocean.passive.drown-damage")) config.set("ocean.passive.drown-damage", 1);
        if (!config.contains("ocean.spark.drown-strength")) config.set("ocean.spark.drown-strength", 20);
        if (!config.contains("ocean.spark.drown-damage")) config.set("ocean.spark.drown-damage", 2);

        if (!config.contains("regen.spark.heal-trusted-radius")) config.set("regen.spark.heal-trusted-radius", 5);

        if (!config.contains("thunder.spark.base-radius")) config.set("thunder.spark.base-radius", 10);
        if (!config.contains("thunder.spark.per-player-boost-radius")) config.set("thunder.spark.per-player-boost-radius", 0.3);

        if (!config.contains("apophis.spark.radius")) config.set("apophis.spark.radius", 5);
        if (!config.contains("apophis.spark.explosion-radius")) config.set("apophis.spark.explosion-radius", 5);
        if (!config.contains("apophis.passive.walk-speed")) config.set("apophis.passive.walk-speed", 0.6);
        if (!config.contains("apophis.multiplier-xp.standard")) config.set("apophis.multiplier-xp.standard", 2);
        if (!config.contains("apophis.multiplier-xp.use-effect")) config.set("apophis.multiplier-xp.use-effect", 4);

        if (!config.contains("apophis.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("thief.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("emerald.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("ender.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("feather.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("fire.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("frost.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("haste.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("heart.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("invis.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("ocean.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("regen.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("speed.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("strength.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());
        if (!config.contains("thunder.blacklisted-worlds")) config.set("apophis.blacklisted-worlds", List.of());

        save();
    }
}
