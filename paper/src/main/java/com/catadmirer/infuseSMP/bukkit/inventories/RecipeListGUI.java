package com.catadmirer.infuseSMP.bukkit.inventories;

import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.catadmirer.infuseSMP.bukkit.effects.BukkitEffect;
import com.catadmirer.infuseSMP.bukkit.util.InventoryUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RecipeListGUI implements InventoryHolder {
    private final Inventory inventory;

    public RecipeListGUI() {
        inventory = Bukkit.createInventory(this, 36, Component.text("Potion Crafting"));

        // Loading the potions into the inventory
        int[] customSlots = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32};

        int i = 0;
        for (BukkitEffect effect : InfusePlugin.getInstance().getEffectRegistry().getRegisteredEffects()) {
            if (effect.augmented()) continue;

            ItemStack potion = effect.createItemWithLimits();
            inventory.setItem(customSlots[i], potion);
            i++;
        }

        InventoryUtils.fillRemainingSlots(inventory);

        // Locking the inventory
        InventoryUtils.lockInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}