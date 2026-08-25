package dev.oribuin.gadgets.gui.api;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;

import java.util.function.Supplier;

public class ExampleMenu extends PluginMenu<ExampleMenu.ExampleMenuConfig, PaginatedGui> {

    /**
     * Creates a new menu for the plugin to use
     *
     * @param plugin The plugin instance
     */
    protected ExampleMenu(GadgetsPlugin plugin) {
        super(plugin, ExampleMenuConfig.class);
    }

    /**
     * Creates the menu for the plugin
     *
     * @return the resulting menu
     */
    @Override
    public Supplier<PaginatedGui> createMenu() {
        return () -> Gui.paginated()
                .title(MessageHandler.parse(this.getConfig().getTitle()))
                .rows(this.getConfig().getRows())
                .disableAllInteractions()
                .apply(paginatedGui -> this.setDummyIcons())
                .create();
    }

    public static class ExampleMenuConfig extends GuiConfig {

    }
}
