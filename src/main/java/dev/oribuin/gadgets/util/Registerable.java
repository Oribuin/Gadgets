package dev.oribuin.gadgets.util;

import dev.oribuin.gadgets.GadgetsPlugin;

public interface Registerable {

    /**
     * Called when the plugin enables
     *
     * @param plugin The owning plugin
     */
    default void enable(final GadgetsPlugin plugin) {
    }

    /**
     * Called when the plugin disables
     *
     * @param plugin The owning plugin
     */
    default void disable(final GadgetsPlugin plugin) {
    }

}
