package com.catadmirer.infuseSMP.platform.services;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public interface EffectRegistry<T extends InfuseEffect> {
    boolean register(T effect);
    boolean unregister(T effect);

    default boolean isRegistered(String key) {
        return getRegisteredEffects().stream().anyMatch(e -> e.key().equals(key));
    }

    default boolean isRegistered(T effect) {
        return getRegisteredEffects().stream().anyMatch(effect::equals);
    }

    default boolean isRegistered(int id) {
        return getRegisteredEffects().stream().anyMatch(e -> e.id() == id);
    }

    @Nullable T fromKey(@Nullable String key);
    @Nullable T fromId(int id);

    @Unmodifiable
    List<T> getRegisteredEffects();
}
