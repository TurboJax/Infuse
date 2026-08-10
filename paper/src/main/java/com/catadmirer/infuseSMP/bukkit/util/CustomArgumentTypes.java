package com.catadmirer.infuseSMP.bukkit.util;

import java.util.List;

public class CustomArgumentTypes {
    public static final BukkitEffectArgumentType BUKKIT_EFFECT = new BukkitEffectArgumentType();
    public static final SelectStringArgumentType SLOT = new SelectStringArgumentType(List.of("1", "2"));
    public static final SelectStringArgumentType CONTROL_MODE = new SelectStringArgumentType(List.of("offhand", "command"));
}
