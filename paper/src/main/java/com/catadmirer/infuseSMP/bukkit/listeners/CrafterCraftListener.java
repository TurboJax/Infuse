package com.catadmirer.infuseSMP.bukkit.listeners;

import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.inventory.ItemStack;

import com.catadmirer.infuseSMP.effects.InfuseEffect;

public class CrafterCraftListener implements Listener {
    /** Prevents infuse effects from being crafted in a crafter. */
    @EventHandler
    public void onCrafterCraft(CrafterCraftEvent event) {
        ItemStack item = event.getResult();
        InfuseEffect effect = InfusePlugin.getInstance().getEffectRegistry().fromItem(item);
        if (effect == null) return;

        event.setCancelled(true);
    }
}
