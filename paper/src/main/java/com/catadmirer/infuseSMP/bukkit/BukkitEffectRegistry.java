package com.catadmirer.infuseSMP.bukkit;

import com.catadmirer.infuseSMP.platform.services.EffectRegistry;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.bukkit.effects.BukkitEffect;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class BukkitEffectRegistry implements EffectRegistry<BukkitEffect> {
    private static final List<BukkitEffect> REGISTERED_EFFECTS = new ArrayList<>();

    public boolean register(BukkitEffect effect) {
        if (effect.id() > 100) {
            Infuse.LOGGER.warn("Effect id {} for {} is invalid.  Effect ids cannot be >100.", effect.id(), effect.plainKey());
            return false;
        }

        BukkitEffect existing = fromId(effect.id());
        if (existing != null) {
            Infuse.LOGGER.warn("Effect id {} has already been taken by {}.  Cannot assign it to {}.", effect.id(), existing.plainKey(), effect.plainKey());
            return false;
        }

        return REGISTERED_EFFECTS.add(effect);
    }

    public boolean unregister(BukkitEffect effect) {
        return REGISTERED_EFFECTS.removeIf(effect::equals);
    }

    public boolean isRegistered(ItemStack item) {
        String key = getKey(item);

        return REGISTERED_EFFECTS.stream().anyMatch(e -> e.key().equals(key));
    }

    @Nullable
    public BukkitEffect fromKey(@Nullable String key) {
        return REGISTERED_EFFECTS.stream().filter(e -> e.key().equals(key)).findFirst().orElse(null);
    }

    @Nullable
    public BukkitEffect fromId(int id) {
        return REGISTERED_EFFECTS.stream().filter(e -> e.id() == id).findFirst().orElse(null);
    }

    @Nullable
    public BukkitEffect fromItem(@Nullable ItemStack item) {
        return fromKey(getKey(item));
    }

    @Nullable
    public static String getKey(@Nullable ItemStack item) {
        if (item == null) return null;
        return item.getPersistentDataContainer().get(BukkitEffect.EFFECT_KEY, PersistentDataType.STRING);
    }

    @Unmodifiable
    public List<BukkitEffect> getRegisteredEffects() {
        return REGISTERED_EFFECTS;
    }
}
