package com.catadmirer.infuseSMP.bukkit.platform;

import com.catadmirer.infuseSMP.platform.World;
import org.bukkit.Location;

public class PaperLocation implements com.catadmirer.infuseSMP.platform.Location {
    private final Location handle;

    public PaperLocation(Location handle) {
        this.handle = handle;
    }

    @Override
    public double getX() {
        return handle.getX();
    }

    @Override
    public double getY() {
        return handle.getY();
    }

    @Override
    public double getZ() {
        return handle.getZ();
    }

    @Override
    public int getBlockX() {
        return handle.getBlockX();
    }

    @Override
    public int getBlockY() {
        return handle.getBlockY();
    }

    @Override
    public int getBlockZ() {
        return handle.getBlockZ();
    }

    @Override
    public World getWorld() {
        return new PaperWorld(handle.getWorld());
    }

    public Location toBukkit() {
        return PaperLocation.toBukkit(this);
    }

    public static Location toBukkit(com.catadmirer.infuseSMP.platform.Location loc) {
        if (loc instanceof PaperLocation ploc) return ploc.handle;

        throw new IllegalArgumentException("Provided location was not a PaperLocation.");
    }
}
