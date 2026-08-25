package dev.oribuin.gadgets.gui.impl.auto;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.config.gui.GuiIcon;
import dev.oribuin.gadgets.config.impl.PluginMessages;
import dev.oribuin.gadgets.config.item.ConstructType;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.config.item.component.TooltipItemType;
import dev.oribuin.gadgets.container.ContainerProvider;
import dev.oribuin.gadgets.container.ContainerWrapper;
import dev.oribuin.gadgets.gui.api.PluginMenu;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.logistics.NodeController;
import dev.oribuin.gadgets.node.logistics.NodeGrid;
import dev.oribuin.gadgets.node.logistics.type.valued.Directional;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.oribuin.gadgets.util.Placeholders;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class NetworkGridGUI extends PluginMenu<NetworkGridGUI.NetworkGridConfig, PaginatedGui> {

    private static final Logger log = LoggerFactory.getLogger(NetworkGridGUI.class);
    private final NodeGrid grid;

    /**
     * Creates a new menu for the plugin to use
     *
     * @param plugin The plugin instance
     */
    public NetworkGridGUI(GadgetsPlugin plugin, NodeGrid grid, Player viewer, @Nullable String filter) {
        super(plugin, NetworkGridConfig.class);
        this.grid = grid;
        this.gui = this.createMenu().get();

        // Set GUI Icons & Functionality
        this.setDummyIcons();

        GuiIcon close = this.getConfig().getCloseButton();
        close.getSlots().forEach(slot ->
                this.gui.setItem(slot, close.withAction(event ->
                        this.gui.close(viewer))
                )
        );

        GuiIcon nextPage = this.getConfig().getNextPageButton();
        nextPage.getSlots().forEach(slot ->
                this.gui.setItem(slot, nextPage.withAction(event ->
                        this.gui.next()
                ))
        );

        GuiIcon previousPage = this.getConfig().getPreviousPageButton();
        previousPage.getSlots().forEach(slot ->
                this.gui.setItem(slot, previousPage.withAction(event ->
                        this.gui.previous()
                ))
        );

        // TODO: Set sort/search functionality
        GuiIcon sort = this.getConfig().getSortButton();
        sort.getSlots().forEach(slot ->
                this.gui.setItem(slot, sort.withAction(
                        Placeholders.of("sort", MessageHandler.getNiceEnum(this.grid.getSort())),
                        event -> {
                            // cycle through sort types
                            NodeGrid.SortType current = this.grid.getSort();
                            NodeGrid.SortType[] values = NodeGrid.SortType.values();
                            int nextOrdinal = (current.ordinal() + 1) % values.length;
                            NodeGrid.SortType next = values[nextOrdinal];
                            this.grid.setSort(next);

                            new NetworkGridGUI(plugin, grid, viewer, filter).open(viewer);
                        })
                )
        );

        // terrible system but WOMP WOMP
        NodeController controller = this.grid.getController();
        if (controller == null) return; // no items grr

        // TODO: Make this update routinely 
        controller.getNetworkItemsAsync().thenAccept(content -> {
            for (Map.Entry<ItemStack, Integer> entry : content.entrySet()) {
                ItemStack item = entry.getKey();

                // Apply filter if available
                if (filter != null) {
                    String itemName = PlainTextComponentSerializer.plainText().serialize(item.displayName()).toLowerCase();
                    if (!itemName.contains(filter.toLowerCase())) {
                        continue;
                    }
                }

                int amount = entry.getValue();

                ItemStack display = item.asOne();
                display.editMeta(meta -> meta.lore(MessageHandler.parse(
                        this.getConfig().getDisplayItemLore(), Placeholders.of(
                                "amount", amount,
                                "stack", Math.min(64, amount),
                                "single", Math.min(1, amount)
                        ))));

                gui.addItem(new GuiItem(display, event -> {
                    int withdrawCount = event.isLeftClick() ? Math.min(64, amount) : 1;

                    Node located = grid.getController().locate(item.asOne());
                    if (located == null) {
                        return;
                    }

                    Block block = located.getBlock();
                    Block targetBlock = (located instanceof Directional directional)
                            ? block.getRelative(directional.getDirection())
                            : block;

                    ContainerWrapper container = ContainerProvider.from(targetBlock);
                    if (container == null) return;

                    int count = container.getContent().getOrDefault(item.asOne(), 0);
                    if (count <= 0) return;

                    if (viewer.getInventory().firstEmpty() == -1) {
                        PluginMessages.get().getFullInventory().send(viewer);
                        return;
                    }

                    container.withdraw(item, withdrawCount);
                    viewer.getInventory().addItem(item.asQuantity(withdrawCount));
                    new NetworkGridGUI(plugin, grid, viewer, filter).open(viewer);
                }));
            }
        }).exceptionally(throwable -> {
            log.error("Failed to get network items for grid GUI", throwable);
            throwable.printStackTrace();
            gui.close(viewer);
            return null;
        });
    }

    /**
     * Creates the menu for the plugin
     *
     * @return the resulting menu
     */
    @Override
    public Supplier<PaginatedGui> createMenu() {
        return () -> Gui.paginated()
                .title(MessageHandler.parse(
                        NetworkGridConfig.getInstance().getTitle(),
                        Placeholders.of("search", this.grid.getSearch() != null ? this.grid.getSearch() : "Empty")
                ))
                .rows(NetworkGridConfig.getInstance().getRows())
                .disableAllInteractions()
                .create();
    }

    @ConfigSerializable
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class NetworkGridConfig extends GuiConfig {
        public NetworkGridConfig() {
            this.title = "Network Grid - <search>";
            this.rows = 6;
            this.dummyItems.add(ItemConstruct.of(Material.BLACK_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(
                            0, 1, 2, 3, 4, 5, 6, 7, 8,
                            45, 46, 47, 48, 49, 50, 51, 52, 53
                    ));

        }

        private final List<String> displayItemLore = List.of(
                "Contains <amount> in network",
                "",
                "Left click to take <stack>",
                "Right click to take <single>"
        );

        private final GuiIcon closeButton = ItemConstruct.of(Material.BARRIER)
                .setName("<red>Close")
                .asMenuItem(49);

        private GuiIcon nextPageButton = ItemConstruct.of(Material.ARROW)
                .setName("<green>Next Page")
                .asMenuItem(53);

        private GuiIcon previousPageButton = ItemConstruct.of(Material.ARROW)
                .setName("<green>Previous Page")
                .asMenuItem(45);

        private GuiIcon sortButton = ItemConstruct.of(Material.COMPARATOR)
                .setName("Sort")
                .setLore("Current: <sort>")
                .asMenuItem(8);

        private GuiIcon filterButton = ItemConstruct.of(Material.HOPPER)
                .setName("Filter")
                .setLore("Current: <filter>", "", "Click to set filter")
                .asMenuItem(7);


        public List<String> getDisplayItemLore() {
            return displayItemLore;
        }

        public GuiIcon getCloseButton() {
            return closeButton;
        }

        public GuiIcon getNextPageButton() {
            return nextPageButton;
        }

        public GuiIcon getPreviousPageButton() {
            return previousPageButton;
        }

        public GuiIcon getSortButton() {
            return sortButton;
        }

        public static NetworkGridConfig getInstance() {
            return GadgetsPlugin.get().getLoader().get(NetworkGridConfig.class);
        }
    }
}
