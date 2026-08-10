package com.catadmirer.infuseSMP.bukkit.listeners;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.catadmirer.infuseSMP.bukkit.platform.PaperPlayer;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerJoinListener implements Listener {
    private final InfusePlugin plugin;

    public PlayerJoinListener(InfusePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void giveRecipes(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player all the infuse recipes
        plugin.getEffectRegistry().getRegisteredEffects().stream().map(plugin.getRecipeManager()::getRecipeKey).forEach(player::discoverRecipe);
    }

    @EventHandler
    public void tellControlMode(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Telling the player their current control mode
        String controlMode = plugin.getDataManager().getControlMode(player.getUniqueId());
        if (controlMode == null) controlMode = "Offhand";
        boolean offhandEnabled = controlMode.equalsIgnoreCase("Offhand");
        player.addAttachment(plugin, "ability.use", !offhandEnabled);

        Message msg = new Message(MessageType.JOIN_ABILITY_NOTIFY);
        msg.applyPlaceholder("control_mode", controlMode);
        player.sendMessage(msg.toComponent());
    }

    /** Activates the player's effects and assigns them a starting effect if they haven't played before. */
    @EventHandler
    public void activateEffects(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Giving the player their starting effects if they haven't joined before
        if (plugin.getMainConfig().joinEffectsEnabled() && !player.getPersistentDataContainer().has(InfusePlugin.JOIN_EFFECT_KEY)) {
            plugin.getEffectManager().giveJoinEffect(player);
            player.getPersistentDataContainer().set(InfusePlugin.JOIN_EFFECT_KEY, PersistentDataType.BOOLEAN, true);
            return;
        }

        // Enabling each effect
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
        if (effect != null && !plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), effect)) effect.equip(new PaperPlayer(player));

        effect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");
        if (effect != null && !plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), effect)) effect.equip(new PaperPlayer(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getRitualManager().isActive()) return;

        //noinspection DataFlowIssue
        event.getPlayer().showBossBar(plugin.getRitualManager().getBossBar());
    }
}
