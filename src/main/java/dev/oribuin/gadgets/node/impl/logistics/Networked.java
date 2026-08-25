package dev.oribuin.gadgets.node.impl.logistics;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType.Serializer;
import dev.oribuin.gadgets.node.storage.NodeProvider;
import dev.oribuin.gadgets.util.InventoryUtils;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.block.FinePosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_CONTROLLER;

public abstract class Networked extends Node {

    private FinePosition controller;

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public Networked(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.registerType(NODE_CONTROLLER, null);
        this.register(BlockPlaceEvent.class, this::handleBlockPlace);
        this.register(BlockBreakEvent.class, this::handleBlockBreak);
        this.register(PlayerInteractEvent.class, this::handleInteract);
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
        if (this.getControllerPosition() == null) {
            FinePosition position = this.findController(provider.type().getLocation());
            if (position != null) {
                this.setValue(NODE_CONTROLLER, position.toLocation());
                this.serialize();
            }
        }
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockPlace(ContextProvider<BlockPlaceEvent, Block> provider) {
        BlockPlaceEvent event = provider.event();

        FinePosition controller = this.findController(event.getBlock().getLocation());
        if (controller == null) return;

        NodeController nodeController = NodeController.from(controller);
        if (nodeController == null) return;

        // Load the networked block into the controller
        this.setValue(NODE_CONTROLLER, controller.toLocation());
        this.serialize();
        NodeProvider.instance().store(nodeController);

        // Serialize the controller inside the block
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockBreak(ContextProvider<BlockBreakEvent, Block> provider) {
        FinePosition position = this.getControllerPosition();
        if (position != null) {
            NodeController nodeController = NodeController.from(position);
            if (nodeController == null) return;

            nodeController.getConnected().remove(FinePosition.from(this.block));
            NodeProvider.instance().store(nodeController);

            // Serialize the controller inside the block
            this.setValue(NODE_CONTROLLER, null);
            this.serialize();
        }
    }

    public void tick() {
    }

    /**
     * Store a {@link Serializer} into a {@link PersistentDataContainer}
     *
     * @param container The container to store the serializer in
     */
    @Override
    public <T extends PersistentDataContainer> void serialize(T container) {
        super.serialize(container);

        // region Cache the node into the Controller to prevent excess loading
        NodeController controller = this.getController();
        if (controller != null) {
            controller.getConnected().add(FinePosition.from(this.block));
            controller.serialize();
        }
        // endregion
    }

    /**
     * Find a controller from a base position
     *
     * @param original The original block position
     * @return The resulting controller {@link FinePosition} if available
     */
    public FinePosition findController(Location original) {
        for (BlockFace face : InventoryUtils.VALID_FACES) {
            Location position = original.clone().add(face.getDirection());
            Block block = position.getBlock();
            if (block.getType() == Material.AIR) continue;

            Node node = NodeFactory.from(block);

            // It found the controller directly
            if (node instanceof NodeController) return FinePosition.from(block);
            if (!(node instanceof Networked networked)) continue;

            FinePosition controller = networked.getControllerPosition();
            if (controller != null) return controller;
        }

        return this.getControllerPosition();
    }

    public final FinePosition getControllerPosition() {
        if (this.controller != null) return this.controller;
        if (this.block == null) return null;

        this.controller = FinePosition.from(this.getValue(PersistenceUtil.NODE_CONTROLLER));
        return this.controller;
    }

    public final NodeController getController() {
        return NodeController.from(this.getControllerPosition());
    }

}
