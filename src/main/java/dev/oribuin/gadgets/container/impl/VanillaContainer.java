package dev.oribuin.gadgets.container.impl;

import dev.oribuin.gadgets.container.ContainerWrapper;
import dev.oribuin.gadgets.container.VanillaWrapper;
import dev.oribuin.gadgets.node.logistics.type.valued.Filtered;
import dev.oribuin.gadgets.util.InventoryUtils;
import dev.oribuin.gadgets.util.block.FinePosition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VanillaContainer implements ContainerWrapper, VanillaWrapper {

    private final Block block;
    private final FinePosition position;
    private Inventory inventory;

    /**
     * Create a new vanilla container for the plugin
     *
     * @param block The block to open
     */
    public VanillaContainer(Block block) {
        this.block = block;
        this.position = FinePosition.from(block.getLocation());

        if (this.isValidContainer() && Bukkit.isPrimaryThread()) {
            this.inventory = ((Container) block.getState()).getInventory();
        }
    }

    /**
     * Check if a provided {@link Block} is a valid container for the wrapper
     *
     * @param block The block provided
     * @return if the container is valid
     */
    @Nullable
    public static VanillaContainer from(Block block) {
        return block.getState() instanceof Container ? new VanillaContainer(block) : null;
    }

    /**
     * Check if a provided {@link Block} is a valid container for the wrapper
     *
     * @return if the container is valid
     */
    @Override
    public boolean isValidContainer() {
        return block.getState() instanceof Container;
    }

    /**
     * Get the position the container is located at if available
     *
     * @return The returning block position
     */
    @Override
    public @Nullable FinePosition getPosition() {
        return this.position;
    }

    /**
     * Retrieve a specified amount of {@link ItemStack} from the {@link ContainerWrapper}.
     * This does not remove the {@link ItemStack} from the {@link ContainerWrapper}, see {@link #deposit(ItemStack)} to remove items
     *
     * @param stack  The {@link ItemStack} that is being taken from the {@link ContainerWrapper}
     * @param amount The amount of items being removed
     * @return The returning stack that was withdrawn
     */
    @Override
    public boolean withdraw(@NotNull ItemStack stack, int amount) {
        boolean result = false;
        int current = 0;
        for (ItemStack itemStack : this.inventory.getContents()) {
            if (current == amount) return true;
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (!itemStack.isSimilar(stack)) continue;

            int remaining = amount - current;
            int toSubtract;
            if (remaining <= itemStack.getAmount()) {
                toSubtract = Math.min(remaining, stack.getMaxStackSize());
                itemStack.subtract(toSubtract);
            } else {
                toSubtract = itemStack.getAmount();
                itemStack.setAmount(0);
            }

            current += toSubtract;
            result = current == amount;
        }

        return result;
    }

    /**
     * Deposit an itemstack into the {@link ContainerWrapper}'s Inventory, Adding to the total amount
     *
     * @param stack The stack being deposited
     * @return whether the item was successful in being deposited
     */
    @Override
    public boolean deposit(@NotNull ItemStack stack) {
        int total = InventoryUtils.getRemainingSpace(this.inventory, stack);
        if (total < stack.getAmount()) return false;

        this.inventory.addItem(stack);
        return true;
    }

    /**
     * Get the first {@link ItemStack} inside of a {@link ContainerWrapper}'s Inventory
     *
     * @param filtered Any filters on the item that needs to be grabbed
     * @return The resulting itemstack if available
     */
    @Override
    public @Nullable ItemStack getFirst(@Nullable Filtered filtered) {
        return Arrays.stream(this.inventory.getStorageContents())
                .filter(stack -> {
                    if (stack == null || stack.getType().isAir()) return false;
                    if (filtered != null) return filtered.getFilterType().canAccept(filtered, stack);
                    return true;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all the content inside the contnainer wrapper
     *
     * @return The resulting item contennts
     */
    @Override
    public @NotNull Map<ItemStack, Integer> getContent() {
        Map<ItemStack, Integer> result = new HashMap<>();
        for (ItemStack stack : this.inventory.getStorageContents()) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            ItemStack single = stack.asQuantity(1);
            int current = result.getOrDefault(single, 0);
            current += single.getAmount();
            result.put(single, current);
        }

        return result;
    }

    /**
     * Check if the provided container is empty
     *
     * @return The container to check
     */
    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    /**
     * Check if the {@link ContainerWrapper} has capped out on the maximum {@link ItemStack} that it can hold
     *
     * @return The result of the check
     */
    @Override
    public boolean isContainerFull() {
        return this.inventory.firstEmpty() == -1; // todo: better check for this, less optimal though
    }

    public Block getBlock() {
        return block;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
