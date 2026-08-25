package dev.oribuin.gadgets.gui.impl.node;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.TextMessage;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.config.gui.GuiIcon;
import dev.oribuin.gadgets.config.item.ConstructType;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.config.item.component.TooltipItemType;
import dev.oribuin.gadgets.gui.api.PluginMenu;
import dev.oribuin.gadgets.node.impl.logistics.Networked;
import dev.oribuin.gadgets.node.impl.logistics.type.FilterType;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Filtered;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.oribuin.gadgets.util.Placeholders;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class NodeFilterGui extends PluginMenu<NodeFilterGui.NodeFilterGuiConfig, Gui> {

    private final Networked node;
    private final Player player;

    private boolean adding;

    // TODO: Ensure only 1 person can use the GUI at a time to prevent desync issues
    public NodeFilterGui(Player player, GadgetsPlugin plugin, Networked node) {
        super(plugin, NodeFilterGui.NodeFilterGuiConfig.class);
        this.node = node;
        this.player = player;
        this.gui = this.createMenu().get();
        if (!(node instanceof Filtered filtered)) return;

        // Set GUI Icons & Functionality
        this.setDummyIcons();

        GuiIcon close = this.getConfig().getBackButton();
        this.gui.setItem(close.getSlots(), close.withAction(
                event -> new ComplexNodeGui(this.player, this.plugin, this.node).open(this.player)
        ));

        GuiIcon infoItem = this.getConfig().getInfoItem();
        this.gui.setItem(infoItem.getSlots(), infoItem.asItem(
                "filter-type", MessageHandler.getNiceEnum(filtered.getFilterType()),
                "filtering", filtered.getFilterContent().size()
        ));

        GuiIcon toggle = filtered.getFilterType() == FilterType.WHITELIST
                ? this.getConfig().getToggleButtonWhitelist()
                : this.getConfig().getToggleButtonBlacklist();
        this.gui.setItem(toggle.getSlots(), toggle.withAction(
                Placeholders.of("filter-type", MessageHandler.getNiceEnum(filtered.getFilterType())),
                event -> {
                    filtered.setFilterType(filtered.getFilterType() == FilterType.WHITELIST
                            ? FilterType.BLACKLIST
                            : FilterType.WHITELIST);
                    this.node.serialize();

                    new NodeFilterGui(this.player, this.plugin, this.node).open(this.player);
                }
        ));

        GuiIcon addButton = this.getConfig().getAddButton();
        this.gui.setItem(addButton.getSlots(), addButton.withAction(
                event -> {
                    Set<ItemStack> currentFilter = filtered.getFilterContent();
                    int size = currentFilter.size();

                    if (size >= this.getConfig().getFilterLimit()) {
                        this.getConfig().getFilterLimitReached().send(this.player, "filter-limit", this.getConfig().getFilterLimit());
                        return;
                    }

                    this.adding = true;
                    this.getConfig().getClickInventoryItem().send(this.player);
                }
        ));

        this.gui.setPlayerInventoryAction(
                event -> {
                    if (!this.adding || event.isShiftClick()) return;

                    ItemStack clicked = event.getCurrentItem();
                    if (clicked == null || clicked.getType().isAir()) return;

                    Set<ItemStack> currentFilter = filtered.getFilterContent();
                    int size = currentFilter.size();

                    if (size >= this.getConfig().getFilterLimit()) {
                        this.getConfig().getFilterLimitReached().send(this.player, "filter-limit", this.getConfig().getFilterLimit());
                        return;
                    }

                    currentFilter.add(clicked.asOne());
                    filtered.setFilterContent(currentFilter);
                    this.node.serialize();

                    new NodeFilterGui(this.player, this.plugin, this.node).open(this.player);
                }
        );

        for (ItemStack stack : filtered.getFilterContent()) {
            this.gui.addItem(this.buildFilteredItem(stack));
        }
    }

    private @NonNull GuiItem buildFilteredItem(ItemStack stack) {

        ItemStack clone = stack.clone();
        clone.editMeta(meta -> {
            List<Component> lore = meta.lore();
            if (lore == null) lore = new ArrayList<>();
            lore.addAll(MessageHandler.parse(this.getConfig().getEntryLore()));
            meta.lore(lore);
        });

        GuiItem item = new GuiItem(clone);
        if (!(node instanceof Filtered filtered)) return item;

        item.setAction(
                event -> {


                    Set<ItemStack> currentFilter = filtered.getFilterContent();
                    currentFilter.remove(stack);

                    filtered.setFilterContent(currentFilter);
                    this.node.serialize();

                    new NodeFilterGui(this.player, this.plugin, this.node).open(this.player);
                }
        );
        return item;
    }

    @Override
    public Supplier<Gui> createMenu() {
        return () -> Gui.gui()
                .title(MessageHandler.parse(this.getConfig().getTitle()))
                .rows(this.getConfig().getRows())
                .disableAllInteractions()
                .create();
    }

    @ConfigSerializable
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class NodeFilterGuiConfig extends GuiConfig {

        public NodeFilterGuiConfig() {
            this.title = "Node Filter";
            this.rows = 4;

            this.dummyItems.add(ItemConstruct.of(Material.BLACK_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(0, 1, 2, 3, 4, 5, 6, 7, 8,
                            9, 17,
                            18, 26,
                            27, 28, 29, 30, 31, 32, 33, 34, 35
                    ));
        }

        private int filterLimit = 14;

        private TextMessage filterLimitReached = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>You have reached the filter limit of <#fbf679><filter-limit> items!");

        private TextMessage clickInventoryItem = new TextMessage("<#93bc80><b>Gadgets</b> <dark_gray>▎ <white>Click an item in your inventory to add to the filter! Don't close the GUI to do this");

        private List<String> entryLore = List.of(
                "",
                "<white>⏩ <#93bc80>Left click <white>remove from filter"
        );

        private GuiIcon backButton = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<red>Back")
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-7827"))
                .asMenuItem(27);

        private GuiIcon infoItem = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Filter Information")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Filter Type: <#fbf679><filter-type>",
                        "<#93bc80>▎ <white>Filtering: <#fbf679><filtering> items",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-10153"))
                .asMenuItem(4);

        private GuiIcon addButton = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Add Item to Filter")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>To add a new item, click this",
                        "<#93bc80>▎ <white>then click the item you wish to filter",
                        "<#93bc80>▎ <white>in your inventory",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9885"))
                .asMenuItem(31);

        private GuiIcon toggleButtonWhitelist = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Filter Type: <green>Whitelist")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Filter Type: <green><filter-type>",
                        "",
                        "<#93bc80>▎ <white>Whitelist: Only items on the filter will be accepted",
                        "<#93bc80>▎ <white>Blacklist: All items except those on the filter will be accepted",
                        "",
                        "<white>⏩ <#93bc80>Left click <white>to change type"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9885"))
                .asMenuItem(35);

        private GuiIcon toggleButtonBlacklist = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Filter Type: <red>Blacklist")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Filter Type: <red><filter-type>",
                        "",
                        "<#93bc80>▎ <white>Whitelist: Only items on the filter will be accepted",
                        "<#93bc80>▎ <white>Blacklist: All items except those on the filter will be accepted",
                        "",
                        "<white>⏩ <#93bc80>Left click <white>to change type"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9885"))
                .asMenuItem(35);

        public int getFilterLimit() {
            return filterLimit;
        }

        public TextMessage getFilterLimitReached() {
            return filterLimitReached;
        }

        public TextMessage getClickInventoryItem() {
            return clickInventoryItem;
        }

        public List<String> getEntryLore() {
            return entryLore;
        }

        public GuiIcon getBackButton() {
            return backButton;
        }

        public GuiIcon getInfoItem() {
            return infoItem;
        }

        public GuiIcon getAddButton() {
            return addButton;
        }

        public GuiIcon getToggleButtonWhitelist() {
            return toggleButtonWhitelist;
        }

        public GuiIcon getToggleButtonBlacklist() {
            return toggleButtonBlacklist;
        }
    }
}
