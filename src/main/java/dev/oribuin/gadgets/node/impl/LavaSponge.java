package dev.oribuin.gadgets.node.impl;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.function.Supplier;

public final class LavaSponge extends Node {

    // todo: make config option
    private static final int DIRECTION_X = 6;
    private static final int DIRECTION_Y = 6;
    private static final int DIRECTION_Z = 6;

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public LavaSponge(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.item = ItemConstruct.of(Material.RESIN_BLOCK)
                .setName("<white>[<#93bc80><bold>Lava Sponge</bold><white>]")
                .setLore("",
                        " <white>| <gray>Can be used to clear an",
                        " <white>| <gray>area of lava like a",
                        " <white>| <gray>regular sponge"
                );

        this.register(BlockPlaceEvent.class, this::handleBlockPlace);
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockPlace(ContextProvider<BlockPlaceEvent, Block> provider) {
        BlockPlaceEvent event = provider.event();
        Location location = event.getBlock().getLocation();

        // TODO: Protection plugin check
        for (int y = -DIRECTION_Y; y < DIRECTION_Y; y++) {
            for (int x = -DIRECTION_X; x < DIRECTION_X; x++) {
                for (int z = -DIRECTION_Z; z < DIRECTION_Z; z++) {
                    Block found = location.getWorld().getBlockAt(location.clone().add(x, y, z));
                    if (found.getType() != Material.LAVA) continue;
                    if (!PlayerUtil.townCheck(event.getPlayer(), block)) continue;
                    found.setType(Material.AIR, false);
                }
            }
        }
    }

    // todo: prevent burn

    /**
     * Get the identifier of a node type
     *
     * @return The node type identifier
     */
    @Override
    public Supplier<String> getIdentifier() {
        return NodeFactory.LAVA_SPONGE::identifier;
    }

    /**
     * Load a {@link NodeType.Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
     *
     * @param container The container to load from
     * @return The resulting type if available
     */
    public static LavaSponge deserialize(Block block, PersistentDataContainer container) {
        return new LavaSponge(block, PersistenceUtil.accessBlockData(container));
    }

}
