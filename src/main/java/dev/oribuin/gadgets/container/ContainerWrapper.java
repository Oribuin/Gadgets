package dev.oribuin.gadgets.container;

import dev.oribuin.gadgets.node.impl.logistics.type.valued.Filtered;
import dev.oribuin.gadgets.util.block.FinePosition;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ContainerWrapper {

    /**
     * Check if a provided {@link Block} is a valid container for the wrapper
     *
     * @return if the container is valid
     */
    boolean isValidContainer();

    /**
     * Get the position the container is located at if available
     *
     * @return The returning block position
     */
    @Nullable
    FinePosition getPosition();

    /**
     * Retrieve a specified amount of {@link ItemStack} from the {@link ContainerWrapper}.
     * This does not remove the {@link ItemStack} from the {@link ContainerWrapper}, see {@link #deposit(ItemStack)} to remove items
     *
     * @param stack  The {@link ItemStack} that is being taken from the {@link ContainerWrapper}
     * @param amount The amount of items being removed
     * @return The returning stack that was withdrawn
     */
    boolean withdraw(@NotNull ItemStack stack, int amount);

    /**
     * Deposit an itemstack into the {@link ContainerWrapper}'s Inventory, Adding to the total amount
     *
     * @param stack The stack being deposited
     * @return whether the item was successful in being deposited
     */
    boolean deposit(@NotNull ItemStack stack);

    /**
     * Get the first {@link ItemStack} inside of a {@link ContainerWrapper}'s Inventory
     *
     * @param filtered Any filters on the item that needs to be grabbed
     * @return The resulting itemstack if available
     */
    @Nullable
    ItemStack getFirst(@Nullable Filtered filtered);

    /**
     * Check if a container has an item available in its inventory
     *
     * @param stack The stack to check
     * @return Whether the item is inside
     */
    default boolean contains(ItemStack stack) {
        return this.contains(stack, stack.getAmount());
    }

    /**
     * Check if a container has an item available in it's inventory
     *
     * @param stack  The stack to check
     * @param amount The amount of items in the inventory
     * @return Whether the item is inside
     */
    default boolean contains(ItemStack stack, Integer amount) {
        if (stack == null) return false;

        Integer available = this.getContent().get(stack.asQuantity(1));
        return available != null && available >= amount;
    }

    /**
     * Get all the content inside the container wrapper
     *
     * @return The resulting item contents
     */
    @NotNull
    Map<ItemStack, Integer> getContent();

    /**
     * Check if the provided container is empty
     *
     * @return The container to check
     */
    boolean isEmpty();

    /**
     * Check if the {@link ContainerWrapper} has capped out on the maximum {@link ItemStack} that it can hold
     *
     * @return The result of the check
     */
    boolean isContainerFull();

}
