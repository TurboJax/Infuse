package com.catadmirer.infuseSMP.bukkit.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.catadmirer.infuseSMP.effects.BaseEffect;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class BukkitEffect extends BaseEffect implements Listener {
    public static final NamespacedKey EFFECT_KEY = new NamespacedKey("infuse", "effect_key");
    public static final NamespacedKey AUG_KEY = new NamespacedKey("infuse", "aug");

    protected final InfusePlugin plugin = InfusePlugin.getInstance();

    protected BukkitEffect(String plainKey, EffectConstants.Id id, boolean augmented, EffectConstants.PotionColor potionColor, EffectConstants.RitualColor ritualColor, EffectConstants.BackgroundColor backgroundMaterial) {
        super(plainKey, id.value(), augmented, potionColor.value(), ritualColor.value(), backgroundMaterial.value());
    }

    protected BukkitEffect(String plainKey, int id, boolean augmented, Color potionColor, BossBar.Color ritualColor, Key backgroundMaterial) {
        super(plainKey, id, augmented, potionColor, ritualColor, backgroundMaterial);
    }

    /**
     * Creates an {@link ItemStack} representation of the effect for a player to consume.
     *
     * @return The corresponding {@link ItemStack}
     */
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.POTION);

        // Adjusting item data
        item.setData(DataComponentTypes.CUSTOM_NAME, getName().toComponent());
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore().toComponentList()));
        item.editPersistentDataContainer(c -> c.set(EFFECT_KEY, PersistentDataType.STRING, toString()));

        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.POTION_CONTENTS));
        item.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().customColor(org.bukkit.Color.fromARGB(potionColor.getRGB())));

        if (augmented) {
            item.setData(DataComponentTypes.ITEM_MODEL, AUG_KEY);
        }

        return item;
    }

    @Nullable
    public ItemStack createItemWithLimits() {
        // Only regular effects should be put here
        if (augmented()) return null;

        // Creating the potion from the effect
        ItemStack potionItem = createItem();

        // Getting an instance of the plugin to read configs
        InfusePlugin plugin = InfusePlugin.getInstance();

        int augLeft = plugin.getMainConfig().getCraftLimit(getAugmentedVersion()) - plugin.getDataManager().getExistingCount(getAugmentedVersion());
        int regLeft = plugin.getMainConfig().getCraftLimit(getRegularVersion()) - plugin.getDataManager().getExistingCount(getRegularVersion());

        List<Component> lore = new ArrayList<>();
        lore.add(Message.toComponent("<gray>Augmented Limit: <aqua>" + augLeft));
        lore.add(Message.toComponent("<gray>Regular Limit: <aqua>" + regLeft));
        potionItem.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

        return potionItem;
    }

    /**
     * Checks if an {@link ItemStack} was created by this effect.
     *
     * @param item The item to check.
     *
     * @return Whether or not the item was created by this effect.
     */
    public boolean itemMatches(@Nullable ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.POTION) return false;

        return key().equals(item.getPersistentDataContainer().get(EFFECT_KEY, PersistentDataType.STRING));
    }
}
