package com.catadmirer.infuseSMP.bukkit.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.bukkit.platform.PaperPlayer;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class Ocean extends BukkitEffect {
    public Ocean() {
        this(false);
    }

    public Ocean(boolean augmented) {
        super("ocean", EffectConstants.Id.OCEAN, augmented, EffectConstants.PotionColor.OCEAN, EffectConstants.RitualColor.OCEAN, EffectConstants.BackgroundColor.OCEAN);
    }

    @Override
    public void equip(com.catadmirer.infuseSMP.platform.Player owner) {
        if (plugin.getRegionBlocker().isEffectBlocked(owner, this)) return;
        Player player = PaperPlayer.toBukkit(owner);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, -1, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, -1, 0, false, false));
    }

    @Override
    public void unequip(com.catadmirer.infuseSMP.platform.Player owner) {
        Player player = PaperPlayer.toBukkit(owner);
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
    }

    @Override
    public void applyPassives(com.catadmirer.infuseSMP.platform.Player owner) {
        // Boosting the strength and damage of the passive drowning if the spark is active
        if (plugin.getRegionBlocker().isEffectBlocked(owner, this)) return;
        Player player = PaperPlayer.toBukkit(owner);

        int drownStrength = plugin.getMainConfig().oceanPassiveDrownStrength();
        int drownDamage = plugin.getMainConfig().oceanPassiveDrownDamage();
        if (CooldownManager.isEffectActive(owner.getUniqueId(), "ocean"))  {
            drownStrength = plugin.getMainConfig().oceanSparkDrownStrength();
            drownDamage = plugin.getMainConfig().oceanSparkDrownDamage();
        }

        // TODO: Make this use packets for air bubbles
        for (Player otherPlayer : player.getWorld().getPlayers()) {
            if (otherPlayer.equals(player)) continue;
            if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(otherPlayer), this)) continue;
            if (otherPlayer.getLocation().distance(player.getLocation()) > 5) continue;

            int newAir = Math.max(otherPlayer.getRemainingAir() - drownStrength, -20);
            otherPlayer.setRemainingAir(newAir);
            if (newAir <= 0) {
                otherPlayer.damage(drownDamage);
            }
        }
    }

    @Override
    public void activateSpark(com.catadmirer.infuseSMP.platform.Player owner) {
        UUID playerUUID = owner.getUniqueId();
        Player player = PaperPlayer.toBukkit(owner);

        if (CooldownManager.isOnCooldown(playerUUID, "ocean")) return;
        if (!plugin.getRegionBlocker().canUseSpark(owner)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(owner, Ocean.this)) return;

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);

        final double radius = 5;
        final World world = player.getWorld();
        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "ocean", duration, cooldown);

        final long durationTicks = duration * 20L;

        new BukkitRunnable() {
            long ticksElapsed = 0L;

            public void run() {
                if (this.ticksElapsed >= durationTicks) {
                    this.cancel();
                    return;
                }

                for (int angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    double x = owner.getLocation().getX() + radius * Math.cos(rad);
                    double z = owner.getLocation().getZ() + radius * Math.sin(rad);
                    Location particleLoc = new Location(world, x, owner.getLocation().getY(), z);
                    world.spawnParticle(Particle.FALLING_WATER, particleLoc, 1);
                }

                this.ticksElapsed += 10L;
            }
        }.runTaskTimer(plugin, 0L, 10L);

        // Ocean pull runnable
        new BukkitRunnable() {
            @Override
            public void run() {
                // Stopping when the spark has run out
                if (!CooldownManager.isEffectActive(owner.getUniqueId(), "ocean")) {
                    cancel();
                    return;
                }

                World world = player.getWorld();
                Location holderLoc = player.getLocation();
                double radius = plugin.getMainConfig().oceanPullRadius();
                double strength = plugin.getMainConfig().oceanPullStrength();

                for (Player p : world.getPlayers()) {
                    if (p.equals(player)) continue;
                    if (plugin.getDataManager().isTrusted(player, p)) continue;
                    if (p.getLocation().distance(holderLoc) > radius) continue;
                    if (!plugin.getRegionBlocker().canBeTargetedBySpark(new PaperPlayer(p))) continue;
                    if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(p), Ocean.this)) continue;

                    Vector direction = holderLoc.toVector().subtract(p.getLocation().toVector());
                    if (direction.lengthSquared() > 0.0001) {
                        Vector pullVector = direction.normalize().multiply(strength);
                        if (Double.isFinite(pullVector.getX()) && Double.isFinite(pullVector.getY()) && Double.isFinite(pullVector.getZ())) {
                            p.setVelocity(pullVector);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, plugin.getMainConfig().oceanPullInterval());
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Ocean();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Ocean(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? Message.MessageType.AUG_OCEAN_NAME : Message.MessageType.OCEAN_NAME);
    }

    @Override
    public Message lore() {
        return new Message(augmented ? Message.MessageType.AUG_OCEAN_LORE : Message.MessageType.OCEAN_LORE);
    }
}
