package com.catadmirer.infuseSMP.bukkit.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.bukkit.events.TenHitEvent;
import com.catadmirer.infuseSMP.bukkit.platform.PaperPlayer;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class Regen extends BukkitEffect {
    public Regen() {
        this(false);
    }

    public Regen(boolean augmented) {
        super("regen", EffectConstants.Id.REGEN, augmented, EffectConstants.PotionColor.REGEN, EffectConstants.RitualColor.REGEN, EffectConstants.BackgroundColor.REGEN);
    }

    @Override
    public void equip(com.catadmirer.infuseSMP.platform.Player owner) {
        if (plugin.getRegionBlocker().isEffectBlocked(owner, this)) return;
        Player player = PaperPlayer.toBukkit(owner);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, -1, 0, false, false));
    }

    @Override
    public void unequip(com.catadmirer.infuseSMP.platform.Player owner) {
        Player player = PaperPlayer.toBukkit(owner);
        player.removePotionEffect(PotionEffectType.REGENERATION);
    }

    @Override
    public void activateSpark(com.catadmirer.infuseSMP.platform.Player owner) {
        UUID playerUUID = owner.getUniqueId();
        Player player = PaperPlayer.toBukkit(owner);

        if (CooldownManager.isOnCooldown(playerUUID, "regen")) return;
        if (!plugin.getRegionBlocker().canUseSpark(owner)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(owner, this)) return;

        // Applying cooldowns and durations for the effect
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "regen", duration, cooldown);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Regen();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Regen(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? Message.MessageType.AUG_REGEN_NAME : Message.MessageType.REGEN_NAME);
    }

    @Override
    public Message lore() {
        return new Message(augmented ? Message.MessageType.AUG_REGEN_LORE : Message.MessageType.REGEN_LORE);
    }

    //// Listeners ////
    //// These are only registered once, so they need to be able to handle being used for every player, no matter what effects they actually have

    @EventHandler
    public void regenRegenerateOnHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, this)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), this)) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false));
        if (CooldownManager.isEffectActive(player.getUniqueId(), "regen")) {
            final double radius = plugin.getMainConfig().regenSparkHealTrustedRadius();
            for (Entity loopentity : player.getNearbyEntities(radius, radius, radius)) {
                if (loopentity instanceof Player otherplayer) {
                    if (plugin.getDataManager().isTrusted(player, otherplayer) && !plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(otherplayer), this)) {
                        otherplayer.heal(event.getDamage() / 2);
                    }
                }
            }
        }
    }

    @EventHandler
    public void consume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().hasEffect(player, this)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), this)) return;

        float sat = player.getSaturation();
        player.setSaturation(sat + 6);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void regenCanAlwaysEat(PlayerInteractEvent event) {
        if (!(event.getAction().isRightClick())) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Filtering an empty hand
        if (item == null) return;

        // Filtering inedible items
        if (!item.getType().isEdible()) return;

        // Filtering normally always edible items
        if (item.getType().getDefaultData(DataComponentTypes.FOOD).canAlwaysEat()) return;

        // Making the food always edible only if the player has the regen effect.  Makes food not always edible otherwise
        if (plugin.getDataManager().hasEffect(player, this) && !plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), this)) {
            FoodProperties properties = item.getData(DataComponentTypes.FOOD);

            // Ignoring null error because we know the item is edible.
            //noinspection DataFlowIssue
            properties = properties.toBuilder().canAlwaysEat(true).build();
            item.setData(DataComponentTypes.FOOD, properties);
        } else {
            item.resetData(DataComponentTypes.FOOD);
        }
    }

    @EventHandler
    public void onTenthAttack(TenHitEvent event) {
        if (!plugin.getDataManager().hasEffect(event.getAttacker(), this)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(event.getAttacker()), this)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(event.getTarget()), this)) return;

        int currentFood = event.getTarget().getFoodLevel();
        event.getTarget().setFoodLevel(currentFood - 2);
    }

    @EventHandler
    public void regenPreserveHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getDataManager().hasEffect(player, this)) return;
        if (plugin.getRegionBlocker().isEffectBlocked(new PaperPlayer(player), this)) return;

        event.setFoodLevel(20);
    }
}
