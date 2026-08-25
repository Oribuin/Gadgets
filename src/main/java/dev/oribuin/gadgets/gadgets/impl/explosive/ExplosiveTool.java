package dev.oribuin.gadgets.gadgets.impl.explosive;

import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class ExplosiveTool extends ExplosiveItem {

    public ExplosiveTool() {
        super("explosive_tool");

        this.items.put("explosive_pickaxe", ItemConstruct.of(Material.DIAMOND_PICKAXE)
                .setName("<#93bc80><bold>Explosive Pickaxe")
                .setLore("",
                        " <white>| <gray>Destroys nearby blocks within a 3x3 radius"
                ));

        this.items.put("explosive_shovel", ItemConstruct.of(Material.DIAMOND_SHOVEL)
                .setName("<#93bc80><bold>Explosive Pickaxe")
                .setLore("",
                        " <white>| <gray>Destroys nearby blocks within a 3x3 radius"
                ));

        this.radius = 3;
        this.register(BlockBreakEvent.class, this::handleBlockBreak);
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
            this.explodeBlock(player, event, this.radius / 2, this.blacklistTypes);
        }
    }

}
