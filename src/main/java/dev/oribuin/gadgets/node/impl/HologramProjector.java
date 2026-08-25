package dev.oribuin.gadgets.node.impl;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.node.storage.Hologram;
import dev.oribuin.gadgets.util.PersistenceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_ALIGNMENT;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_BACKGROUND;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_BILLBOARD;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_ENTITY;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_POSITION;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_SCALE;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_SHADOW;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_TEXT;

public class HologramProjector extends Node {

    private Hologram hologram;

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public HologramProjector(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.item = ItemConstruct.of(Material.VERDANT_FROGLIGHT)
                .setName("<white>[<#93bc80><bold>Hologram Projector</bold><white>]")
                .setLore("", " <white>| <gray>Displays a hologram");

        // region Event Registration
        this.register(BlockPlaceEvent.class, this::handleBlockPlace);
        this.register(BlockBreakEvent.class, this::handleBlockBreak);
        // endregion

        // region Node Value Registration
        this.registerType(HOLOGRAM_ENTITY, null);
        this.registerType(HOLOGRAM_POSITION, null);
        this.registerType(HOLOGRAM_TEXT, "Change Me");
        this.registerType(HOLOGRAM_BACKGROUND, null);
        this.registerType(HOLOGRAM_BILLBOARD, Display.Billboard.VERTICAL);
        this.registerType(HOLOGRAM_ALIGNMENT, TextDisplay.TextAlignment.CENTER);
        this.registerType(HOLOGRAM_SCALE, 1.0);
        this.registerType(HOLOGRAM_SHADOW, true);
        // endregion
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockPlace(ContextProvider<BlockPlaceEvent, Block> provider) {

        BlockPlaceEvent event = provider.event();
        Location location = event.getBlock().getLocation().toCenterLocation().add(0, 1.0, 0);

        this.setValue(HOLOGRAM_POSITION, location);

        // Kill any existing holograms
        UUID hologramUUID = this.getValue(HOLOGRAM_ENTITY);
        if (hologramUUID != null) {
            Entity entity = location.getWorld().getEntity(hologramUUID);
            if (entity != null && !entity.isDead()) entity.remove();
        }

        // Create a new hologram
        if (this.hologram == null) {
            this.hologram = Hologram.from(this);
        }

        this.hologram.update();
        this.setValue(HOLOGRAM_ENTITY, this.hologram.getDisplay().getUniqueId());
        this.serialize();
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockBreak(ContextProvider<BlockBreakEvent, Block> provider) {
        BlockBreakEvent event = provider.event();

        // Kill any existing holograms
        UUID hologramUUID = this.getValue(HOLOGRAM_ENTITY);
        if (hologramUUID != null) {
            Entity entity = event.getBlock().getWorld().getEntity(hologramUUID);
            if (entity != null && !entity.isDead()) entity.remove();
        }

        this.resetValues(
                HOLOGRAM_ALIGNMENT,
                HOLOGRAM_BACKGROUND,
                HOLOGRAM_BILLBOARD,
                HOLOGRAM_ENTITY,
                HOLOGRAM_POSITION,
                HOLOGRAM_SCALE,
                HOLOGRAM_SHADOW,
                HOLOGRAM_TEXT
        );
    }

    /**
     * Get the identifier of a node type
     *
     * @return The node type identifier
     */
    @Override
    public Supplier<String> getIdentifier() {
        return NodeFactory.HOLOGRAM_PROJECTOR::identifier;
    }

    /**
     * Load a {@link NodeType.Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
     *
     * @param block The container to load from
     * @return The resulting type if available
     */
    public static HologramProjector deserialize(Block block, PersistentDataContainer container) {
        return new HologramProjector(block, PersistenceUtil.accessBlockData(container));
    }

}
