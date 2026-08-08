package com.catadmirer.infuseSMP.util;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import com.catadmirer.infuseSMP.platform.Entity;
import com.catadmirer.infuseSMP.platform.Location;
import com.catadmirer.infuseSMP.platform.Player;

import java.util.Set;

public interface RegionBlocker {
    void init();

    boolean canUseSpark(Player player);

    boolean canBeTargetedBySpark(Entity entity);
    boolean canBeTargetedBySpark(Player player);

    Set<InfuseEffect> getBlockedEffects(Entity entity);
    Set<InfuseEffect> getBlockedEffects(Player player);
    Set<InfuseEffect> getBlockedEffects(Location loc);

    boolean isEffectBlocked(Entity entity, InfuseEffect effect);
    boolean isEffectBlocked(Player player, InfuseEffect effect);
    boolean isEffectBlocked(Location loc, InfuseEffect effect);
}
