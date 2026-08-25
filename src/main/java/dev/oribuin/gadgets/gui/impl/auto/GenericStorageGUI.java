package dev.oribuin.gadgets.gui.impl.auto;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.gui.api.GuiTickable;
import dev.oribuin.gadgets.gui.api.PluginMenu;
import dev.oribuin.gadgets.node.impl.logistics.StorageNode;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.Placeholders;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.StorageGui;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_INVENTORY;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_INVENTORY_ROWS;

public class GenericStorageGUI extends PluginMenu<GenericStorageGUI.StorageNodeConfig, StorageGui> implements GuiTickable {

    private final Supplier<? extends StorageNode> supplier;

    /**
     * Creates a new menu for the plugin to use
     *
     * @param plugin   The plugin instance
     * @param supplier The supplier for the storage node
     */
    public GenericStorageGUI(GadgetsPlugin plugin, Supplier<? extends StorageNode> supplier) {
        super(plugin, StorageNodeConfig.class);
        this.supplier = supplier;
        this.gui = this.createMenu().get();
    }

    /**
     * Creates a tickable task for a {@link PluginMenu}
     */
    @Override
    public void tick() {
        this.placeContent(this.gui);
    }

    private void placeContent(StorageGui gui) {
        StorageNode node = this.supplier.get();
        int maxSlots = node.getValue(PersistenceUtil.NODE_INVENTORY_ROWS, 0) * 9;
        if (maxSlots <= 0) return;

        Map<Integer, ItemStack> content = node.getValue(NODE_INVENTORY, new HashMap<>());
        if (content.isEmpty()) return;

        for (int i = 0; i < maxSlots; i++) {
            ItemStack stack = content.get(i);
            if (stack == null) continue;
            gui.getInventory().setItem(i, stack);
        }
    }

    /**
     * Creates the menu for the plugin
     *
     * @return the resulting menu
     */
    @Override
    public Supplier<StorageGui> createMenu() {
        StorageNode node = this.supplier.get();
        String nodeName = MessageHandler.capitalize(node.getIdentifier().get().replace("_", " "));

        return () -> Gui.storage()
                .title(MessageHandler.parse(StorageNodeConfig.getInstance().getTitle(), Placeholders.of("node", nodeName)))
                .rows(node.getValue(NODE_INVENTORY_ROWS, 1))
                .apply(storageGui -> {
                    storageGui.setDragAction(event -> this.saveGui(event.getInventory()));
                    storageGui.setOutsideClickAction(event -> this.saveGui(event.getInventory()));
                    storageGui.setPlayerInventoryAction(event -> this.saveGui(event.getInventory()));
                    storageGui.setCloseGuiAction(event -> this.saveGui(event.getInventory()));
                    storageGui.setDefaultTopClickAction(event -> this.saveGui(event.getInventory()));

                    this.placeContent(storageGui);
                })
                .create();
    }

    /**
     * Save the contents of a storage node into the block
     *
     * @param inventory The inventory to save
     */
    private void saveGui(Inventory inventory) {
        StorageNode node = this.supplier.get();
        Map<Integer, ItemStack> contents = new ConcurrentHashMap<>();
        int size = node.getValue(NODE_INVENTORY_ROWS, 0) * 9;
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType().isAir()) continue;
            contents.put(i, stack);
        }

        System.out.println("Saving items: " + contents.size());
        node.setValue(NODE_INVENTORY, contents);
        node.serialize(node.getData());
    }


@ConfigSerializable
@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal" })
public static class StorageNodeConfig extends GuiConfig {

        public StorageNodeConfig() {
            this.title = "<node>";
        }

        public static StorageNodeConfig getInstance() {
            return GadgetsPlugin.get().getLoader().get(StorageNodeConfig.class);
        }
    }

}

