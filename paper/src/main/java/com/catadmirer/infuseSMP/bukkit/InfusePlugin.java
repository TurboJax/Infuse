package com.catadmirer.infuseSMP.bukkit;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.InfuseProvider;
import com.catadmirer.infuseSMP.MessageTranslator;
import com.catadmirer.infuseSMP.bukkit.commands.*;
import com.catadmirer.infuseSMP.bukkit.effects.*;
import com.catadmirer.infuseSMP.bukkit.extraeffects.*;
import com.catadmirer.infuseSMP.bukkit.listeners.*;
import com.catadmirer.infuseSMP.bukkit.managers.*;
import com.catadmirer.infuseSMP.bukkit.placeholders.InfusePlaceholders;
import com.catadmirer.infuseSMP.bukkit.util.regions.BasicRegionBlocker;
import com.catadmirer.infuseSMP.bukkit.util.regions.DualRegionBlocker;

import java.io.File;

import com.catadmirer.infuseSMP.util.RegionBlocker;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class InfusePlugin extends JavaPlugin implements Infuse {
    public static final NamespacedKey JOIN_EFFECT_KEY = new NamespacedKey("infuse", "has_join_effects");
    public static final MessageComponentSerializer mcs = MessageComponentSerializer.message();

    private final MainConfig mainConfig;
    private final RegionBlocker regionBlocker;
    private final BukkitEffectRegistry effectRegistry;

    private final DataManager dataManager;
    private final EffectManager effectManager;
    private final GlobalLoop loop;
    private final RecipeManager recipeManager;
    private final HitTracker hitTracker;
    private final RitualManager ritualManager;

    @NonNull
    public static InfusePlugin getInstance() {
        return JavaPlugin.getPlugin(InfusePlugin.class);
    }

    public InfusePlugin() {
        this.mainConfig = new MainConfig(this);

        if (canUseWG()) {
            regionBlocker = new DualRegionBlocker();
            Infuse.LOGGER.info("WorldGuard found!  Enabling region-based effect management.");
        } else {
            regionBlocker = new BasicRegionBlocker();
            Infuse.LOGGER.info("WorldGuard is not installed! Using blacklisted-worlds configs");
        }

        this.effectRegistry = new BukkitEffectRegistry();

        this.dataManager = new DataManager(this);
        this.effectManager = new EffectManager(this);
        this.loop = new GlobalLoop(this);
        this.recipeManager = new RecipeManager(this);
        this.hitTracker = new HitTracker(this);
        this.ritualManager = new RitualManager();

        InfuseProvider.setInstance(this);
    }

    public void onLoad() {
        // Registering the vanilla effects
        registerEffects();

        regionBlocker.init();
    }

    public void onEnable() {
        // Loading the message translator
        new MessageTranslator().loadAll();

        // Loading the config
        mainConfig.load();

        // Loading the data manager
        dataManager.load();

        // Applying config updates
        mainConfig.applyUpdates();
        dataManager.applyUpdates();

        // Registering infuse commands
        this.registerCommands();

        // Starting the passive effect loop
        loop.start();

        // Registering event listeners for the plugin
        this.registerEvents();

        // Registering the infuse recipes
        recipeManager.registerRecipes();

        // Initializing the action bar updater
        new ActionBarUpdater(this).runTaskTimer(this, 0, 20);

        // Registering the PlaceholderAPI listener if the plugin is installed
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new InfusePlaceholders(this).register();
            Infuse.LOGGER.info("Placeholders Enabled!");
        } else {
            Infuse.LOGGER.warn("PlaceholderAPI is not installed, so custom placeholders won't work.");
        }

        // Logging the success message
        Infuse.LOGGER.info("Infuse Plugin has been enabled!");
    }

    /** Registers the commands for the plugin. */
    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, e -> {
            e.registrar().register(SparkCommand.build(this, true));
            e.registrar().register(SparkCommand.build(this, false));

            e.registrar().register(TrustCommand.build(dataManager, true));
            e.registrar().register(TrustCommand.build(dataManager, false));

            e.registrar().register(SwapCommand.build(this));

            e.registrar().register(InfuseCommand.build(this));

            e.registrar().register(DrainCommand.build(this, true));
            e.registrar().register(DrainCommand.build(this, false));

            e.registrar().register(DrawCommand.build());
        });
    }

    public void onDisable() {
        // Stopping the passive effect loop
        loop.stop();

        // Sending the log message
        Infuse.LOGGER.info("Infuse Plugin is disabling...");

        // Stopping existing rituals
        ritualManager.stopRitual();

        // Finalizing the message
        Infuse.LOGGER.info("Infuse Plugin has been disabled!");
    }

    private void registerEvents() {
        // Initializing the hit tracker
        Bukkit.getPluginManager().registerEvents(hitTracker, this);

        // Registering events for all the listeners
        Bukkit.getPluginManager().registerEvents(new PlayerSwapHandItemsListener(dataManager), this);
        Bukkit.getPluginManager().registerEvents(new CrafterCraftListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDeathListener(dataManager), this);
        Bukkit.getPluginManager().registerEvents(new EntityDropItemListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityPickupItemListener(this), this);
        Bukkit.getPluginManager().registerEvents(hitTracker, this);
        Bukkit.getPluginManager().registerEvents(new EffectCraftManager(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemDespawnListener(dataManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerItemConsumeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSwapHandItemsListener(dataManager), this);

        // Registering events for all the effects
        // TODO: Figure out a better way to do this.  Maybe something in an EffectRegistrationEvent
        Bukkit.getPluginManager().registerEvents(new Emerald(), this);
        Bukkit.getPluginManager().registerEvents(new Ender(), this);
        Bukkit.getPluginManager().registerEvents(new Feather(), this);
        Bukkit.getPluginManager().registerEvents(new Fire(), this);
        Bukkit.getPluginManager().registerEvents(new Frost(), this);
        Bukkit.getPluginManager().registerEvents(new Haste(), this);
        Bukkit.getPluginManager().registerEvents(new Heart(), this);
        Bukkit.getPluginManager().registerEvents(new Invis(), this);
        Bukkit.getPluginManager().registerEvents(new Ocean(), this);
        Bukkit.getPluginManager().registerEvents(new Regen(), this);
        Bukkit.getPluginManager().registerEvents(new Speed(), this);
        Bukkit.getPluginManager().registerEvents(new Strength(), this);
        Bukkit.getPluginManager().registerEvents(new Thunder(), this);

        // Enabling apophis listeners if the config allows
        if (mainConfig.enableApophis()) {
            getServer().getPluginManager().registerEvents(new Apophis(), this);
        }

        // Enabling thief listeners if the config allows
        if (mainConfig.enableThief()) {
            getServer().getPluginManager().registerEvents(new Thief(), this);
        }
    }

    private void registerEffects() {
        effectRegistry.register(new Emerald());
        effectRegistry.register(new Ender());
        effectRegistry.register(new Feather());
        effectRegistry.register(new Fire());
        effectRegistry.register(new Frost());
        effectRegistry.register(new Haste());
        effectRegistry.register(new Heart());
        effectRegistry.register(new Invis());
        effectRegistry.register(new Ocean());
        effectRegistry.register(new Regen());
        effectRegistry.register(new Speed());
        effectRegistry.register(new Strength());
        effectRegistry.register(new Thunder());

        if (mainConfig.enableApophis()) effectRegistry.register(new Apophis());
        if (mainConfig.enableThief()) effectRegistry.register(new Thief());
    }

    @Override
    public File getInfuseFolder() {
        return getDataFolder();
    }

    @Override
    public boolean canUseWG() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    @Override
    public String getVersion() {
        return getPluginMeta().getVersion();
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public RegionBlocker getRegionBlocker() {
        return regionBlocker;
    }

    public BukkitEffectRegistry getEffectRegistry() {
        return effectRegistry;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public HitTracker getHitTracker() {
        return hitTracker;
    }

    public RitualManager getRitualManager() {
        return ritualManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
}
