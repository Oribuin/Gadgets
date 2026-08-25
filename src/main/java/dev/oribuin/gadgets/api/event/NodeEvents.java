package dev.oribuin.gadgets.api.event;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.node.Node;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * A list of functionalities that are actively being utilized by {@link Node}
 */
public interface NodeEvents {

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleBlockBreak(ContextProvider<BlockBreakEvent, Block> provider) {
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleBlockPlace(ContextProvider<BlockPlaceEvent, Block> provider) {
    }

    /**
     * Handles any functionality behind a player initially damaging a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleBlockDamage(ContextProvider<BlockDamageEvent, Block> provider) {
    }


    /**
     * Handles any functionality behind a player toggling their sneak on or off
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleSneak(ContextProvider<PlayerToggleSneakEvent, Block> provider) {
    }

    /**
     * Handles any functionality behind a player toggling their fly on or off
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleFly(ContextProvider<PlayerToggleFlightEvent, Block> provider) {
    }

    /**
     * Handles any functionality behind a player jumping
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleJump(ContextProvider<PlayerJumpEvent, Block> provider) {
    }

    /**
     * Handles any functionality behind a player dropping an item
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handlePlayerDropItem(ContextProvider<PlayerDropItemEvent, Block> provider) {
    }

    /**
     * Handles any functionality behind a furnace starting to smelt an item
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleStartSmelt(ContextProvider<FurnaceStartSmeltEvent, Block> provider) {
    }

}
