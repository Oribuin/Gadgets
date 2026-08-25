package dev.oribuin.gadgets.gadgets.impl.explosive;

import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.EnumSet;
import java.util.Set;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class ExplosiveHoe extends ExplosiveItem {
    protected Set<Material> farmingHoeList = EnumSet.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.NETHER_WART
    );

    public ExplosiveHoe() {
        super("explosive_hoe", ItemConstruct.of(Material.DIAMOND_HOE)
                .setName("<#93bc80><bold>Explosive Farming Hoe")
                .setLore("",
                        " <white>| <gray>Tills farm land or destroys nearby crops"
                )
        );
        this.register(BlockBreakEvent.class, this::handleBlockBreak);
        this.register(PlayerInteractEvent.class, this::handleInteract);
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockBreak(ContextProvider<BlockBreakEvent, ItemStack> provider) {
        BlockBreakEvent event = provider.event();
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            this.explodeFarm(player, event, this.radius / 2, this.farmingHoeList);
        }
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, ItemStack> provider) {
        PlayerInteractEvent event = provider.event();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getPlayer().isSneaking()) return;

        this.tillFarm(event.getPlayer(), event, this.radius / 2);
    }

}
