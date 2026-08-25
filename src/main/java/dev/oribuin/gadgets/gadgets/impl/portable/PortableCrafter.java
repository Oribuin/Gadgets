package dev.oribuin.gadgets.gadgets.impl.portable;

import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class PortableCrafter extends Gadget {

    public PortableCrafter() {
        super("portable_crafter", ItemConstruct.of(Material.ANVIL)
                .setName("<#93bc80><bold>Portable Anvil")
                .setLore("", " <white>| <gray>A crafting table available anywhere")
        );
        this.register(PlayerInteractEvent.class, this::handleInteract);
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, ItemStack> provider) {
        PlayerInteractEvent event = provider.event();
        if (event.useItemInHand() == Event.Result.DENY || event.useInteractedBlock() == Event.Result.DENY) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        Inventory inventory = Bukkit.createInventory(null, InventoryType.WORKBENCH);
        player.openInventory(inventory);
    }

}