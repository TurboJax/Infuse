package com.catadmirer.infuseSMP.platform;

public interface Location {
    double getX();
    double getY();
    double getZ();
    
    int getBlockX();
    int getBlockY();
    int getBlockZ();

    World getWorld();
}
