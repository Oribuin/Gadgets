package dev.oribuin.gadgets.manager;

import dev.oribuin.gadgets.GadgetsPlugin;

public interface Manager {

    /**
     * The task that runs when the plugin is loaded/reloaded
     *
     * @param plugin The plugin reloading
     */
    void reload(GadgetsPlugin plugin);

    /**
     * The task that runs when the plugin is disabled, usually takes priority over {@link Manager#reload(GadgetsPlugin)}
     *
     * @param plugin The plugin being disabled
     */
    void disable(GadgetsPlugin plugin);

}
