package com.catadmirer.infuseSMP.bukkit.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.bukkit.events.EffectEquipEvent;
import com.catadmirer.infuseSMP.bukkit.events.TenHitEvent;
import com.catadmirer.infuseSMP.bukkit.platform.PaperLocation;
import com.catadmirer.infuseSMP.bukkit.platform.PaperPlayer;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Frost extends BukkitEffect {
    private final static Set<UUID> frozenAttackers = new HashSet<>();
    private final static Set<Location> frozenSnow = new HashSet<>();

    public Frost() {
        this(false);
    }

    public Frost(boolean augmented) {
        super("frost", EffectConstants.Id.FROST, augmented, EffectConstants.PotionColor.FROST, EffectConstants.RitualColor.FROST, EffectConstants.BackgroundColor.FROST);
    }

    @Override
    public void equip(com.catadmirer.infuseSMP.platform.Player owner) {
        if (plugin.getRegionBlocker().isEffectBlocked(owner, this)) return;
        Player player = PaperPlayer.toBukkit(owner);
        changeToSnow(player);
    }

    @Override
    public void unequip(com.catadmirer.infuseSMP.platform.Player owner) {}

    @Override
    public void applyPassives(com.catadmirer.infuseSMP.platform.Player owner) {
        if (plugin.getRegionBlocker().isEffectBlocked(owner, this)) return;
        Player player = PaperPlayer.toBukkit(owner);

        if (!(player.getVelocity().lengthSquared() < 0.01)) {
            if (player.isInPowderedSnow()) {
                player.setGliding(true);
            }

            Material blockType = player.getLocation().subtract(0, 1, 0).getBlock().getType();
            if (MaterialSetTag.ICE.isTagged(blockType)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 2, false, false));
            }
        }
    }

    @Override
    public void activateSpark(com.catadmirer.infuseSMP.platform.Player owner) {
        UUID playerUUID = owner.getUniqueId();
        Player player = PaperPlayer.toBukkit(owner);

        if (CooldownManager.isOnCooldown(playerUUID, "frost")) return;
        if (!plugin.getRegionBlocker().canUseSpark(owner)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(owner, this)) return;


        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 300, 0));

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "frost", duration, cooldown);

        Location center = player.getLocation();
        final double radius = plugin.getMainConfig().frostSparkRadius();
        World world = player.getWorld();
        final Set<Player> affectedPlayers = new HashSet<>();

        for (Player p : world.getPlayers()) {
            if (p.equals(owner)) continue;
            if (plugin.getDataManager().isTrusted(p, player)) continue;
            if (p.getLocation().distance(center) > radius) continue;
            if (!plugin.getRegionBlocker().canBeTargetedBySpark(new PaperPlayer(p))) continue;
            if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(p), Frost.this)) continue;

            affectedPlayers.add(p);
            AttributeInstance jumpAttribute = p.getAttribute(Attribute.JUMP_STRENGTH);
            if (jumpAttribute != null) {
                jumpAttribute.setBaseValue(0.1);
            }
        }

        frozenAttackers.add(owner.getUniqueId());

        new BukkitRunnable() {
            public void run() {
                for (Player player : affectedPlayers) {
                    AttributeInstance jumpAttribute = player.getAttribute(Attribute.JUMP_STRENGTH);
                    if (jumpAttribute != null) {
                        jumpAttribute.setBaseValue(0.42);
                    }
                }
                frozenAttackers.remove(owner.getUniqueId());
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Frost();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Frost(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? Message.MessageType.AUG_FROST_NAME : Message.MessageType.FROST_NAME);
    }

    @Override
    public Message lore() {
        return new Message(augmented ? Message.MessageType.AUG_FROST_LORE : Message.MessageType.FROST_LORE);
    }

    public void changeToSnow(Player player) {
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), this)) return;

        final int frostSnowRadius = plugin.getMainConfig().frostPassiveSnowChangingRadius();
        Location center = player.getLocation();

        for (int dx = -frostSnowRadius; dx <= frostSnowRadius; dx++) {
            for (int dy = -frostSnowRadius; dy <= frostSnowRadius; dy++) {
                for (int dz = -frostSnowRadius; dz <= frostSnowRadius; dz++) {
                    // Getting the block in the radius
                    Block powderSnowBlock = center.toBlockLocation().add(dx, dy, dz).getBlock();

                    // Skipping non-powdered snow blocks
                    if (powderSnowBlock.getType() != Material.POWDER_SNOW) continue;

                    // Skipping if there is a block above this one
                    if (powderSnowBlock.getRelative(BlockFace.UP).getType() != Material.AIR) continue;

                    // Skipping if the block's location is in a blocked region.
                    if (plugin.getRegionBlocker().isEffectBlocked(new PaperLocation(powderSnowBlock.getLocation()), this)) return;

                    // Changing the block to regular snow
                    powderSnowBlock.setType(Material.SNOW_BLOCK);
                    frozenSnow.add(powderSnowBlock.getLocation());

                    Bukkit.getScheduler().runTaskTimer(plugin, task -> {
                        // Skipping if the player is too close to the block
                        if (powderSnowBlock.getLocation().distance(player.getLocation()) <= frostSnowRadius) return;
                        // Skipping if the player has broke the snow block when it has been changed
                        if (!(powderSnowBlock.getType().equals(Material.SNOW_BLOCK))) return;

                        // Resetting the block to powdered snow
                        powderSnowBlock.setType(Material.POWDER_SNOW);
                        frozenSnow.remove(powderSnowBlock.getLocation());
                        task.cancel();
                    }, 10, 10);
                }
            }
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (!(event.getPlugin().getName().equalsIgnoreCase(plugin.getName()))) return;
        frozenSnow.forEach(snow -> snow.getBlock().setType(Material.POWDER_SNOW));
    }

    //// Listeners ////
    //// These are only registered once, so they need to be able to handle being used for every player, no matter what effects they actually have

    @EventHandler
    public void onCancelSwim(EntityToggleGlideEvent event) {
        if (event.isGliding()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, this)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), this)) return;

        if (player.isInPowderedSnow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, this)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), this)) return;

        boolean inFrost = player.getLocation().getBlock().getType() == Material.POWDER_SNOW;
        Vector direction = player.getLocation().getDirection().normalize();
        if (inFrost) {
            if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;
            final double boostStrength = plugin.getMainConfig().frostPassiveWalkSpeed();
            Vector newVelocity = direction.multiply(boostStrength);
            player.setVelocity(newVelocity);
        } else {
            changeToSnow(player);
        }
    }

    @EventHandler
    public void onPlayerInteractWithWindCharge(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.WIND_CHARGE) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), this)) return;
        if (player.getFreezeTicks() <= 1) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onTenthAttack(TenHitEvent event) {
        Infuse.LOGGER.debug("[Frost] Recieved TenHitEvent");
        Infuse.LOGGER.debug("[Frost] TenHitEvent Attacker: {}", event.getAttacker().getName());
        Infuse.LOGGER.debug("[Frost] TenHitEvent Target: {}", event.getTarget().getName());

        if (!plugin.getDataManager().hasEffect(event.getAttacker(), this)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(event.getAttacker()), this)) return;

        Infuse.LOGGER.debug("[Frost] Attacker has frost effect");

        (new BukkitRunnable() {
            int ticksElapsed = 0;
            final int freezeDuration = 200;

            public void run() {
                if (this.ticksElapsed >= freezeDuration) {
                    event.getTarget().setFreezeTicks(0);
                    this.cancel();
                } else {
                    int currentFreezeTicks = event.getTarget().getFreezeTicks();
                    event.getTarget().setFreezeTicks(currentFreezeTicks + 2);
                    this.ticksElapsed += 2;
                }
            }
        }).runTaskTimer(plugin, 0L, 2L);
    }

    @EventHandler
    public void onPlayerJoin(EffectEquipEvent event) {
        // TODO: Give this a NamespacedKey and make it an AttributeModifier
        Player player = event.getPlayer();
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (jumpAttribute != null && jumpAttribute.getBaseValue() == 0.1) {
            jumpAttribute.setBaseValue(0.42);
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!attacker.hasPotionEffect(PotionEffectType.UNLUCK)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(attacker), this)) return;
        PotionEffect effect = attacker.getPotionEffect(PotionEffectType.UNLUCK);
        if (effect.getAmplifier() >= 0 && frozenAttackers.contains(attacker.getUniqueId()) && event.getEntity() instanceof Player target) {
            target.setFreezeTicks(200);
        }
    }
}
