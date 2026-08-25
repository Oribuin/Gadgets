package dev.oribuin.gadgets.container.impl;

import dev.oribuin.gadgets.container.ContainerWrapper;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.impl.DeepStorageBarrel;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Filtered;
import dev.oribuin.gadgets.util.block.FinePosition;
import dev.oribuin.gadgets.util.block.NodePipePath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_AMOUNT;
import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_ITEM;
import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_MAX_AMOUNT;
import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_VOID_EXCESS;

public class DeepBarrelContainer implements ContainerWrapper {

    private final DeepStorageBarrel barrel;

    /**
     * Create a new vanilla container for the plugin
     *
     * @param barrel The block to open
     */
    public DeepBarrelContainer(DeepStorageBarrel barrel) {
        this.barrel = barrel;
    }

    /**
     * Check if a provided {@link Block} is a valid container for the wrapper
     *
     * @param block The block provided
     * @return if the container is valid
     */
    @Nullable
    public static DeepBarrelContainer from(Block block) {
        if (block == null || !NodeFactory.isTypeOf(block, NodeFactory.DEEP_BARREL)) return null;

        return new DeepBarrelContainer(NodeFactory.from(block));
    }

    /**
     * Check if a provided {@link Block} is a valid container for the wrapper
     *
     * @return if the container is valid
     */
    @Override
    public boolean isValidContainer() {
        return this.barrel != null && this.barrel.getValue(BARREL_ITEM) != null;
    }

    /**
     * Get the position the container is located at if available
     *
     * @return The returning block position
     */
    @Override
    public @Nullable FinePosition getPosition() {
        return FinePosition.from(this.barrel.getBlock());
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
        // Check if the container is loaded or the item matches
        Location position = this.barrel.getBlock().getLocation();
        boolean loaded = NodePipePath.isLoaded(position);
        ItemStack stored = this.barrel.getValue(BARREL_ITEM);
        if (!loaded || stored == null || !stored.isSimilar(stack)) return false;

        // Make sure the container has the amount being withdrawn
        int storedAmount = this.barrel.getValue(BARREL_AMOUNT, 0);
        int toWithdraw = Math.min(amount, storedAmount);
        if (toWithdraw <= 0) return false;

        // or do i only do it here
        this.barrel.setValue(BARREL_AMOUNT, storedAmount - toWithdraw);
        this.barrel.serialize();
        return true;
    }

    /**
     * Deposit an itemstack into the {@link ContainerWrapper}'s Inventory, Adding to the total amount
     *
     * @param stack The stack being deposited
     * @return whether the item was successful in being deposited
     */
    @Override
    public boolean deposit(@NotNull ItemStack stack) {
        // Check if the container is loaded or the item matches
        Location position = this.barrel.getBlock().getLocation();
        boolean loaded = NodePipePath.isLoaded(position);
        ItemStack stored = this.barrel.getValue(BARREL_ITEM);
        if (!loaded || stored == null || !stored.isSimilar(stack)) return false;

        int storedAmount = this.barrel.getValue(BARREL_AMOUNT, 0);
        int maxAmount = this.barrel.getValue(BARREL_MAX_AMOUNT, 4096);
        boolean isVoiding = this.barrel.getValue(BARREL_VOID_EXCESS, true);
        boolean isOverflow = storedAmount + stack.getAmount() > maxAmount;

        if (isOverflow && isVoiding) {
            if (storedAmount == maxAmount) return true; // It's already the max amount, we don't need to waste process setting it again
            this.barrel.setValue(BARREL_AMOUNT, maxAmount);
            this.barrel.serialize();
            return true;
        }

        if (!isOverflow) {
            this.barrel.setValue(BARREL_AMOUNT, storedAmount + stack.getAmount());
            this.barrel.serialize();
            return true;
        }

        return false;
    }

    /**
     * Get the first {@link ItemStack} inside of a {@link ContainerWrapper}'s Inventory
     *
     * @param filtered Any filters on the item that needs to be grabbed
     * @return The resulting itemstack if available
     */
    @Override
    public @Nullable ItemStack getFirst(@Nullable Filtered filtered) {
        ItemStack stack = this.barrel.getValue(BARREL_ITEM);
        if (stack == null) return null;

        return stack.asQuantity(stack.getMaxStackSize());
    }

    /**
     * Get all the content inside the contnainer wrapper
     *
     * @return The resulting item contennts
     */
    @Override
    public @NotNull Map<ItemStack, Integer> getContent() {
        ItemStack current = this.barrel.getValue(BARREL_ITEM);
        if (current == null || current.getType() == Material.AIR) return Map.of();

        return Map.of(current, this.barrel.getValue(BARREL_AMOUNT, 0));
    }

    /**
     * Check if the provided container is empty
     *
     * @return The container to check
     */
    @Override
    public boolean isEmpty() {
        return this.barrel.getValue(BARREL_AMOUNT, 0) <= 0;
    }

    /**
     * Check if the {@link ContainerWrapper} has capped out on the maximum {@link ItemStack} that it can hold
     *
     * @return The result of the check
     */
    @Override
    public boolean isContainerFull() {
        int storedAmount = this.barrel.getValue(BARREL_AMOUNT, 0);
        int maxAmount = this.barrel.getValue(BARREL_MAX_AMOUNT, 4096);
        boolean isVoiding = this.barrel.getValue(BARREL_VOID_EXCESS, true);
        return storedAmount >= maxAmount && !isVoiding;
    }

}
