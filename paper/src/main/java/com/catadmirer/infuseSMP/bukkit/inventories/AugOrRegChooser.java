package com.catadmirer.infuseSMP.bukkit.inventories;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.bukkit.effects.BukkitEffect;
import com.catadmirer.infuseSMP.bukkit.util.InventoryUtils;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class AugOrRegChooser implements InventoryHolder {
    private final Inventory inventory;

    public AugOrRegChooser(InfuseEffect effect) {
        inventory = Bukkit.createInventory(this, 27, Message.toComponent("<yellow>Choose"));

        // Filling the inventory with a filler item.
        InventoryUtils.fillInventory(inventory, InventoryUtils.createNoTooltip(Registry.MATERIAL.get(effect.backgroundMaterial())));

        if (!(effect.getRegularVersion() instanceof BukkitEffect bReg)) return;
        if (!(effect.getAugmentedVersion() instanceof BukkitEffect bAug)) return;

        // Adding the effects to the inventory
        inventory.setItem(11, bReg.createItem());
        inventory.setItem(15, bAug.createItem());

        // Locking the inventory
        InventoryUtils.lockInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
