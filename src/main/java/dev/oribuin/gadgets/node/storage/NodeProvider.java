package dev.oribuin.gadgets.node.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.node.logistics.NodeController;
import dev.oribuin.gadgets.node.logistics.type.Tickable;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.block.FinePosition;
import dev.oribuin.gadgets.util.block.NodePipePath;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class NodeProvider {

    public static final Map<FinePosition, Node> NODE_CACHE = new ConcurrentHashMap<>();
    private final Cache<FinePosition, Node> activeNodes;

    private static NodeProvider provider;
    private final GadgetsPlugin plugin;
    private BukkitTask task;

    public NodeProvider(GadgetsPlugin plugin) {
        this.plugin = plugin;
        this.activeNodes = CacheBuilder.newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build();

        provider = this;

        NODE_CACHE.clear();

        // Load positions
        this.plugin.getDataManager().loadPositions().thenAccept(map -> {
            for (Map.Entry<FinePosition, NodeType<?>> type : map.entrySet()) {
                Block block = type.getKey().toLocation().getBlock();
                CustomBlockData data = PersistenceUtil.accessBlockData(block);

                Node node = type.getValue().deserializer().apply(block, data);
                if (node instanceof NodeController controller) {
                    controller.scanPosition(block.getLocation());
                }

                NODE_CACHE.put(type.getKey(), node);
            }
        });

        // run ticker task :)
        if (this.task != null) this.task.cancel();
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (NODE_CACHE.isEmpty()) return;
            
            NODE_CACHE.values().forEach(node -> {
                if (!(node instanceof Tickable tickable)) return;
                
                Block block = node.getBlock();
                if (block == null || !NodePipePath.isLoaded(block.getLocation())) return;
                
                tickable.tick();
            });
        }, 20, 20);
    }

    public void tick(NodeController controller) {
        Block block = controller.getBlock();
        if (block == null || !block.getLocation().isChunkLoaded()) return;
        if (block.getType().isAir()) {
            this.delete(controller);
            return;
        }

        controller.tick();
    }

    /**
     * Get a cached node from a plugin
     *
     * @param tClass   The node class type
     * @param position The position to get
     * @param <T>      The node type
     * @return The resulting node if available
     */
    @SuppressWarnings("unchecked")
    public <T extends Node> T from(Class<T> tClass, FinePosition position, Supplier<T> loader) {
        Node node = this.activeNodes.getIfPresent(position);
        if (node == null || !node.getClass().isAssignableFrom(tClass)) {
            T supplied = loader.get();
            if (supplied != null) {
                this.activeNodes.put(position, supplied);
                return supplied;
            }

            return null;
        }

        return (T) node;
    }

    public <T extends Node> void cache(Block position, T node) {
        this.activeNodes.put(FinePosition.from(position), node);
    }

    public void store(Node node) {
        Block block = node.getBlock();
        if (block == null) return;

        FinePosition position = FinePosition.from(block);
        if (node instanceof NodeController controller) {
            controller.scanPosition(block.getLocation());
        }

        if (!NODE_CACHE.containsKey(position)) {
            this.plugin.getDataManager().storePosition(node);
        }
        NODE_CACHE.put(position, node);
    }

    public void delete(Node node) {
        Block block = node.getBlock();
        if (block == null) return;

        FinePosition position = FinePosition.from(block);
        NODE_CACHE.remove(position, node);
        this.plugin.getDataManager().deletePosition(node);
    }

    public static NodeProvider instance() {
        return provider;
    }
    
    public Cache<FinePosition, Node> getActiveNodes() {
        return activeNodes;
    }
}
