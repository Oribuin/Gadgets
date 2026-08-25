package dev.oribuin.gadgets.gui.impl.node;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.config.item.ConstructType;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.config.item.component.TooltipItemType;
import dev.oribuin.gadgets.gui.api.PluginMenu;
import dev.oribuin.gadgets.node.impl.logistics.NodeController;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.function.Supplier;

public class ControllerGui extends PluginMenu<ControllerGui.ControllerGuiConfig, Gui> {

    private final NodeController node;
    private final Player player;

    // TODO: Ensure only 1 person can use the GUI at a time to prevent desync issues
    public ControllerGui(Player player, GadgetsPlugin plugin, NodeController node) {
        super(plugin, ControllerGui.ControllerGuiConfig.class);
        this.node = node;
        this.player = player;
        this.gui = this.createMenu().get();

        // Set GUI Icons & Functionality
        this.setDummyIcons();

        // TODO: List the current node power
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
    public static class ControllerGuiConfig extends GuiConfig {

        public ControllerGuiConfig() {
            this.title = "Node Controller";
            this.rows = 3;

            this.dummyItems.add(ItemConstruct.of(Material.BLACK_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(
                            0, 1, 2, 3, 4, 5, 6, 7, 8,
                            18, 19, 20, 21, 22, 23, 24, 25, 26
                    ));
        }
    }

}
