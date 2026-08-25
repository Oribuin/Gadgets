package dev.oribuin.gadgets.command.impl;

import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.List;

public class GiveCommand {

    @Command("cyg give <player> <type> [amount]")
    @Permission("cyg.admin")
    @CommandDescription("Give the targeted player a custom nexo originated item")
    private void give(CommandSender sender,
                      @Argument("player") Player player,
                      @Argument(value = "type", suggestions = "nodes") String type,
                      @Argument(value = "amount") Integer amount
    ) {
        if (amount == null) amount = 1;
        if (amount < 1 || amount > 64) {
            sender.sendMessage(Component.text("Amount must be between 1 and 64"));
            return;
        }

        NodeType<?> nodeType = NodeFactory.from(type);
        if (nodeType == null) {
            sender.sendMessage("Unknown node type[" + type + "]: Available[" + String.join(", ", NodeFactory.REGISTRY.keySet()) + "]");
            return;
        }

        player.getInventory().addItem(NodeFactory.create(nodeType, amount));
    }

    @Suggestions("nodes")
    public List<String> suggestion(CommandContext<CommandSender> sender, String input) {
        return NodeFactory.REGISTRY.keySet().stream().toList();
    }

}
