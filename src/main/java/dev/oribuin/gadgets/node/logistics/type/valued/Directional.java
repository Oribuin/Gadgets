package dev.oribuin.gadgets.node.logistics.type.valued;

import org.bukkit.block.BlockFace;

public interface Directional {

    /**
     * Get the {@link BlockFace} direction of a node
     *
     * @return The direction
     */
    BlockFace getDirection();

    /**
     * Set a {@link BlockFace} direction for a node
     *
     * @param direction The direction of the node
     */
    void setDirection(BlockFace direction);

}
