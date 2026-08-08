package com.catadmirer.infuseSMP.bukkit;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.InfuseProvider;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.MessageTranslator;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.bukkit.commands.*;
import com.catadmirer.infuseSMP.bukkit.effects.*;
import com.catadmirer.infuseSMP.bukkit.extraeffects.*;
import com.catadmirer.infuseSMP.bukkit.listeners.*;
import com.catadmirer.infuseSMP.bukkit.managers.*;
import com.catadmirer.infuseSMP.bukkit.placeholders.InfusePlaceholders;
import com.catadmirer.infuseSMP.bukkit.util.regions.BasicRegionBlocker;
import com.catadmirer.infuseSMP.bukkit.util.regions.DualRegionBlocker;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import com.catadmirer.infuseSMP.util.RegionBlocker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class InfusePlugin extends JavaPlugin implements Infuse {
    private final MainConfig mainConfig;
    private final RegionBlocker regionBlocker;

    private final DataManager dataManager;
    private final EffectManager effectManager;
    private final GlobalLoop loop;
    private final RecipeManager recipeManager;
    private final HitTracker hitTracker;

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

        this.dataManager = new DataManager(this);
        this.effectManager = new EffectManager(this);
        this.loop = new GlobalLoop(this);
        this.recipeManager = new RecipeManager(this);
        this.hitTracker = new HitTracker(this);

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
        getCommand("trust").setExecutor(new TrustCommand(dataManager));
        getCommand("untrust").setExecutor(new TrustCommand(dataManager));
        getCommand("recipes").setExecutor(new Recipes(this));
        getCommand("swap").setExecutor(new SwapEffects(this));

        getCommand("infuse").setExecutor(new InfuseCommand(this));
        getCommand("infuse").setTabCompleter(new InfuseCommand(this));

        getCommand("ldrain").setExecutor(new DrainCommand(this));
        getCommand("rdrain").setExecutor(new DrainCommand(this));

        getCommand("rspark").setExecutor(new Abilities(this));
        getCommand("lspark").setExecutor(new Abilities(this));

        getCommand("draw").setExecutor(new Draw());

        getCommand("controls").setExecutor((sender, a, b, args) -> {
            // Making sure only players can run the command
            if (!(sender instanceof Player player)) {
                sender.sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                return true;
            }

            // Making sure the command has an argument
            if (args.length != 1) {
                player.sendMessage(new Message(MessageType.CONTROLS_USAGE).toComponent());
                return true;
            }

            // Getting the selected control mode
            String choice = args[0].toLowerCase();

            // Validating the control mode string
            if (!choice.equals("offhand") && !choice.equals("command")) {
                player.sendMessage(new Message(MessageType.CONTROLS_INVALID_PARAM).toComponent());
                return true;
            }

            // Setting the control mode for the player
            dataManager.setControlMode(player.getUniqueId(), choice);
            player.addAttachment(this, "ability.use", choice.equals("command"));
            return true;
        });
        getCommand("controls").setTabCompleter((a, b, c, args) -> {
            if (args.length == 1) {
                return Stream.of("command", "offhand").filter(opt -> opt.startsWith(args[0])).toList();
            }

            return List.of();
        });
    }

    public void onDisable() {
        // Stopping the passive effect loop
        loop.stop();

        // Sending the log message
        Infuse.LOGGER.info("Infuse Plugin is disabling...");

        // Removing ritual beams
        EffectCraftManager.removeBeam();

        // Finalizing the message
        Infuse.LOGGER.info("Infuse Plugin has been disabled!");
    }

    private void registerEvents() {
        // Initializing the hit tracker
        Bukkit.getPluginManager().registerEvents(hitTracker, this);

        // Registering events for all the listeners
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
        InfuseEffect.register(new Emerald());
        InfuseEffect.register(new Ender());
        InfuseEffect.register(new Feather());
        InfuseEffect.register(new Fire());
        InfuseEffect.register(new Frost());
        InfuseEffect.register(new Haste());
        InfuseEffect.register(new Heart());
        InfuseEffect.register(new Invis());
        InfuseEffect.register(new Ocean());
        InfuseEffect.register(new Regen());
        InfuseEffect.register(new Speed());
        InfuseEffect.register(new Strength());
        InfuseEffect.register(new Thunder());

        if (mainConfig.enableApophis()) InfuseEffect.register(new Apophis());
        if (mainConfig.enableThief()) InfuseEffect.register(new Thief());
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

    public DataManager getDataManager() {
        return dataManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public HitTracker getHitTracker() {
        return hitTracker;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
}
