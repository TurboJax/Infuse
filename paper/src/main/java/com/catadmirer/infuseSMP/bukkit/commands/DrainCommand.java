package com.catadmirer.infuseSMP.bukkit.commands;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.bukkit.InfusePlugin;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DrainCommand {
    private final InfusePlugin plugin;
    private final String slot;

    public static LiteralCommandNode<CommandSourceStack> build(InfusePlugin plugin, boolean left) {
        DrainCommand cmd = new DrainCommand(plugin, left ? "1" : "2");

        return Commands.literal(left ? "ldrain" : "rdrain")
            .executes(cmd::drain)
            .build();
    }

    public DrainCommand(InfusePlugin plugin, String slot) {
        this.plugin = plugin;
        this.slot = slot;
    }

    public int drain(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
            return 1;
        }

        plugin.getEffectManager().drainEffect(player, slot);

        return 1;
    }
}