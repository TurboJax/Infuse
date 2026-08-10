package com.catadmirer.infuseSMP.bukkit;

import com.catadmirer.infuseSMP.bukkit.effects.Heart;
import com.catadmirer.infuseSMP.bukkit.extraeffects.Apophis;
import com.catadmirer.infuseSMP.bukkit.managers.ParticleManager;
import com.catadmirer.infuseSMP.bukkit.platform.PaperPlayer;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GlobalLoop extends BukkitRunnable {
    private final InfusePlugin plugin;

    private static final HashSet<UUID> lEffectDisabled = new HashSet<>();
    private static final HashSet<UUID> rEffectDisabled = new HashSet<>();

    public GlobalLoop(InfusePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.runTaskTimer(plugin, 0, 20);
    }

    public void stop() {
        this.cancel();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            com.catadmirer.infuseSMP.platform.Player platformPlayer = new PaperPlayer(player);
            // Getting the player's equipped effects
            final InfuseEffect lEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "1");
            final InfuseEffect rEffect = plugin.getDataManager().getEffect(player.getUniqueId(), "2");

            ParticleManager.spawnEffectParticles(player, lEffect);
            ParticleManager.spawnEffectParticles(player, rEffect);
            ParticleManager.spawnCursedParticles(player);

            // Applying passive effects to the player
            if (lEffect != null) {
                final boolean shouldBlock = plugin.getRegionBlocker().isEffectBlocked(platformPlayer, lEffect);
                boolean isBlocked = lEffectDisabled.contains(player.getUniqueId());

                if (shouldBlock && !isBlocked) {
                    lEffect.unequip(platformPlayer);
                    lEffectDisabled.add(player.getUniqueId());
                    isBlocked = true;
                } else if (!shouldBlock && isBlocked) {
                    lEffect.equip(platformPlayer);
                    lEffectDisabled.remove(player.getUniqueId());
                    isBlocked = false;
                }

                if (!isBlocked) lEffect.applyPassives(platformPlayer);
            }

            // Applying passive effects to the player
            if (rEffect != null) {
                final boolean shouldBlock = plugin.getRegionBlocker().isEffectBlocked(platformPlayer, rEffect);
                boolean isBlocked = rEffectDisabled.contains(player.getUniqueId());
                if (shouldBlock && !isBlocked) {
                    rEffect.unequip(platformPlayer);
                    rEffectDisabled.add(player.getUniqueId());
                    isBlocked = true;
                } else if (!shouldBlock && isBlocked) {
                    rEffect.equip(platformPlayer);
                    rEffectDisabled.remove(player.getUniqueId());
                    isBlocked = false;
                }

                if (!isBlocked) rEffect.applyPassives(platformPlayer);
            }

            // Making sure the apophis boost has been removed
            if (!plugin.getDataManager().hasEffect(player, new Apophis())) {
                AttributeInstance playerHealth = player.getAttribute(Attribute.MAX_HEALTH);
                playerHealth.removeModifier(Apophis.APOPHIS_BOOST);
            }

            // Making sure the heart boost has been removed
            if (!plugin.getDataManager().hasEffect(player, new Heart())) {
                AttributeInstance playerHealth = player.getAttribute(Attribute.MAX_HEALTH);
                playerHealth.removeModifier(Heart.heartBoost);
            }
        }
    }
}
