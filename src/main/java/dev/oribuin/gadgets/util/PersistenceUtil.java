package dev.oribuin.gadgets.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.morepersistentdatatypes.datatypes.collections.MapDataType;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.node.GadgetType;
import dev.oribuin.gadgets.node.impl.logistics.NodeGrid;
import dev.oribuin.gadgets.node.impl.logistics.type.FilterType;
import dev.oribuin.gadgets.node.storage.Hologram;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.jeff_media.morepersistentdatatypes.DataType.BOOLEAN;
import static com.jeff_media.morepersistentdatatypes.DataType.DOUBLE;
import static com.jeff_media.morepersistentdatatypes.DataType.FLOAT;
import static com.jeff_media.morepersistentdatatypes.DataType.INTEGER;
import static com.jeff_media.morepersistentdatatypes.DataType.ITEM_STACK;
import static com.jeff_media.morepersistentdatatypes.DataType.LOCATION;
import static com.jeff_media.morepersistentdatatypes.DataType.STRING;
import static com.jeff_media.morepersistentdatatypes.DataType.UUID;

public final class PersistenceUtil {

    private static final Cache<Location, CustomBlockData> DATA_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    // region Long Data Types
    public static final MapDataType<Map<Integer, ItemStack>, Integer, ItemStack> NODE_INVENTORY_TYPE = DataType.asMap(
            INTEGER,
            ITEM_STACK
    );
    // endregion

    // region Handheld Gadget Items
    public static GadgetType<String, String> GADGET_IDENTIFIER = register("gadget_identifier", STRING);
    // endregion

    // region Node Namespaces 
    public static GadgetType<String, String> NODE_TYPE = register("node_type", STRING);
    public static GadgetType<Byte, Boolean> NODE_NEXO = register("node_nexo", BOOLEAN);
    public static GadgetType<byte[], Location> NODE_CONTROLLER = register("node_controller", LOCATION);
    public static GadgetType<String, BlockFace> NODE_DIRECTION = register("node_direction", DataType.asEnum(BlockFace.class));
    public static GadgetType<String, FilterType> NODE_FILTER_TYPE = register("node_filter_type", DataType.asEnum(FilterType.class));
    public static GadgetType<PersistentDataContainer, Set<ItemStack>> NODE_FILTER_CONTENT = register("node_filters", DataType.asSet(ITEM_STACK));
    public static GadgetType<byte[], ItemStack[]> NODE_FILTER_CONTENT_ARRAY = register("node_filters", DataType.ITEM_STACK_ARRAY);

    public static GadgetType<Byte, Boolean> NODE_FILTER_IGNORE_META = register("node_ignore_meta", BOOLEAN);
    public static GadgetType<Integer, Integer> NODE_CHANNEL = register("node_channel", INTEGER);
    public static GadgetType<Integer, Integer> NODE_PRIORITY = register("node_priority", INTEGER);
    public static GadgetType<Integer, Integer> NODE_STOCK_CAPACITY = register("node_stock_capacity", INTEGER); // (5 = 320 Items/5 Stacks)
    public static GadgetType<Integer, Integer> NODE_INVENTORY_ROWS = register("node_inventory_rows", INTEGER);
    public static GadgetType<byte[], ItemStack[]> NODE_INVENTORY_INPUT = register("node_inventory_input", DataType.ITEM_STACK_ARRAY);
    public static GadgetType<byte[], ItemStack[]> NODE_INVENTORY_OUTPUT = register("node_inventory_output", DataType.ITEM_STACK_ARRAY);
    public static GadgetType<String, String> NODE_SEARCH = register("node_search", STRING);
    public static GadgetType<String, NodeGrid.SortType> NODE_GRID_SORT = register("node_grid_sort", DataType.asEnum(NodeGrid.SortType.class));
    public static GadgetType<Integer, Integer> NETWORK_POWER = register("node_power", INTEGER);
    public static GadgetType<PersistentDataContainer, Map<Integer, ItemStack>> NODE_INVENTORY = register(
            "node_inventory",
            NODE_INVENTORY_TYPE
    );
    // endregion 

    // region Deep Barrel Namespaces
    public static GadgetType<byte[], ItemStack> BARREL_ITEM = register("barrel_stored_item", ITEM_STACK); // ItemStack
    public static GadgetType<Integer, Integer> BARREL_AMOUNT = register("barrel_stored_amount", INTEGER); // Integer, Could be Long
    public static GadgetType<Integer, Integer> BARREL_MAX_AMOUNT = register("barrel_max_amount", INTEGER); // Integer, Could be Long
    public static GadgetType<Byte, Boolean> BARREL_VOID_EXCESS = register("barrel_void_excess", BOOLEAN); // Boolean
    // endregion

    // region Enhanced Furnace Namespaces
    public static GadgetType<Double, Double> ENHANCED_FURNACE_MULTIPLIER = register("enhanced_furnace_multiplier", DOUBLE); // Double
    // endregion

    // region Hologram Display Namespaces
    public static GadgetType<byte[], UUID> HOLOGRAM_ENTITY = register("hologram_entity", UUID); // UUID
    public static GadgetType<byte[], Location> HOLOGRAM_POSITION = register("hologram_position", LOCATION); // String(world/x/y/z/pitch)
    public static GadgetType<String, String> HOLOGRAM_TEXT = register("hologram_text", STRING); // List<Component>
    public static GadgetType<Byte, Boolean> HOLOGRAM_BACKGROUND = register("hologram_background", BOOLEAN); // Boolean
    public static GadgetType<String, Display.Billboard> HOLOGRAM_BILLBOARD = register("hologram_billboard", DataType.asEnum(Display.Billboard.class));  // Billboard
    public static GadgetType<Float, Float> HOLOGRAM_SCALE = register("hologram_scale", FLOAT); // Float (max 2.0)
    public static GadgetType<Byte, Boolean> HOLOGRAM_SHADOW = register("hologram_shadow", BOOLEAN); // Boolean
    public static GadgetType<String, Hologram.Rotation> HOLOGRAM_ROTATION = register("hologram_rotation", DataType.asEnum(Hologram.Rotation.class)); // HologramRotation
    // endregion

    // region Chunk Hopper Namespaces
    public static GadgetType<Double, Double> VACUUM_RADIUS = register("vacuum_radius", DOUBLE); // Might not be used
    // endregion

    /**
     * Returns the CustomBlockData related to the Block
     *
     * @param block The desired block
     * @return The access CustomBlockData
     */
    public static CustomBlockData accessBlockData(Block block) {
        if (block == null) return null;

        try {
            return DATA_CACHE.get(block.getLocation(), () -> new CustomBlockData(block, GadgetsPlugin.get()));
        } catch (ExecutionException ex) {
            return new CustomBlockData(block, GadgetsPlugin.get());
        }
    }

    /**
     * Returns the CustomBlockData version of a PersistentDataContainer
     *
     * @param container The desired container
     * @return The access CustomBlockData
     */
    public static CustomBlockData accessBlockData(PersistentDataContainer container) {
        if (container instanceof CustomBlockData blockData) return blockData;
        else return null;
    }

    /**
     * A utility function to shorten the namespace key
     *
     * @param name The namespace to register
     * @return The resulting namespace
     */
    public static <P, C> GadgetType<P, C> register(String name, PersistentDataType<P, C> dataType) {
        return new GadgetType<>(name, dataType);
    }

}
