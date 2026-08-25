package dev.oribuin.gadgets.gui.api;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.scheduler.PluginScheduler;
import dev.oribuin.gadgets.util.Placeholders;
import dev.triumphteam.gui.guis.BaseGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class PluginMenu<T extends GuiConfig, Z extends BaseGui> {

    private static final List<Class<? extends GuiConfig>> registeredConfigs = new ArrayList<>();

    protected final GadgetsPlugin plugin;
    protected final Class<T> config;
    protected Z gui;
    protected boolean viewed;

    /**
     * Creates a new menu for the plugin to use
     *
     * @param plugin The plugin instance
     * @param config The config for the menu
     */
    protected PluginMenu(GadgetsPlugin plugin, Class<T> config) {
        this.plugin = plugin;
        this.config = config;
        this.viewed = false;

        if (!registeredConfigs.contains(config)) {
            registeredConfigs.add(config);
            plugin.getLoader().loadConfig(config, Path.of("guis"));
        }
    }

    /**
     * Open the menu for the player synchronously and mark the menu as being viewed
     *
     * @param player The player opening the menu
     */
    public void open(Player player) {
        if (this.gui == null) return;

        this.viewed = true;
        PluginScheduler.get().runTaskAtEntity(player, () -> this.gui.open(player));

        // If the gui is tickable, tick the gui
        if (this instanceof GuiTickable tickable) {
            long delay = tickable.getTickDelay().toSeconds() * 20L;

            Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(GadgetsPlugin.get(), task -> {
                // Gui doesn't exist, don't tick & cancel
                if (this.gui == null) {
                    task.cancel();
                    return;
                }

                // GUI is viewed but no longer has viewers, cancel
                if (this.viewed && this.gui.getInventory().getViewers().isEmpty()) {
                    task.cancel();
                    return;
                }

                tickable.tick();
            }, delay, delay);
        }
    }

    /**
     * Creates the menu for the plugin
     *
     * @return the resulting menu
     */
    public abstract Supplier<Z> createMenu();

    /**
     * Set the dummy items as defined in the config
     */
    public void setDummyIcons() {
        this.setDummyIcons(Placeholders.empty());
    }
    
    /**
     * Set the dummy items as defined in the config
     *
     * @param placeholders The placeholders
     */
    public void setDummyIcons(Placeholders placeholders) {
        this.getConfig().getDummyItems().forEach(guiIcon -> this.gui.setItem(
                guiIcon.getSlots(), guiIcon.asItem(placeholders)
        ));
    }

    /**
     * Get the config for the plugin menu
     *
     * @return The resulting config loader
     */
    public final T getConfig() {
        return this.plugin.getLoader().get(config);
    }

    public Z getGui() {
        return gui;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

}
