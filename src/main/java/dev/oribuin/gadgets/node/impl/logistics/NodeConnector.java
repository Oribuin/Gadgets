package dev.oribuin.gadgets.node.impl.logistics;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.util.PersistenceUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;
import dev.oribuin.gadgets.config.TextMessage;

import java.util.List;
import java.util.function.Supplier;


public class NodeConnector extends Networked {

    private final TextMessage nodeConnected = new TextMessage()
            .actionbar("<white>[ <#8bf25e>Connected <white>]"); // TODO: Add to config
    private final TextMessage nodeDisconnected = new TextMessage()
            .actionbar("<white>[ <#f25e5e>Disconnected <white>]"); // TODO: Add to config
    
    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public NodeConnector(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.item = ItemConstruct.of(Material.WHITE_STAINED_GLASS)
                .setName("<white>[<#93bc80><bold>" + this.getIdentifier().get() + "</bold><white>]")
                .setLore(List.of("", " <white>| <gray>Does Something!"));

        this.register(BlockPlaceEvent.class, this::handleBlockPlace);
        this.register(BlockBreakEvent.class, this::handleBlockBreak);
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
        super.handleInteract(provider);
        Player player = provider.event().getPlayer();
        provider.event().setCancelled(false);

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (player.isSneaking() && mainHand.getType().isAir()) {
            (this.getControllerPosition() != null ? this.nodeConnected : this.nodeDisconnected).send(player);
        }
    }

    /**
     * Get the identifier of a node type
     *
     * @return The node type identifier
     */
    @Override
    public Supplier<String> getIdentifier() {
        return NodeFactory.NODE_CONNECTOR::identifier;
    }

    /**
     * Load a {@link NodeType.Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
     *
     * @param container The container to load from
     * @return The resulting type if available
     */
    public static NodeConnector deserialize(Block block, PersistentDataContainer container) {
        return new NodeConnector(block, PersistenceUtil.accessBlockData(container));
    }

}
