package dev.oribuin.gadgets.api;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BarrelSetItemEvent extends Event {

    private final Block block;
    private final @Nullable ItemStack itemStack;
    private final CustomBlockData data;

    public BarrelSetItemEvent(final Block block, final @Nullable ItemStack itemStack, final CustomBlockData data) {
        this.block = block;
        this.itemStack = itemStack;
        this.data = data;
    }

    public Block getBlock() {
        return this.block;
    }

    @Nullable
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public CustomBlockData getData() {
        return this.data;
    }

    // Default Event Stuff

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
