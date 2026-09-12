package dev.oribuin.gadgets.node;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.api.event.EventHandler;
import dev.oribuin.gadgets.api.event.NodeEvents;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.node.storage.NodeProvider;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.Placeholder;
import dev.oribuin.gadgets.util.Placeholders;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.jeff_media.customblockdata.CustomBlockData.getDataType;
import static com.jeff_media.morepersistentdatatypes.DataType.STRING;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_TYPE;

public abstract class Node extends EventHandler implements NodeEvents, NodeType.Serializer, Placeholder {

    protected transient Map<GadgetType<?, ?>, NodeValue<?>> nodeValues;
    protected transient Block block;
    protected ItemConstruct item;

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public Node(@Nullable Block block, @Nullable CustomBlockData data) {
        this.block = block;
        this.item = ItemConstruct.of(Material.BLACK_STAINED_GLASS)
                .setName("<white>[<#93bc80><bold>" + this.getIdentifier().get() + "</bold><white>]")
                .setLore("", " <white>#94bc80><b>Gadgets</b> <dark_gray>▎ <gray>Does Something!");
        this.nodeValues = new HashMap<>();
    }

    /**
     * The functionality provided when the {@link Node} is requested in {@link ItemStack} form
     *
     * @param quantity The quantity of the {@link ItemStack}
     * @return The resulting {@link ItemStack}
     */
    public final ItemStack establishItem(int quantity) {
        return this.establishItem(null, quantity, Placeholders.empty());
    }

    /**
     * The functionality provided when the {@link Node} is requested in {@link ItemStack} form
     *
     * @param quantity     The quantity of the {@link ItemStack}
     * @param placeholders Any placeholders for the {@link Node}
     * @return The resulting {@link ItemStack}
     */
    public final ItemStack establishItem(int quantity, Placeholders placeholders) {
        return this.establishItem(null, quantity, placeholders);
    }

    /**
     * The functionality provided when the {@link Node} is requested in {@link ItemStack} form
     *
     * @param consumer     Any modifications that are wanted for the {@link ItemStack}
     * @param quantity     The quantity of the {@link ItemStack}
     * @param placeholders Any placeholders for the {@link Node}
     * @return The resulting {@link ItemStack}
     */
    public final ItemStack establishItem(Consumer<ItemStack> consumer, int quantity, Placeholders placeholders) {

        ItemStack stack = this.item.create(Placeholders.builder()
                .addAll(this.getPlaceholders().get())
                .addAll(placeholders)
                .build()
        );
        
        if (!stack.hasItemMeta()) return null;

        // Add the item id to the node
        stack.editPersistentDataContainer(this::serialize);

        // Add any additional modifications someone might want 
        if (consumer != null) consumer.accept(stack);
        return stack.asQuantity(quantity);
    }

    /**
     * Store a {@link NodeType.Serializer} into a {@link PersistentDataContainer}
     */
    public void serialize() {
        this.serialize(this.getData());
    }

    /**
     * Apply data to the node when it is dropped
     *
     * @return The item being dropped
     */
    public Consumer<ItemStack> modifyNodeDrop() {
        return stack -> stack.editPersistentDataContainer(this::transferTo);
    }

    /**
     * Set a specified value to the persistent data container of the block, Removes value if null
     *
     * @param key   The namespace key used to store the data
     * @param type  The data type
     * @param value the value being set to the data container
     * @param <P>   The first data type value
     * @param <C>   The value being set
     */
    protected <P, C> void set(NamespacedKey key, PersistentDataType<P, C> type, C value) {
        CustomBlockData data = this.getData();
        if (data == null) return;

        if (value == null) {
            data.remove(key);
            return;
        }

        data.set(key, type, value);
    }


    /**
     * Store a {@link NodeType.Serializer} into a {@link PersistentDataContainer}
     *
     * @param container The container to store the serializer in
     */
    @Override
    public <T extends PersistentDataContainer> void serialize(T container) {
        // region Serialize the data into the PDC Container
        container.set(NODE_TYPE.key(), STRING, this.getIdentifier().get());

        nodeValues.values().forEach(nodeValue -> {
            if (nodeValue.isDirty()) nodeValue.serialize(container);
        });
        // endregion

        if (this.block != null) {
            NodeProvider.instance().cache(this.block, this);
        }
    }

    /**
     * Store a {@link NodeType.Serializer} into a {@link PersistentDataContainer}
     *
     * @param container The container to store the serializer in
     */
    public <T extends PersistentDataContainer> void loadFrom(T container) {
        if (container == null) return;
        nodeValues.values().forEach(nodeValue -> nodeValue.deserialize(container));
    }

    /**
     * Get a value from a namespaced key
     *
     * @param gadgetType The key to load the value from
     * @param <T>        The node value type
     * @return The resulting value if available
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getValue(GadgetType<?, T> gadgetType) {
        CustomBlockData blockData = this.getData();
        NodeValue<T> nodeValue = (NodeValue<T>) this.nodeValues.get(gadgetType);

        // Check if the block data exists, if not use default node value
        T storedValue = nodeValue != null ? nodeValue.getValue() : null;
        if (blockData == null) return storedValue;

        // If the value from the block data does not match what's stored, update the stored value
        T blockDataValue = blockData.get(gadgetType.key(), gadgetType);
        if (blockDataValue != null && !blockDataValue.equals(storedValue)) {
            this.setValue(gadgetType, blockDataValue);
        }

        // Prefer using the block data value, otherwise use stored
        return blockDataValue != null ? blockDataValue : storedValue;
    }

    /**
     * Get a value from a namespaced gadgetType
     *
     * @param gadgetType The gadgetType to load the value from
     * @param <T>        The node value type
     * @return The resulting value if available
     */
    @NotNull
    public <T> T getValue(GadgetType<?, T> gadgetType, @NotNull T other) {
        T result = this.getValue(gadgetType);
        return result == null ? other : result;
    }

    /**
     * Set a value in the plugin to a specified value
     *
     * @param gadgetType The gadgetType to set
     * @param value      The value being modified
     * @param <T>        The type of value being set
     */
    @SuppressWarnings("unchecked")
    public <T> T setValue(GadgetType<?, T> gadgetType, @Nullable T value) {
        NodeValue<?> nodeValue = this.nodeValues.get(gadgetType);
        if (nodeValue == null) {
            GadgetsPlugin.get().getLogger().warning("Gadget Type[" + gadgetType.namespace() + "] is not registered as a note value");
            return  null;
        }

        NodeValue<T> result = (NodeValue<T>) nodeValue;
        result.setValue(value);
        this.nodeValues.put(gadgetType, result);
        return value;
    }

    /**
     * Register a stored value into the plugin to load/unload to or from the data container
     *
     * @param gadgetType   The utilized  gadgetType
     * @param defaultValue The default value for it
     * @param <T>          he stored data type
     * @return The resulting stored value
     */
    public <T> NodeValue<T> registerType(GadgetType<?, T> gadgetType, @Nullable T defaultValue) {
        NodeValue<T> result = new NodeValue<>(gadgetType, defaultValue);
        this.nodeValues.put(gadgetType, result);
        return result;
    }

    public void resetValues(GadgetType<?, ?>... values) {
        for (GadgetType<?, ?> gadgetType : values) {
            NodeValue<?> value = this.nodeValues.get(gadgetType);
            value.resetDefault();
            this.nodeValues.put(gadgetType, value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void transferTo(PersistentDataContainer container) {
        if (this.getData() == null) return;

        // Add the item id to the node
        this.getData().getKeys().forEach(key -> {
            PersistentDataType dataType = getDataType(this.getData(), key);
            if (dataType == null) return;
            Object result = this.getData().get(key, dataType);
            if (result != null) container.set(key, dataType, result);
        });
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public CustomBlockData getData() {
        return PersistenceUtil.accessBlockData(this.block);
    }

    public ItemConstruct getItem() {
        return item;
    }

    public void setItem(ItemConstruct item) {
        this.item = item;
    }

    public Map<GadgetType<?, ?>, NodeValue<?>> getNodeValues() {
        return nodeValues;
    }

}