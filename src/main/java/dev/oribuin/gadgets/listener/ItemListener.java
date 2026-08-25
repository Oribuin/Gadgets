package dev.oribuin.gadgets.listener;

import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ExecutorService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;

public final class ItemListener implements Listener {

    /**
     * Execute the event for all {@link Gadget} that are registered in the plugin that use this event
     *
     * @param event The event being used
     */
    @EventHandler(priority = EventPriority.LOW)
    public void handleInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;
        if (event.getItem() == null || event.getItem().getType() == Material.AIR) return;

        ExecutorService.handleForGadget(event, event.getPlayer(), event.getItem());
    }

    /**
     * Execute the event for all {@link Gadget} that are registered in the plugin that use this event
     *
     * @param event The event being used
     */
    @EventHandler(priority = EventPriority.LOW)
    public void handleEntityInteract(PlayerInteractAtEntityEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();
        ExecutorService.handleForGadget(event, event.getPlayer(), inventory.getItemInMainHand());
    }

    /**
     * Execute the event for all {@link Gadget} that are registered in the plugin that use this event
     *
     * @param event The event being used
     */
    @EventHandler(priority = EventPriority.LOW)
    public void handleBlockBreak(BlockBreakEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();
        ExecutorService.handleForGadget(event, event.getPlayer(), inventory.getItemInMainHand());
    }

    /**
     * Execute the event for all {@link Gadget} that are registered in the plugin that use this event
     *
     * @param event The event being used
     */
    @EventHandler(priority = EventPriority.LOW)
    public void handleBlockDamage(BlockDamageEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();
        ExecutorService.handleForGadget(event, event.getPlayer(), inventory.getItemInMainHand());
    }

    /**
     * Execute the event for all {@link Gadget} that are registered in the plugin that use this event
     *
     * @param event The event being used
     */
    @EventHandler(priority = EventPriority.LOW)
    public void handleEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            ExecutorService.handleForGadget(event, attacker, attacker.getInventory().getItemInMainHand());
        }

        if (event.getEntity() instanceof Player attacked) {
            Arrays.stream(attacked.getInventory().getContents())
                    .filter(stack -> stack != null && stack.getType() != Material.AIR && stack.hasItemMeta())
                    .forEach(stack -> ExecutorService.handleForGadget(event, attacked, stack));
        }
    }

    /**
     * Execute the event for all {@link Gadget} that are registered in the plugin that use this event
     *
     * @param event The event being used
     */
    @EventHandler(priority = EventPriority.LOW)
    public void handleSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer().getInventory().isEmpty()) return;

        Arrays.stream(event.getPlayer().getInventory().getContents())
                .filter(stack -> stack != null && stack.getType() != Material.AIR && stack.hasItemMeta())
                .forEach(stack -> ExecutorService.handleForGadget(event, event.getPlayer(), stack));
    }

//    // region Unused Events
//    /**
//     * Execute the event for all {@link uk.mangostudios.items.gadgets.Gadget} that are registered in the plugin that use this event
//     *
//     * @param event The event being used
//     */
//    @EventHandler(priority = EventPriority.LOW)
//    public void handleFly(PlayerToggleFlightEvent event) {
//        if (event.getPlayer().getInventory().isEmpty()) return;
//
//        Arrays.stream(event.getPlayer().getInventory().getContents())
//                .filter(stack -> stack != null && stack.getType() != Material.AIR && stack.hasItemMeta())
//                .forEach(stack -> ExecutorService.handleForGadget(event, event.getPlayer(), stack));
//    }
//
//    /**
//     * Execute the event for all {@link uk.mangostudios.items.gadgets.Gadget} that are registered in the plugin that use this event
//     *
//     * @param event The event being used
//     */
//    @EventHandler(priority = EventPriority.LOW)
//    public void handleJump(PlayerJumpEvent event) {
//        if (event.getPlayer().getInventory().isEmpty()) return;
//
//        Arrays.stream(event.getPlayer().getInventory().getContents())
//                .filter(stack -> stack != null && stack.getType() != Material.AIR && stack.hasItemMeta())
//                .forEach(stack -> ExecutorService.handleForGadget(event, event.getPlayer(), stack));
//    }
//
//    /**
//     * Execute the event for all {@link uk.mangostudios.items.gadgets.Gadget} that are registered in the plugin that use this event
//     *
//     * @param event The event being used
//     */
//    @EventHandler(priority = EventPriority.LOW)
//    public void handlePlayerDropItem(PlayerDropItemEvent event) {
//        // we might want to do this for the entire inv
//        ExecutorService.handleForGadget(event, event.getPlayer(), event.getItemDrop().getItemStack());
//    }
//
//    /**
//     * Execute the event for all {@link uk.mangostudios.items.gadgets.Gadget} that are registered in the plugin that use this event
//     *
//     * @param event The event being used
//     */
//    @EventHandler(priority = EventPriority.LOW)
//    public void handlePlayerConsume(PlayerItemConsumeEvent event) {
//        // we might want to do this for the entire inv
//        ExecutorService.handleForGadget(event, event.getPlayer(), event.getItem());
//    }
//    // endregion 

}
