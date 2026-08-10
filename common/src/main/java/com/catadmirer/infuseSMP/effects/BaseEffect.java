package com.catadmirer.infuseSMP.effects;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;

import java.awt.Color;

public abstract class BaseEffect implements InfuseEffect {
    protected final int id;
    protected final String plainKey;
    protected final boolean augmented;
    protected final Color potionColor;
    protected final BossBar.Color ritualColor;
    protected final Key backgroundMaterial;
    
    protected BaseEffect(String plainKey, int id, boolean augmented, Color potionColor, BossBar.Color ritualColor, Key backgroundMaterial) {
        this.id = id;
        this.plainKey = plainKey;
        this.augmented = augmented;
        this.potionColor = potionColor;
        this.ritualColor = ritualColor;
        this.backgroundMaterial = backgroundMaterial;
    }

    public int id() {
        return id;
    }

    public String plainKey() {
        return plainKey;
    }

    public String key() {
        return augmented ? "aug_" + plainKey : plainKey;
    }

    public boolean augmented() {
        return augmented;
    }

    public Color potionColor() {
        return potionColor;
    }

    public BossBar.Color ritualColor() {
        return ritualColor;
    }

    public Key backgroundMaterial() {
        return backgroundMaterial;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof InfuseEffect effect)) return false;

        return effect.augmented() == augmented && effect.id() == id;
    }

    @Override
    public String toString() {
        return key();
    }

    public char getIcon() {
        return (char) Integer.parseInt("E" + (augmented ? 2 : 0) + String.format("%02d", id), 16);
    }

    public char getActiveIcon() {
        return (char) Integer.parseInt("E" + (augmented ? 3 : 1) + String.format("%02d", id), 16);
    }
}
