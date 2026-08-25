package dev.oribuin.gadgets.container;

import dev.oribuin.gadgets.container.impl.DeepBarrelContainer;
import dev.oribuin.gadgets.container.impl.VanillaContainer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ContainerProvider {

    private static final List<Function<Block, ContainerWrapper>> WRAPPERS = new ArrayList<>();

    static {
        register(DeepBarrelContainer::from);
        register(VanillaContainer::from);
//        register(SmeltableContainer::from);
    }

    public static void register(Function<Block, ContainerWrapper> provider) {
        WRAPPERS.add(provider);
    }

    /**
     * Load a container wrapper from the plugin
     *
     * @param block The block to load the container wrapper from
     * @return The resulting wrapper if available
     */
    public static CompletableFuture<@Nullable ContainerWrapper> fromCustomSafe(Block block) {
        return CompletableFuture.supplyAsync(() -> fromCustom(block));
    }

    /**
     * Load a container wrapper from the plugin
     *
     * @param block The block to load the container wrapper from
     * @return The resulting wrapper if available
     */
    public static @Nullable ContainerWrapper fromCustom(Block block) {
        if (block == null || block.getType() == Material.AIR) return null;

        return WRAPPERS.stream()
                .map(x -> x.apply(block))
                .filter(wrapper -> {
                    if (wrapper == null) return false;
                    if (!wrapper.isValidContainer()) return false;
                    
                    return !(wrapper instanceof VanillaWrapper);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Load a container wrapper from the plugin
     *
     * @param block The block to load the container wrapper from
     * @return The resulting wrapper if available
     */
    public static @Nullable ContainerWrapper from(Block block) {
        if (block == null || block.getType() == Material.AIR) return null;

        // Check if the container is a furnace or not
        ContainerWrapper wrapper = fromCustom(block);
        return wrapper != null ? wrapper : VanillaContainer.from(block);
    }

}

