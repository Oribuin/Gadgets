package dev.oribuin.gadgets.api.event;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * A list of functionalities that are actively being utilized by {@link Gadget}
 */
public interface GadgetEvents {

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleInteract(ContextProvider<PlayerInteractEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player interacting with an entity in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleEntityInteract(ContextProvider<PlayerInteractAtEntityEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleBlockBreak(ContextProvider<BlockBreakEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleBlockPlace(ContextProvider<BlockPlaceEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player initially damaging a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleBlockDamage(ContextProvider<BlockDamageEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player damaging an entity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleEntityDamage(ContextProvider<EntityDamageByEntityEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player toggling their sneak on or off
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleSneak(ContextProvider<PlayerToggleSneakEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player toggling their fly on or off
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleFly(ContextProvider<PlayerToggleFlightEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player jumping
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handleJump(ContextProvider<PlayerJumpEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player dropping an item
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handlePlayerDropItem(ContextProvider<PlayerDropItemEvent, ItemStack> provider) {
    }

    /**
     * Handles any functionality behind a player eating an item
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    default void handlePlayerConsume(ContextProvider<PlayerItemConsumeEvent, ItemStack> provider) {
    }

}
