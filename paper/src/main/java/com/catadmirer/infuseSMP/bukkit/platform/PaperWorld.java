package com.catadmirer.infuseSMP.bukkit.platform;

import net.kyori.adventure.key.Key;
import org.bukkit.World;

public class PaperWorld implements com.catadmirer.infuseSMP.platform.World {
    private final World handle;

    public PaperWorld(World handle) {
        this.handle = handle;
    }

    @Override
    public Key key() {
        return handle.key();
    }

    public World toBukkit() {
        return PaperWorld.toBukkit(this);
    }

    public static World toBukkit(com.catadmirer.infuseSMP.platform.World world) {
        if (world instanceof PaperWorld pworld) return pworld.handle;

        throw new IllegalArgumentException("Provided world was not a PaperWorld.");
    }
}
