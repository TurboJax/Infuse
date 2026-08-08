package com.catadmirer.infuseSMP.bukkit.platform;

import com.catadmirer.infuseSMP.platform.Location;
import org.bukkit.entity.Entity;

public class PaperEntity implements com.catadmirer.infuseSMP.platform.Entity {
    private final Entity handle;

    public PaperEntity(Entity handle) {
        this.handle = handle;
    }

    @Override
    public Location getLocation() {
        return new PaperLocation(handle.getLocation());
    }

    public Entity toBukkit() {
        return PaperEntity.toBukkit(this);
    }

    public static Entity toBukkit(com.catadmirer.infuseSMP.platform.Entity entity) {
        if (entity instanceof PaperEntity pentity) return pentity.handle;

        throw new IllegalArgumentException("Provided entity was not a PaperEntity.");
    }
}
