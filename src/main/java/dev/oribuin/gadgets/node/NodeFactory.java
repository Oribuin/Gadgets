package dev.oribuin.gadgets.node;

import com.jeff_media.morepersistentdatatypes.DataType;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.node.impl.DeepStorageBarrel;
import dev.oribuin.gadgets.node.impl.EnhancedFurnace;
import dev.oribuin.gadgets.node.impl.HologramProjector;
import dev.oribuin.gadgets.node.impl.LavaSponge;
import dev.oribuin.gadgets.node.logistics.Networked;
import dev.oribuin.gadgets.node.logistics.NodeConnector;
import dev.oribuin.gadgets.node.logistics.NodeController;
import dev.oribuin.gadgets.node.logistics.NodeDeposit;
import dev.oribuin.gadgets.node.logistics.NodeGrid;
import dev.oribuin.gadgets.node.logistics.NodeWithdraw;
import dev.oribuin.gadgets.node.storage.NodeProvider;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.block.FinePosition;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_TYPE;

public final class NodeFactory {
    
    public static final Map<String, NodeType<?>> REGISTRY = new HashMap<>();

    // region Regular Nodes
    public static final NodeType<DeepStorageBarrel> DEEP_BARREL = register(DeepStorageBarrel.class, "deep_barrel", DeepStorageBarrel::deserialize);
    public static final NodeType<EnhancedFurnace> ENHANCED_FURNACE = register(EnhancedFurnace.class, "enhanced_furnace", EnhancedFurnace::deserialize);
    public static final NodeType<HologramProjector> HOLOGRAM_PROJECTOR = register(HologramProjector.class, "hologram_projector", HologramProjector::deserialize);
    public static final NodeType<LavaSponge> LAVA_SPONGE = register(LavaSponge.class, "lava_sponge", LavaSponge::deserialize);
    // endregion

    // region Logistics Nodes
    public static final NodeType<NodeController> NODE_CONTROLLER = register(NodeController.class, "node_controller", NodeController::deserialize);
    public static final NodeType<NodeConnector> NODE_CONNECTOR = register(NodeConnector.class, "node_connector", NodeConnector::deserialize);
    public static final NodeType<NodeDeposit> NODE_DEPOSIT = register(NodeDeposit.class, "node_deposit", NodeDeposit::deserialize);
    public static final NodeType<NodeWithdraw> NODE_WITHDRAW = register(NodeWithdraw.class, "node_withdraw", NodeWithdraw::deserialize);
    public static final NodeType<NodeGrid> NODE_GRID = register(NodeGrid.class, "node_grid", NodeGrid::deserialize);
    // endregion

    // region Unused 
    // public static final NodeType<NodeCell> NODE_CELL = register(NodeCell.class, "node_cell", NodeCell::deserialize);
    // endregion 

    public static void init() {
        // java does not load a static class until it's referenced at least once
    }

    public static <T extends Node> ItemStack create(NodeType<T> type, int amount) {
        Node node = type.deserializer().apply(null, null);
        ItemStack stack = node.establishItem(amount);
        stack.editPersistentDataContainer(data -> type.deserializer().apply(null, data));
        return stack;
    }


    /**
     * Load a node from a block in the plugin
     *
     * @param block The block the data is stored inside
     * @param <T>   The type of node being stored
     * @return The resulting node if available
     */
    @SuppressWarnings("unchecked")
    public static <T extends Node> T from(Block block) {
        return from(block, PersistenceUtil.accessBlockData(block));
    }

    /**
     * Load a node from an existing block in the plugin
     *
     * @param block     The block to load from
     * @param container The container the data is stored inside
     * @param <T>       The type of node being stored
     * @return The resulting node if available
     */
    @SuppressWarnings("unchecked")
    public static <T extends Node> T from(Block block, PersistentDataContainer container) {
        String identifier = container.get(NODE_TYPE.key(), DataType.STRING);
        if (identifier == null) return null;

        NodeType<T> nodeType = (NodeType<T>) REGISTRY.get(identifier.toLowerCase());
        FinePosition position = FinePosition.from(block);

        return NodeProvider.instance().from(nodeType.nodeClass(), position, () -> nodeType.deserializer().apply(
                block,
                container
        ));
    }

    /**
     * Register a {@link NodeType} into the plugin to be used to load a {@link Node}
     *
     * @param identifier The identifier of the node
     * @param <T>        The node that is associated with this type
     * @return The resulting node if available
     */
    public static <T extends Node> NodeType<T> register(
            Class<T> nodeClass,
            String identifier,
            BiFunction<Block, PersistentDataContainer, T> deserializer
    ) {
        return register(
                identifier, new NodeType<>(nodeClass,
                        identifier,
                        (data, node) -> node.serialize(data),
                        deserializer
                ));
    }

    /**
     * Register a {@link NodeType} into the plugin to be used to load a {@link Node}
     *
     * @param identifier   The identifier of the node
     * @param serializer   The method used to serialize the {@link NodeType}
     * @param deserializer The method used to deserialize the {@link NodeType}
     * @param <T>          The type of node being loaded
     * @return The resulting node type
     */
    public static <T extends Node> NodeType<T> register(
            Class<T> nodeClass,
            String identifier,
            BiConsumer<? extends PersistentDataContainer, T> serializer,
            BiFunction<Block, PersistentDataContainer, T> deserializer
    ) {
        return register(identifier, new NodeType<>(nodeClass, identifier, serializer, deserializer));
    }

    /**
     * Register a {@link NodeType} into the plugin to be used to load a {@link Node}
     *
     * @param identifier The identifier of the node
     * @param type       The node serialize/deserialize type
     * @param <T>        The node that is associated with this type
     * @return The resulting node if available
     */
    public static <T extends Node> NodeType<T> register(String identifier, NodeType<T> type) {
        REGISTRY.put(identifier.toLowerCase(), type);

        // TODO: Load the node from a FileConfiguration
        return type;
    }

    /**
     * Check if a container belongs to a node
     *
     * @param container The container to check
     * @param type      The type of node that the container could be from
     * @param <T>       The node type
     * @return Whether the container belongs to the specified node
     */
    public static <T extends Node> boolean isTypeOf(@NotNull PersistentDataContainer container, @NotNull NodeType<T> type) {
        String identifier = container.get(NODE_TYPE.key(), DataType.STRING);
        if (identifier == null) return false;

        return type.identifier().equalsIgnoreCase(identifier);
    }

    /**
     * Check if a ItemStack belongs to a node
     *
     * @param stack The ItemStack to check
     * @param type  The type of node that the container could be from
     * @param <T>   The node type
     * @return Whether the container belongs to the specified node
     */
    public static <T extends Node> boolean isTypeOf(@NotNull ItemStack stack, @NotNull NodeType<T> type) {
        if (stack.getType().isAir() || !stack.hasItemMeta()) return false;

        return isTypeOf(stack.getItemMeta().getPersistentDataContainer(), type);
    }

    /**
     * Check if a Block belongs to a node
     *
     * @param block The Block to check
     * @param type  The type of node that the container could be from
     * @param <T>   The node type
     * @return Whether the container belongs to the specified node
     */
    public static <T extends Node> boolean isTypeOf(@NotNull Block block, @NotNull NodeType<T> type) {
        if (block.getType().isAir()) return false;

        return isTypeOf(PersistenceUtil.accessBlockData(block), type);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> NodeType<T> from(String identifier) {
        if (identifier == null) return null;

        return (NodeType<T>) REGISTRY.get(identifier);
    }

}

