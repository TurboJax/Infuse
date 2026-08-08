package com.catadmirer.infuseSMP.bukkit.util.regions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.catadmirer.infuseSMP.EffectRegistry;
import com.catadmirer.infuseSMP.InfuseProvider;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.platform.Entity;
import com.catadmirer.infuseSMP.platform.Location;
import com.catadmirer.infuseSMP.platform.Player;
import com.catadmirer.infuseSMP.util.RegionBlocker;
import net.kyori.adventure.key.Key;

public class BasicRegionBlocker implements RegionBlocker {
    @Override
    public void init() {}
    
    @Override
    public boolean canUseSpark(Player player) {
        return true;
    }

    @Override
    public boolean canBeTargetedBySpark(Entity entity) {
        return true;
    }

    @Override
    public boolean canBeTargetedBySpark(Player player) {
        return true;
    }

    @Override
    public Set<InfuseEffect> getBlockedEffects(Entity entity) {
        return getBlockedEffects(entity.getLocation());
    }

    @Override
    public Set<InfuseEffect> getBlockedEffects(Player player) {
        return getBlockedEffects(player.getLocation());
    }

    @Override
    public Set<InfuseEffect> getBlockedEffects(Location loc) {
        return EffectRegistry.getRegisteredEffects()
            .values()
            .stream()
            .filter(e -> {
                List<Key> worlds = InfuseProvider.getInstance().getMainConfig().getBlacklistedWorlds(e);

                return worlds.contains(loc.getWorld().key());
            })
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isEffectBlocked(Entity entity, InfuseEffect effect) {
        return isEffectBlocked(entity.getLocation(), effect);
    }

    @Override
    public boolean isEffectBlocked(Player player, InfuseEffect effect) {
        return isEffectBlocked(player.getLocation(), effect);
    }

    @Override
    public boolean isEffectBlocked(Location loc, InfuseEffect effect) {
        List<Key> worlds = InfuseProvider.getInstance().getMainConfig().getBlacklistedWorlds(effect);

        return worlds.contains(loc.getWorld().key());
    }
    
}
