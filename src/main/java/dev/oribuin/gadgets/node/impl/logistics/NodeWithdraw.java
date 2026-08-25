package dev.oribuin.gadgets.node.impl.logistics;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.container.ContainerProvider;
import dev.oribuin.gadgets.container.ContainerWrapper;
import dev.oribuin.gadgets.container.transaction.ItemTransferRequest;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.gui.impl.node.ComplexNodeGui;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.node.impl.logistics.type.Depositing;
import dev.oribuin.gadgets.node.impl.logistics.type.FilterType;
import dev.oribuin.gadgets.node.impl.logistics.type.Tickable;
import dev.oribuin.gadgets.node.impl.logistics.type.Withdrawing;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Channelled;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Directional;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Filtered;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Priority;
import dev.oribuin.gadgets.scheduler.PluginScheduler;
import dev.oribuin.gadgets.util.InventoryUtils;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.block.FinePosition;
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
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_CHANNEL;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_CONTROLLER;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_DIRECTION;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_FILTER_CONTENT;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_FILTER_IGNORE_META;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_FILTER_TYPE;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_PRIORITY;

public class NodeWithdraw extends Networked implements Withdrawing, Directional, Filtered, Channelled, Tickable {

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public NodeWithdraw(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.item = ItemConstruct.of(Material.RED_STAINED_GLASS)
                .setName("<white>[<#93bc80><bold>" + this.getIdentifier().get() + "</bold><white>]")
                .setLore("", " <white>| <gray>Does Something!");

        this.registerType(NODE_DIRECTION, BlockFace.SELF);
        this.registerType(NODE_FILTER_TYPE, FilterType.BLACKLIST);
        this.registerType(NODE_FILTER_CONTENT, new HashSet<>());
        this.registerType(NODE_FILTER_IGNORE_META, false);
        this.registerType(NODE_CHANNEL, 1);
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
        super.handleInteract(provider);
        PlayerInteractEvent event = provider.event();
        if (event.getPlayer().isSneaking()) return;

        ComplexNodeGui gui = new ComplexNodeGui(event.getPlayer(), GadgetsPlugin.get(), this);
        PluginScheduler.get().runTaskAtEntity(event.getPlayer(), () -> gui.open(event.getPlayer()));
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockBreak(ContextProvider<BlockBreakEvent, Block> provider) {
        super.handleBlockBreak(provider);
        this.setValue(NODE_CONTROLLER, null);
        this.setValue(NODE_DIRECTION, BlockFace.SELF);
        this.setValue(NODE_FILTER_TYPE, FilterType.BLACKLIST);
        this.setValue(NODE_FILTER_CONTENT, new HashSet<>());
        this.setValue(NODE_FILTER_IGNORE_META, false);
        this.setValue(NODE_PRIORITY, 1);
        this.setValue(NODE_CHANNEL, 1);
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockPlace(ContextProvider<BlockPlaceEvent, Block> provider) {
        super.handleBlockPlace(provider);

        BlockPlaceEvent event = provider.event();
        BlockFace relative = InventoryUtils.getDirection(event.getBlock(), event.getBlockAgainst());
        if (relative != null) {
            FinePosition position = this.findController(event.getBlock().getLocation());
            if (position != null) {
                this.setValue(NODE_CONTROLLER, position.toLocation());
            }

            this.setValue(NODE_DIRECTION, relative);
            this.serialize(this.getData());
        }
    }

    @Override
    public void tick() {
        if (this.block == null || this.block.getType().isAir()) return;

        BlockFace direction = this.getDirection();
        if (direction == null || direction == BlockFace.SELF) return;

        Block relative = this.block.getRelative(direction);

        // container should be loaded async but vanilla containers requiring sync is annoying
        ContainerWrapper withdrawContainer = ContainerProvider.from(relative);
        if (withdrawContainer == null || withdrawContainer.isEmpty()) return;

        NodeController controller = this.getController();
        if (controller == null) return;

        ItemStack stack = withdrawContainer.getFirst(this);
        if (stack == null || !this.getFilterType().canAccept(this, stack)) return;

        ItemTransferRequest request = new ItemTransferRequest(
                stack.clone(),
                stack.getAmount(),
                FinePosition.from(this.block)
        );

        controller.getPendingTransfers().add(request);

        // region Distribute the request to all conne cted networks that deposit items
        List<Networked> outputs = new ArrayList<>(controller.getFiltered(x -> x instanceof Depositing));
        outputs.sort((o1, o2) -> {
            if (o1 instanceof Priority firstPriority && o2 instanceof Priority secPriority) {
                return Integer.compare(firstPriority.getPriority(), secPriority.getPriority());
            }
            return 0;
        });

        for (Networked networked : outputs) {
            Depositing depositing = (Depositing) networked;
            if (depositing.deposit()) break;
        }

        // Remove the request once finished to not hold it 
        controller.getPendingTransfers().remove(request);

        // endregion
    }

    /**
     * Get the identifier of a node type
     *
     * @return The node type identifier
     */
    @Override
    public Supplier<String> getIdentifier() {
        return NodeFactory.NODE_WITHDRAW::identifier;
    }

    /**
     * Load a {@link NodeType.Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
     *
     * @param container The container to load from
     * @return The resulting type if available
     */
    public static NodeWithdraw deserialize(Block block, PersistentDataContainer container) {
        return new NodeWithdraw(block, PersistenceUtil.accessBlockData(container));
    }

    /**
     * Get the {@link BlockFace} direction of a node
     *
     * @return The direction
     */
    @Override
    public BlockFace getDirection() {
        return this.getValue(NODE_DIRECTION);
    }

    /**
     * Set a {@link BlockFace} direction for a node
     *
     * @param direction The direction of the node
     */
    @Override
    public void setDirection(BlockFace direction) {
        this.setValue(NODE_DIRECTION, direction);
    }

    /**
     * Get the channel id for the node
     *
     * @return The node channel id
     */
    @Override
    public int getChannel() {
        return this.getValue(NODE_CHANNEL, 1);
    }

    /**
     * Set a node's priority
     *
     * @param channel The channel id
     */
    @Override
    public void setChannel(int channel) {
        this.setValue(NODE_CHANNEL, channel);
    }

    /**
     * Get all the filtered items inside the container
     *
     * @return The filtered itemstacks
     */
    @Override
    public Set<ItemStack> getFilterContent() {
        return this.getValue(NODE_FILTER_CONTENT);
    }

    /**
     * Set a list of filtered itemstacks
     *
     * @param filterContent The itemstacks that will be filtered
     */
    @Override
    public void setFilterContent(@NonNull Set<ItemStack> filterContent) {
        this.setValue(NODE_FILTER_CONTENT, filterContent);
    }

    /**
     * Get the type of filter
     *
     * @return The type of filter
     */
    @Override
    public @NonNull FilterType getFilterType() {
        return this.getValue(NODE_FILTER_TYPE, FilterType.BLACKLIST);
    }

    /**
     * Set a filter type for the filter
     *
     * @param filterType The type of filter being set
     */
    @Override
    public void setFilterType(@NonNull FilterType filterType) {
        this.setValue(NODE_FILTER_TYPE, filterType);
    }

    /**
     * If the filter should ignore meta (just go off type alone. This should not work with barrels at ALL)
     *
     * @return Whether the filter should ignore meta
     */
    @Override
    public boolean isIgnoreMeta() {
        return this.getValue(NODE_FILTER_IGNORE_META, false);
    }

    /**
     * Set whether the filter should ignore meta (just go off type alone)
     *
     * @param ignoreMeta Whether the item is ignoring meta
     */
    @Override
    public void setIgnoreMeta(boolean ignoreMeta) {
        this.setValue(NODE_FILTER_IGNORE_META, ignoreMeta);
    }

}
