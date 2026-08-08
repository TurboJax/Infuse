package com.catadmirer.infuseSMP.platform;

import java.util.UUID;

public interface Player {
    UUID getUniqueId();

    Location getLocation();

    World getWorld();
}
