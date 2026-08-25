package dev.oribuin.gadgets.listener;

import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.nexomc.nexo.mechanics.Mechanic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class NexoListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onFurnitureInteract(final NexoFurnitureInteractEvent event) {
        Player player = event.getPlayer();
        Mechanic mechanic = event.getMechanic();

//        ExecutorService.executeFor(event, player, mechanic.getItemID());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteract(final NexoNoteBlockInteractEvent event) {
        Player player = event.getPlayer();
        Mechanic mechanic = event.getMechanic();

//        ExecutorService.executeFor(event, player, mechanic.getItemID());
    }
}
