package dev.oribuin.gadgets.gui.impl.node;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.config.gui.GuiIcon;
import dev.oribuin.gadgets.config.item.ConstructType;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.config.item.component.TooltipItemType;
import dev.oribuin.gadgets.gui.api.PluginMenu;
import dev.oribuin.gadgets.node.logistics.Networked;
import dev.oribuin.gadgets.node.logistics.NodeDeposit;
import dev.oribuin.gadgets.node.logistics.type.valued.Channelled;
import dev.oribuin.gadgets.node.logistics.type.valued.Directional;
import dev.oribuin.gadgets.node.logistics.type.valued.Filtered;
import dev.oribuin.gadgets.node.logistics.type.valued.Priority;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.oribuin.gadgets.util.Placeholders;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ComplexNodeGui extends PluginMenu<ComplexNodeGui.ComplexNodeGuiConfig, Gui> {

    private final Networked node;
    private final Player player;

    // TODO: Ensure only 1 person can use the GUI at a time to prevent desync issues
    public ComplexNodeGui(Player player, GadgetsPlugin plugin, Networked node) {
        super(plugin, ComplexNodeGuiConfig.class);
        this.node = node;
        this.player = player;
        this.gui = this.createMenu().get();

        String type = node instanceof NodeDeposit ? "Deposit" : "Withdraw";

        this.gui.updateTitle(MessageHandler.parse(this.getConfig().getTitle(), Placeholders.of("type", type)));

        // Set GUI Icons & Functionality
        this.setDummyIcons();

        GuiIcon close = this.getConfig().getCloseButton();
        this.gui.setItem(close.getSlots(), close.withAction(
                event -> this.gui.close(this.player)
        ));

        List<String> placeholders = new ArrayList<>(List.of("type", type));
        GuiIcon directionButton = this.getConfig().getDirectionButton();
        if (node instanceof Directional directional) {
            placeholders.add("direction");
            placeholders.add(MessageHandler.getNiceEnum(directional.getDirection()));
            this.gui.setItem(directionButton.getSlots(), directionButton.withAction(
                    Placeholders.of("direction", MessageHandler.getNiceEnum(directional.getDirection())),
                    event -> {
                        switch (directional.getDirection()) {
                            case UP -> directional.setDirection(BlockFace.DOWN);
                            case DOWN -> directional.setDirection(BlockFace.NORTH);
                            case NORTH -> directional.setDirection(BlockFace.SOUTH);
                            case SOUTH -> directional.setDirection(BlockFace.EAST);
                            case EAST -> directional.setDirection(BlockFace.WEST);
                            case WEST -> directional.setDirection(BlockFace.UP);
                        }
                        this.node.serialize();

                        new ComplexNodeGui(this.player, this.plugin, this.node).open(this.player);
                    }
            ));
        }

        if (node instanceof Priority prioritied) {
            placeholders.add("priority");
            placeholders.add(String.valueOf(prioritied.getPriority()));
            GuiIcon priorityButton = this.getConfig().getPriorityButton();
            this.gui.setItem(priorityButton.getSlots(), priorityButton.withAction(
                    Placeholders.of("priority", prioritied.getPriority()),
                    event -> {
                        int next = prioritied.getPriority() + 1 > 10 ? 1 : prioritied.getPriority() + 1;
                        prioritied.setPriority(next);
                        this.node.serialize();

                        new ComplexNodeGui(this.player, this.plugin, this.node).open(this.player);
                    }
            ));
        }

        if (node instanceof Channelled channelled) {
            placeholders.add("channel");
            placeholders.add(String.valueOf(channelled.getChannel()));

            GuiIcon channelButton = this.getConfig().getChannelButton();
            this.gui.setItem(channelButton.getSlots(), channelButton.withAction(
                    Placeholders.of("channel", channelled.getChannel()),
                    event -> {
                        int next = channelled.getChannel() + 1 > 10 ? 1 : channelled.getChannel() + 1;
                        channelled.setChannel(next);
                        this.node.serialize();

                        new ComplexNodeGui(this.player, this.plugin, this.node).open(this.player);
                    }
            ));
        }

        if (node instanceof Filtered filtered) {
            placeholders.addAll(List.of("filter-type", MessageHandler.getNiceEnum(filtered.getFilterType())));
            placeholders.addAll(List.of("filtering", String.valueOf(filtered.getFilterContent().size())));

            GuiIcon filterButton = this.getConfig().getFilterButton();
            this.gui.setItem(filterButton.getSlots(), filterButton.withAction(
                    Placeholders.of(
                            "filtering", String.valueOf(filtered.getFilterContent().size()),
                            "filter-type", MessageHandler.getNiceEnum(filtered.getFilterType())
                    ),
                    event -> new NodeFilterGui(this.player, this.plugin, node).open(this.player)
            ));

            placeholders.add("meta");
            placeholders.add(filtered.isIgnoreMeta() ? "Yes" : "No");
            GuiIcon metaButton = filtered.isIgnoreMeta() ? this.getConfig().getMetaButtonEnabled() : this.getConfig().getMetaButtonDisable();
            this.gui.setItem(metaButton.getSlots(), metaButton.withAction(
                    event -> {
                        filtered.setIgnoreMeta(!filtered.isIgnoreMeta());
                        this.node.serialize();

                        new NodeFilterGui(this.player, this.plugin, this.node).open(this.player);
                    }
            ));
        }

        GuiIcon infoItem = this.getConfig().getInfoItem();
        this.gui.setItem(infoItem.getSlots(), infoItem.asItem(placeholders.toArray()));
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
    public static class ComplexNodeGuiConfig extends GuiConfig {

        public ComplexNodeGuiConfig() {
            this.title = "Node <type>";
            this.rows = 6;

            this.dummyItems.add(ItemConstruct.of(Material.BLACK_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(0, 1, 2, 3, 4, 5, 6, 7, 8,
                            45, 46, 47, 48, 49, 50, 51, 52, 53
                    ));
        }

        private GuiIcon closeButton = ItemConstruct.of(Material.BARRIER)
                .setName("<red>Close")
                .asMenuItem(49);

        private GuiIcon infoItem = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80><type> Node Information")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Direction: <#fbf679><direction>",
                        "<#93bc80>▎ <white>Priority: <#fbf679><priority>",
                        "<#93bc80>▎ <white>Channel: <#fbf679><channel>",
                        "<#93bc80>▎ <white>Filter Type: <#fbf679><filter-type>",
                        "<#93bc80>▎ <white>Filtering: <#fbf679><filtering> items",
                        "<#93bc80>▎ <white>Ignoring Item Meta: <#fbf679><meta>",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-10153"))
                .asMenuItem(22);

        private GuiIcon directionButton = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Change Direction")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Direction is the side of the node block",
                        "<#93bc80>▎ <white>the node attempts to access containers to",
                        "<#93bc80>▎ <white>take from/input into and into/from the network",
                        "",
                        "<#93bc80>▎ <white>Current Direction: <#fbf679><direction>",
                        "",
                        "<white>⏩ <#93bc80>Left click <white>to modify",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9221"))
                .asMenuItem(29);

        private GuiIcon priorityButton = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Change Priority")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>When multiple nodes are trying to use",
                        "<#93bc80>▎ <white>the same container, priority determines which",
                        "<#93bc80>▎ <white>node gets to access it first",
                        "",
                        "<#93bc80>▎ <white>Current Priority: <#fbf679><priority>",
                        "",
                        "<white>⏩ <#93bc80>Left click <white>to modify",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9868"))
                .asMenuItem(30);

        private GuiIcon channelButton = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Change Channel")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Only nodes using the same channel will",
                        "<#93bc80>▎ <white>connect to each other. This allows you to have",
                        "<#93bc80>▎ <white>several independent networks in close proximity",
                        "",
                        "<#93bc80>▎ <white>Current Channel: <#fbf679><channel>",
                        "",
                        "<white>⏩ <#93bc80>Left click <white>to modify",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-62111"))
                .asMenuItem(31);

        private GuiIcon filterButton = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Change Filter")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Filter allows you to select what items to",
                        "<#93bc80>▎ <white>allow or disallow into the network - based",
                        "<#93bc80>▎ <white>on blacklist or whitelist setting",
                        "",
                        "<#93bc80>▎ <white>Current Filter: <#fbf679><filter-type>",
                        "<#93bc80>▎ <white>Items Filtered: <#fbf679><filtering>",
                        "",
                        "<white>⏩ <#93bc80>Left click <white>to modify",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-30877"))
                .asMenuItem(32);

        private GuiIcon metaButtonEnabled = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<green>Ignore Item Meta: Enabled")
                .setLore(
                        "",
                        "<green>▎ <white>When enabled, the node will ignore",
                        "<green>▎ <white>item meta when filtering items.",
                        "",
                        "<green>▎ <white>Current Setting: <green>Enabled",
                        "",
                        "<white>⏩ <green>Left click <white>to disable",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9896"))
                .asMenuItem(33);

        private GuiIcon metaButtonDisable = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<red>Ignore Item Meta: Disabled")
                .setLore(
                        "",
                        "<red>▎ <white>When enabled, the node will ignore",
                        "<red>▎ <white>item meta when filtering items.",
                        "",
                        "<red>▎ <white>Current Setting: <red>Disabled",
                        "",
                        "<white>⏩ <red>Left click <white>to enable",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9356"))
                .asMenuItem(33);

        public GuiIcon getCloseButton() {
            return closeButton;
        }

        public GuiIcon getInfoItem() {
            return infoItem;
        }

        public GuiIcon getDirectionButton() {
            return directionButton;
        }

        public GuiIcon getPriorityButton() {
            return priorityButton;
        }

        public GuiIcon getFilterButton() {
            return filterButton;
        }

        public GuiIcon getChannelButton() {
            return channelButton;
        }

        public GuiIcon getMetaButtonEnabled() {
            return metaButtonEnabled;
        }

        public GuiIcon getMetaButtonDisable() {
            return metaButtonDisable;
        }
    }
}
