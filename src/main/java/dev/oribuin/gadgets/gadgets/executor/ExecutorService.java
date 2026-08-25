package dev.oribuin.gadgets.gadgets.executor;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.util.PlayerUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public final class ExecutorService {

    /**
     * Executes an interaction for the given {@link ItemStack} if it's linked to a gadget.
     *
     * @param event  The event to execute for
     * @param player The player to execute for
     * @param stack  The stack to execute for
     * @return Status of the executed action, false if action was denied
     */
    public static <T extends Event> boolean handleForGadget(T event, Player player, ItemStack stack) {
        Gadget gadget = GadgetsPlugin.get().getGadgetManager().from(stack); // checks for null & air
        if (gadget == null || !gadget.isEnabled()) return false;
        if (!gadget.isApplicable(event)) return false;

        // Check if the player can use the gadget
        if (!PlayerUtil.playerCheck(player, player.getLocation(), gadget)) {
            GadgetsPlugin.get().getLogger().info("Player is not permitted to use item with key [" + gadget.getIdentifier() + "] at their location");
            return false;
        }

        gadget.callEvent(new ContextProvider<>(event, player, stack));
        return true;
    }

    /**
     * Executes an interaction for the given {@link ItemStack} if it's linked to a gadget.
     *
     * @param event  The event to execute for
     * @param player The player to execute for
     * @param block  The block to execute for
     * @return Status of the executed action, false if action was denied
     */
    public static <T extends Event> boolean handleForNode(T event, Player player, Node node, Block block) {
        if (!node.isApplicable(event)) return false;

        // Check if the player can use the node
//        if (!PlayerUtil.playerCheck(player, player.getLocation(), node)) {
//            Debugger.log("item", "Player is not permitted to use item with key [" + node.getName() + "] at their location");
//            return false;
//        }

        node.callEvent(new ContextProvider<>(event, player, block));
        return true;
    }

}
