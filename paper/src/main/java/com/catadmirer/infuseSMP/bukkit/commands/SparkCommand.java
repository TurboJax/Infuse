package com.catadmirer.infuseSMP.bukkit.commands;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.catadmirer.infuseSMP.bukkit.platform.PaperPlayer;
import com.catadmirer.infuseSMP.effects.InfuseEffect;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class SparkCommand {
    private final InfusePlugin plugin;
    private final String slot;

    public static LiteralCommandNode<CommandSourceStack> build(InfusePlugin plugin, boolean lSpark) {
        SparkCommand cmd = new SparkCommand(plugin, lSpark ? "1" : "2");

        return Commands.literal(lSpark ? "lspark" : "rspark").executes(cmd::activateSpark).build();
    }

    private SparkCommand(InfusePlugin plugin, String slot) {
        this.plugin = plugin;
        this.slot = slot;
    }

    public int activateSpark(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
            return 1;
        }

        PaperPlayer paperPlayer = new PaperPlayer(player);

        // Getting the name of the equipped effect.
        InfuseEffect equippedEffect = plugin.getDataManager().getEffect(player.getUniqueId(), slot);

        // Handling if the slot is empty.
        if (equippedEffect == null) {
            Message msg = new Message(MessageType.SLOT_EMPTY);
            msg.applyPlaceholder("slot", slot);
            player.sendMessage(msg.toComponent());
            return 1;
        }

        // Warning the player that they can't use the spark right now
        if (!plugin.getRegionBlocker().canUseSpark(paperPlayer) || plugin.getRegionBlocker().isEffectBlocked(paperPlayer, equippedEffect)) {
            sender.sendMessage(Message.toComponent("<red>You cannot activate your spark in this area!"));
        }

        equippedEffect.activateSpark(paperPlayer);

        return 1;
    }
}
