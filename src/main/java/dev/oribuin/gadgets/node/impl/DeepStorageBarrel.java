package dev.oribuin.gadgets.node.impl;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.gui.OpenedMenuCache;
import dev.oribuin.gadgets.gui.impl.manual.BarrelGUI;
import dev.oribuin.gadgets.node.GadgetType;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType.Serializer;
import dev.oribuin.gadgets.node.logistics.type.valued.Filtered;
import dev.oribuin.gadgets.scheduler.PluginScheduler;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.Placeholders;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Optional;
import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_AMOUNT;
import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_ITEM;
import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_MAX_AMOUNT;
import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_VOID_EXCESS;

public class DeepStorageBarrel extends Node implements Filtered.MetaImmune {

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public DeepStorageBarrel(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);
        this.item = ItemConstruct.of(Material.BROWN_TERRACOTTA)
                .setName("<white>[<#93bc80><bold>Deep Storage Barrel</bold><white>]")
                .setLore("",
                        " <white>| <gray>Can be used to store",
                        " <white>| <gray>significantly more items inside",
                        " <white>| <gray>a single block",
                        "",
                        " <white>| <gray>Item <#93bc80><item>",
                        " <white>| <gray>Amount <#93bc80><amount><white>/<#93bc80><max>",
                        " <white>| <gray>Voiding Excess <#93bc80><void_excess>"
                );

        // region Event Registration
        this.register(BlockPlaceEvent.class, this::handleBlockPlace);
        this.register(PlayerInteractEvent.class, this::handleInteract);
        // endregion

        // region Node Value Registration
        this.registerType(BARREL_AMOUNT, 0);
        this.registerType(BARREL_MAX_AMOUNT, 4096);
        this.registerType(BARREL_ITEM, null);
        this.registerType(BARREL_VOID_EXCESS, true);
        // region
    }

    /**
     * Check if an item is stored in the node
     *
     * @param stack The stack to check
     * @return Whether the item is stored in the node
     */
    private boolean isStored(ItemStack stack) {
        ItemStack stored = this.getValue(BARREL_ITEM);
        if (stored == null || stored.getType() == Material.AIR) return false;

        return stored.isSimilar(stack);
    }

    /**
     * Check if an item is blacklisted from being allowed inside a barrel
     *
     * @param stack The stack to check
     * @return Whether the itemstack is allowed
     */
    public static boolean isBlacklisted(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return false;

        // Check Shulker Boxes
        if (Tag.SHULKER_BOXES.isTagged(stack.getType())) {
            BlockStateMeta meta = (BlockStateMeta) stack.getItemMeta();
            BlockState state = meta.getBlockState();
            if (!(state instanceof ShulkerBox shulker)) return true;

            return !shulker.getInventory().isEmpty();
        }

        // Check Bundles
        if (Tag.ITEMS_BUNDLES.isTagged(stack.getType())) {
            BundleMeta meta = (BundleMeta) stack.getItemMeta();
            return meta.hasItems();
        }

        // TODO: advanced blacklist check
        return false;
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockPlace(ContextProvider<BlockPlaceEvent, Block> provider) {
        BlockPlaceEvent event = provider.event();
        ItemStack itemStack = event.getItemInHand();
        if (itemStack.getItemMeta() != null) {
            // Migrates all the existing values from the itemstack into the barrel
            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            CustomBlockData data = this.getData();
            this.migrateKey(BARREL_ITEM, container, data);
            this.migrateKey(BARREL_AMOUNT, container, data);
            this.migrateKey(BARREL_MAX_AMOUNT, container, data);
            this.migrateKey(BARREL_VOID_EXCESS, container, data);
        }
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
        PlayerInteractEvent event = provider.event();
        Block clickedBlock = provider.type();

        if (event.getPlayer().isSneaking()) return;

        // Check if the inventory is open 
        Location location = clickedBlock.getLocation();
        if (!OpenedMenuCache.isOpenedInventoryAt(location)) {
            OpenedMenuCache.addOpenedInventory(block.getLocation());
        }

        OpenedMenuCache.addViewer(location, event.getPlayer());
        PluginScheduler.get().runTaskAtEntity(event.getPlayer(), () -> {
            BarrelGUI gui = new BarrelGUI(
                    GadgetsPlugin.get(),
                    () -> deserialize(clickedBlock, PersistenceUtil.accessBlockData(clickedBlock))
            );

            gui.open(event.getPlayer());
        });
    }


    /**
     * Migrate a value from one {@link PersistentDataContainer} to another
     *
     * @param type          The {@link NamespacedKey} used to identify the value that is being migrated
     * @param type          The {@link PersistentDataType} of the value being moved over
     * @param fromContainer The {@link PersistentDataContainer} that has the original value
     * @param toContainer   The {@link PersistentDataContainer} that is receiving the value that was in the original container
     * @param <P>           Persistent data type bullshit #1
     * @param <C>           Persistent data type bullshit #2
     */
    private <P, C> void migrateKey(GadgetType<P, C> type, PersistentDataContainer fromContainer, PersistentDataContainer toContainer) {
        C result = fromContainer.get(type.key(), type);
        if (result != null) toContainer.set(type.key(), type, result);
    }

    /**
     * Get the identifier of a node type
     *
     * @return The node type identifier
     */
    @Override
    public Supplier<String> getIdentifier() {
        return NodeFactory.DEEP_BARREL::identifier;
    }

    /**
     * Load a {@link Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
     *
     * @param block The container to load from
     * @return The resulting type if available
     */
    public static DeepStorageBarrel deserialize(Block block, PersistentDataContainer container) {
        return new DeepStorageBarrel(block, PersistenceUtil.accessBlockData(container));
    }

    /**
     * Get the placeholders for the object
     *
     * @return The resulting placeholders
     */
    @Override
    public Supplier<Placeholders> getPlaceholders() {
        return () -> Placeholders.of(
                "item", Optional.ofNullable(this.getValue(BARREL_ITEM))
                        .map(ItemStack::displayName)
                        .orElse(Component.text("None")),
                
                "amount", this.getValue(BARREL_AMOUNT, 0),
                "max", this.getValue(BARREL_MAX_AMOUNT, 4096),
                "void_excess", this.getValue(BARREL_VOID_EXCESS, false) ? "Yes" : "No"
        );
    }

}
