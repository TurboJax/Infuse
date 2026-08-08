package com.catadmirer.infuseSMP.bukkit.platform;

import com.catadmirer.infuseSMP.platform.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PaperPlayer implements com.catadmirer.infuseSMP.platform.Player {
    private final Player handle;

    public PaperPlayer(Player handle) {
        this.handle = handle;
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public Location getLocation() {
        return new PaperLocation(handle.getLocation());
    }

    public Player toBukkit() {
        return PaperPlayer.toBukkit(this);
    }

    public static Player toBukkit(com.catadmirer.infuseSMP.platform.Player player) {
        if (player instanceof PaperPlayer pplayer) return pplayer.handle;

        throw new IllegalArgumentException("Provided player was not a PaperPlayer.");
    }
}
