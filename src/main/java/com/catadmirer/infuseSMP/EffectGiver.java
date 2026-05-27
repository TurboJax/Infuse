package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EffectGiver implements CommandExecutor, Listener {
    private final Infuse plugin;

    public EffectGiver(Infuse plugin) {
        this.plugin = plugin;
    }

    public ItemStack getRandomEffect() {
        List<EffectMapping> effects = plugin.getMainConfig().effectsFromNetherStar();
        EffectMapping effect = effects.get((int) (Math.random() * effects.size())).regular();

        if (Math.random() < plugin.getMainConfig().netherStarAugChance()) {
            effect = effect.augmented();
        }

        return effect.createItem();
    }

    public ItemStack getSelector() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);

        int cmd = plugin.getMainConfig().netherStarCMD();
        if (cmd > -1) {
            item.editMeta(meta -> {
                meta.setCustomModelData(cmd);
            });
        }

        return item;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        Map<Integer,ItemStack> leftovers = player.getInventory().addItem(getSelector());
        if (leftovers.isEmpty()) return true;

        player.sendMessage("Your inventory is too full to get an effect selector.");
        return true;
    }

    @EventHandler
    public void onUseNetherStar(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (event.useInteractedBlock() == Event.Result.ALLOW) return;
        if (!event.hasItem()) return;

        ItemStack item = event.getItem();
        if (item.getType() != Material.NETHER_STAR) return;

        int cmd = plugin.getMainConfig().netherStarCMD();
        if (cmd > -1) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            if (!meta.hasCustomModelData()) return;
            if (meta.getCustomModelData() != cmd) return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        Map<Integer,ItemStack> leftovers = player.getInventory().addItem(getRandomEffect());

        if (leftovers.isEmpty()) {
            item.subtract(1);
            player.sendMessage(Component.text("You have recieved a random effect!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("You don't have enough space in your inventory to get an effect.", NamedTextColor.RED));
        }
    }
}
