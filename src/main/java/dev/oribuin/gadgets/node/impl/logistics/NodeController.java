package dev.oribuin.gadgets.node.impl.logistics;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.container.ContainerProvider;
import dev.oribuin.gadgets.container.ContainerWrapper;
import dev.oribuin.gadgets.container.transaction.ItemTransferRequest;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.node.impl.logistics.type.Storable;
import dev.oribuin.gadgets.node.impl.logistics.type.Tickable;
import dev.oribuin.gadgets.node.impl.logistics.type.valued.Directional;
import dev.oribuin.gadgets.node.storage.NodeProvider;
import dev.oribuin.gadgets.util.InventoryUtils;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.block.FinePosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.jeff_media.morepersistentdatatypes.DataType.STRING;
import static dev.oribuin.gadgets.util.PersistenceUtil.NETWORK_POWER;
import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_TYPE;


public class NodeController extends Node implements Tickable, Storable {

    private final Set<FinePosition> connected;
    private final ConcurrentLinkedQueue<ItemTransferRequest> pendingTransfers;
    protected int limit; // The maximum nodes connected to a network

    /**
     * Establish a new type of {@link Node} within the plugin
     *
     * @param block The block being used to define the node
     * @param data  The data of the block
     */
    public NodeController(@Nullable Block block, @Nullable CustomBlockData data) {
        super(block, data);

        this.limit = 1250; // TODO: Add to config
        this.connected = new HashSet<>(this.limit);
        this.pendingTransfers = new ConcurrentLinkedQueue<>();
        this.registerType(NETWORK_POWER, 0);

        this.register(PlayerInteractEvent.class, this::handleInteract);
        this.register(BlockBreakEvent.class, this::handleBlockBreak);
        this.register(BlockPlaceEvent.class, this::handleBlockPlace);
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
        Player player = provider.player();
        if (player == null) return;

        if (!NodeProvider.NODE_CACHE.containsKey(FinePosition.from(this.block))) {
            NodeProvider.instance().store(this);
        }

        Map<String, Integer> count = new HashMap<>();
        for (FinePosition entry : this.connected) {
            Block block = entry.toLocation().getBlock();
            Networked node = NodeFactory.from(block);

            if (node == null) {
                this.connected.remove(entry);
                continue;
            }

            int current = count.getOrDefault(node.getIdentifier().get(), 0);
            count.put(node.getIdentifier().get(), current + 1);
        }

        player.sendMessage("Total Connected: " + this.connected.size());
        for (Map.Entry<String, Integer> entry : count.entrySet()) {
            player.sendMessage(entry.getKey() + " - " + entry.getValue());
        }
    }


    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockPlace(ContextProvider<BlockPlaceEvent, Block> provider) {
        Location location = provider.type().getLocation();
        if (!this.scanPosition(location)) {
            Player player = provider.player();
            if (player != null) {
                provider.player().sendMessage("network already has a controller"); // todo: stop bypass for other nodes
                provider.event().setCancelled(true);
            }
            return;
        }

        NodeProvider.instance().store(this);
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockBreak(ContextProvider<BlockBreakEvent, Block> provider) {
        this.clearConnected();
        NodeProvider.instance().delete(this);
    }

    /**
     * Get a map of all the items that are currently stored inside the network. This does not include vanilla container content
     *
     * @return All the items stored inside the network
     */
    public CompletableFuture<@NotNull Map<ItemStack, Integer>> getNetworkItemsAsync() {
        return CompletableFuture.supplyAsync(this::getNetworkItems);
    }

    /**
     * Get a map of all the items that are currently stored inside the network
     *
     * @return All the items stored inside the network
     */
    @NotNull
    public Map<ItemStack, Integer> getNetworkItems() {
        Map<ItemStack, Integer> result = new HashMap<>();
        for (FinePosition entry : this.connected) {
            Block block = entry.toLocation().getBlock();
            Node node = NodeFactory.from(block);
            if (!(node instanceof Networked networked)) {
                this.connected.remove(entry);
                continue;
            }

            // Prioritise a directional block over the networked block
            if (networked instanceof Directional directional) {
                if (directional.getDirection() == BlockFace.SELF)
                    continue; // Networked block isnt connected to anything
                block = block.getRelative(directional.getDirection());
            }

            // might want to unregister this but ?
            if (block.getType() == Material.AIR) continue;

            // Put all inventory content inside the network
            ContainerWrapper wrapper = ContainerProvider.fromCustom(block);
            if (wrapper != null) result.putAll(wrapper.getContent());
        }

        return result;
    }

    public void clearConnected() {
        for (FinePosition entry : this.connected) {
            Block block = entry.toLocation().getBlock();
            Node node = NodeFactory.from(block);
            if (node == null) {
                this.connected.remove(entry);
                continue;
            }

            node.setValue(PersistenceUtil.NODE_CONTROLLER, null);
            node.serialize();
        }
    }

    /**
     * Scan all the blocks connected to the controller
     *
     * @param origin A block connected
     */
    public boolean scanPosition(Location origin) {
        Deque<Location> queue = new ArrayDeque<>();
        Set<FinePosition> visited = new HashSet<>();

        queue.add(origin);
        visited.add(FinePosition.from(origin));

        while (!queue.isEmpty() && this.connected.size() < this.limit) {
            Location current = queue.poll();

            for (BlockFace face : InventoryUtils.VALID_FACES) {
                if (this.connected.size() >= this.limit) break;

                Location neighbour = current.clone().add(face.getDirection());
                FinePosition fineNeighbour = FinePosition.from(neighbour);

                if (visited.contains(fineNeighbour)) continue;
                if (this.connected.contains(fineNeighbour)) continue;
                visited.add(fineNeighbour);

                Node node = NodeFactory.from(neighbour.getBlock());

                if (node instanceof NodeController other && other != this) {
                    Location otherControllerPos = other.getValue(PersistenceUtil.NODE_CONTROLLER);
                    if (otherControllerPos == null) return false;
                    continue;
                }

                if (!(node instanceof Networked networked)) continue;

                this.connected.add(fineNeighbour);
                networked.setValue(PersistenceUtil.NODE_CONTROLLER, this.block.getLocation());
                networked.serialize();

                queue.add(neighbour);
            }
        }

        return true;
    }

    public Node locate(ItemStack stack) {
        Node result = null;

        for (FinePosition entry : this.connected) {
            Block block = entry.toLocation().getBlock();
            Node node = NodeFactory.from(block);
            if (!(node instanceof Networked networked)) {
                this.connected.remove(entry);
                continue;
            }

            // Prioritise a directional block over the networked block
            if (networked instanceof Directional directional) {
                if (directional.getDirection() == BlockFace.SELF) continue; // Networked block isnt connected to anything
                block = block.getRelative(directional.getDirection());
            }

            // TODO: might want to unregister this but ?
            if (block.getType() == Material.AIR) continue;

            // Put all inventory content inside the network
            ContainerWrapper wrapper = ContainerProvider.fromCustom(block);
            if (wrapper == null) continue;

            if (wrapper.contains(stack)) {
                result = node;
                break;
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Networked> List<T> from(Class<T> type) {
        return this.connected.stream().map(position -> (T) NodeFactory.from(block))
                .filter(networked -> networked != null && networked.getClass().equals(type))
                .toList();
    }

    public List<Networked> getFiltered(Predicate<Networked> predicate) {
        return this.connected.stream()
                .map(finePosition -> {
                    Node node = NodeFactory.from(finePosition.toLocation().getBlock());
                    return node instanceof Networked networked ? networked : null;
                })
                .filter(networked -> {
                    if (networked == null) return false;
                    return predicate.test(networked);
                })
                .toList();
    }

    /**
     * Get the identifier of a node type
     *
     * @return The node type identifier
     */
    @Override
    public Supplier<String> getIdentifier() {
        return NodeFactory.NODE_CONTROLLER::identifier;
    }

    @Override
    public void tick() {
        for (FinePosition entry : this.connected) {
            Block block = entry.toLocation().getBlock();
            Node node = NodeFactory.from(block);
            if (!(node instanceof Tickable tickable)) continue; // ideally remove it icl!

            Block nodeBlock = node.getBlock();
            if (nodeBlock == null) continue;
            if (!block.getLocation().isChunkLoaded()) continue;

            tickable.tick();
        }
    }

    public static NodeController from(FinePosition position) {
        if (position == null) return null;

        Node node = NodeProvider.NODE_CACHE.get(position);
        return node instanceof NodeController controller ? controller : null;
    }

    /**
     * Store a {@link NodeType.Serializer} into a {@link PersistentDataContainer}
     *
     * @param container The container to store the serializer in
     */
    @Override
    public <T extends PersistentDataContainer> void serialize(T container) {
        // region Serialize the data into the PDC Container
        container.set(NODE_TYPE.key(), STRING, this.getIdentifier().get());

        nodeValues.values().forEach(nodeValue -> {
            if (nodeValue.isDirty()) nodeValue.serialize(container);
        });
        // endregion

        // region Store the node in the nodeprovider
        NodeProvider.instance().store(this);
    }

    /**
     * Load a {@link NodeType.Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
     *
     * @param container The container to load from
     * @return The resulting type if available
     */
    public static NodeController deserialize(Block block, PersistentDataContainer container) {
        if (block == null) return new NodeController(null, null);

        FinePosition position = FinePosition.from(block);
        Node node = NodeProvider.NODE_CACHE.get(position);

        if (node instanceof NodeController controller) {
            return controller;
        }

        return (NodeController) NodeProvider.NODE_CACHE.putIfAbsent(position,
                new NodeController(block, PersistenceUtil.accessBlockData(container))
        );

    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Set<FinePosition> getConnected() {
        return connected;
    }

    public ConcurrentLinkedQueue<ItemTransferRequest> getPendingTransfers() {
        return pendingTransfers;
    }
}
