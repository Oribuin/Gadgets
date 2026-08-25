package dev.oribuin.gadgets.node.impl;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.Placeholders;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.ENHANCED_FURNACE_MULTIPLIER;

public final class EnhancedFurnace extends Node {

    private transient double cookMultiplier = 2.0;

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public EnhancedFurnace(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.item = ItemConstruct.of(Material.FURNACE)
                .setName("<white>[<#93bc80><bold>Enhanced Furnace</bold><white>]")
                .setLore("",
                        " <white>| <gray>Smelt items within a furnace",
                        " <white>| <gray>at <#93bc80>x<multiplier><gray> speed"
                );

        this.register(BlockBreakEvent.class, this::handleBlockBreak);
        this.register(FurnaceStartSmeltEvent.class, this::handleStartSmelt);
        this.register(PlayerInteractEvent.class, this::handleInteract);
        
        this.registerType(ENHANCED_FURNACE_MULTIPLIER, this.cookMultiplier);
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
        provider.event().setCancelled(false);
    }

    /**
     * Handles any functionality behind a furnace starting to smelt an item
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleStartSmelt(ContextProvider<FurnaceStartSmeltEvent, Block> provider) {
        FurnaceStartSmeltEvent event = provider.event();
        event.setTotalCookTime((int) (event.getTotalCookTime() / this.cookMultiplier));
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockBreak(ContextProvider<BlockBreakEvent, Block> provider) {
        Furnace furnace = (Furnace) provider.type().getState();

        Arrays.stream(furnace.getInventory().getContents()).forEach(it -> {
            if (it == null || it.getType() == Material.AIR) return;
            furnace.getLocation().getWorld().dropItem(furnace.getLocation(), it);
        });
    }

    /**
     * Get the placeholders for the object
     *
     * @return The resulting placeholders
     */
    @Override
    public Supplier<Placeholders> getPlaceholders() {
        return () -> Placeholders.of("multiplier", this.getValue(ENHANCED_FURNACE_MULTIPLIER, 2.0));
    }

    /**
     * Get the identifier of a node type
     *
     * @return The node type identifier
     */
    @Override
    public Supplier<String> getIdentifier() {
        return NodeFactory.ENHANCED_FURNACE::identifier;
    }

    /**
     * Load a {@link NodeType.Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
     *
     * @param container The container to load from
     * @return The resulting type if available
     */
    public static EnhancedFurnace deserialize(Block block, PersistentDataContainer container) {
        EnhancedFurnace furnace = new EnhancedFurnace(block, PersistenceUtil.accessBlockData(container));
        if (container != null) furnace.loadFrom(container);
        return furnace;
    }

}
