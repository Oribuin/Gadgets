package dev.oribuin.gadgets.gui.api;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.node.impl.logistics.type.Tickable;
import dev.triumphteam.gui.guis.BaseGui;

public abstract class MachinePluginMenu<T extends GuiConfig, Z extends BaseGui> extends PluginMenu<T, Z> implements Tickable {

    /**
     * Creates a new menu for the plugin to use
     *
     * @param plugin The plugin instance
     * @param config The config for the menu
     */
    protected MachinePluginMenu(GadgetsPlugin plugin, Class<T> config) {
        super(plugin, config);
    }

    @Override
    public void tick() {
        if (!this.viewed) return;
        if (this.gui.getInventory().getViewers().isEmpty()) return;

        this.setDummyIcons();
        gui.update();
    }

}
