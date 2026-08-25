package dev.oribuin.gadgets.node.logistics;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.node.Node;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;

import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_INVENTORY;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_INVENTORY_ROWS;

@ConfigSerializable
public abstract class StorageNode extends Networked {

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public StorageNode(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.registerType(NODE_INVENTORY, new HashMap<>());
        this.registerType(NODE_INVENTORY_ROWS, 6);
    }

}
