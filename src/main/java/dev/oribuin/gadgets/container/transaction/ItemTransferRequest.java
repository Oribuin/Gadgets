package dev.oribuin.gadgets.container.transaction;

import dev.oribuin.gadgets.container.ContainerProvider;
import dev.oribuin.gadgets.container.ContainerWrapper;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Channelled;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Directional;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Filtered;
import dev.oribuin.gadgets.util.block.FinePosition;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Create a new request to transfer an {@link ItemStack} from a source {@link Node} to a destination {@link Node}
 *
 */
public record ItemTransferRequest(@NotNull ItemStack stack, @NotNull Integer amount, @NotNull FinePosition source,
                                  @Nullable Player requester) {

    /**
     * @param stack     The {@link ItemStack} to transfer
     * @param amount    The amount of the items being transferred
     * @param source    The {@link FinePosition} representing the node the item is from
     * @param requester The {@link Player} who requested the item be transferred
     */
    public ItemTransferRequest {
    }

    /**
     * Create a new request to transfer an {@link ItemStack} from a source {@link Node} to a destination {@link Node}
     *
     * @param stack  The {@link ItemStack} to transfer
     * @param amount The amount of the items being transferred
     * @param source The {@link FinePosition} representing the node the item is from
     */
    public ItemTransferRequest(@NotNull ItemStack stack, @NotNull Integer amount, @NotNull FinePosition source) {
        this(stack, amount, source, null);
    }

    /**
     * Create a new request to transfer an {@link ItemStack} from a source {@link Node} to a destination {@link Node}
     *
     * @param stack     The {@link ItemStack} to transfer
     * @param source    The {@link FinePosition} representing the node the item is from
     * @param requester The {@link Player} who requested the item be transferred
     */
    public ItemTransferRequest(@NotNull ItemStack stack, @NotNull FinePosition source, @Nullable Player requester) {
        this(stack, stack.getAmount(), source, requester);
    }

    /**
     * Create a new request to transfer an {@link ItemStack} from a source {@link Node} to a destination {@link Node}
     *
     * @param stack  The {@link ItemStack} to transfer
     * @param source The {@link FinePosition} representing the node the item is from
     */
    public ItemTransferRequest(@NotNull ItemStack stack, @NotNull FinePosition source) {
        this(stack, stack.getAmount(), source, null);
    }

    /**
     * Check if a node can accept an item
     *
     * @param source      The source of the transfer request
     * @param destination The potential destination of the transfer request
     * @return Whether the node can accept the item
     */
    public boolean canAccept(Node source, Node destination) {
        if (source == null || destination == null) return false;

        // Check if two nodes are on the same channel 
        if (source instanceof Channelled sourceChannel && destination instanceof Channelled destChannel) {
            boolean result = sourceChannel.getChannel() == destChannel.getChannel();
            if (!result) return false;
        }

        // TODO: Check StockOption

        // Check if the destionation has a filter this disables this 
        if (destination instanceof Filtered filtered) {
            return filtered.getFilterType().canAccept(filtered, this.stack.asQuantity(amount));
        }

        return true;
    }

    /**
     * Check if a node can accept an item
     *
     * @param destination The potential destination of the transfer request
     * @return Whether the node can accept the item
     */
    public boolean canAccept(Node destination) {
        return this.canAccept(NodeFactory.from(this.source.toLocation().getBlock()), destination);
    }

    /**
     * Withdraw an item from the source node of the transfer request
     *
     * @return The result of the check
     */
    public boolean withdrawSource() {
        return this.withdrawSource(NodeFactory.from(this.source.toLocation().getBlock()));
    }

    /**
     * Withdraw an item from the source node of the transfer request
     *
     * @param node The node to remove items from
     * @return The result of the withdrawal
     */
    public boolean withdrawSource(Node node) {
        if (node == null) return false;

        // Get the direction block for the withdrawing if applicable
        Block block = node.getBlock();
        Block relative = null;
        if (node instanceof Directional directional) {
            relative = block.getRelative(directional.getDirection());
        }

        // Get the container from ths node
        ContainerWrapper container = ContainerProvider.from(relative != null ? relative : block);
        if (container == null) return false;

        return container.withdraw(this.stack, this.amount);
    }

    /**
     * Deposit an item into the destination node of the transfer request
     *
     * @param node The node to deposit items into
     * @return The result of the deposit
     */
    public boolean depositSource(Node node) {
        if (node == null) return false;

        // Get the direction block for the withdrawing if applicable
        Block block = node.getBlock();
        Block relative = null;
        if (node instanceof Directional directional) {
            relative = block.getRelative(directional.getDirection());
        }

        // Get the container from ths node
        ContainerWrapper container = ContainerProvider.from(relative != null ? relative : block);
        if (container == null) return false;

        return container.deposit(this.stack.asQuantity(this.amount));
    }

}
