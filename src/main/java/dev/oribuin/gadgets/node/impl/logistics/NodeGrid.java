package dev.oribuin.gadgets.node.impl.logistics;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.gui.impl.auto.NetworkGridGUI;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.scheduler.PluginScheduler;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.oribuin.gadgets.util.PersistenceUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_GRID_SORT;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_SEARCH;

public class NodeGrid extends Networked {

    private SortType sort = SortType.ALPHABETICAL; // TODO: Add to config
    private String search = null;

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public NodeGrid(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.item = ItemConstruct.of(Material.NOTE_BLOCK)
                .setName("<white>[<#93bc80><bold>" + this.getIdentifier().get() + "</bold><white>]")
                .setLore("", " <white>| <gray>Does Something!");

        this.registerType(NODE_GRID_SORT, this.sort);
        this.registerType(NODE_SEARCH, this.search);
    }

    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
        super.handleInteract(provider);
        PlayerInteractEvent event = provider.event();
        if (event.getPlayer().isSneaking()) return;
        System.out.println("Opening grid GUI for " + this.getIdentifier().get());

        NetworkGridGUI gui = new NetworkGridGUI(GadgetsPlugin.get(), this, event.getPlayer(), null);
        PluginScheduler.get().runTaskAtEntity(event.getPlayer(), () -> gui.open(event.getPlayer()));
    }

    /**
     * Get the identifier of a node type
     *
     * @return The node type identifier
     */
    @Override
    public Supplier<String> getIdentifier() {
        return NodeFactory.NODE_GRID::identifier;
    }

    /**
     * Load a {@link NodeType.Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
     *
     * @param container The container to load from
     * @return The resulting type if available
     */
    public static NodeGrid deserialize(Block block, PersistentDataContainer container) {
        return new NodeGrid(block, PersistenceUtil.accessBlockData(container));
    }

    public SortType getSort() {
        return sort;
    }

    public NodeGrid setSort(SortType sort) {
        this.sort = sort;
        return this;
    }

    public String getSearch() {
        return search;
    }

    public NodeGrid setSearch(String search) {
        this.search = search;
        return this;
    }

    public enum SortType {
        ALPHABETICAL((o1, o2) -> {
            PlainTextComponentSerializer plain = MessageHandler.PLAIN_TEXT;
            String first = plain.serialize(o1.displayName());
            String second = plain.serialize(o2.displayName());
            return first.compareToIgnoreCase(second);
        }),
        QUANTITY((o1, o2) -> {
            Integer amount = o1.getAmount();
            return amount.compareTo(o2.getAmount());
        }),
        ALPHABETICAL_INVERTED((o1, o2) -> {
            PlainTextComponentSerializer plain = MessageHandler.PLAIN_TEXT;
            String first = plain.serialize(o1.displayName());
            String second = plain.serialize(o2.displayName());
            return second.compareToIgnoreCase(first);
        }),
        QUANTITY_INVERTED((o1, o2) -> {
            Integer amount = o1.getAmount();
            return amount.compareTo(o2.getAmount()) * -1;
        });

        private final Comparator<ItemStack> sort;

        SortType(Comparator<ItemStack> sort) {
            this.sort = sort;
        }

        public Comparator<ItemStack> getSort() {
            return sort;
        }
    }

}
