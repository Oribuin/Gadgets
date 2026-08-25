package dev.oribuin.gadgets.node;

import com.jeff_media.morepersistentdatatypes.DataType;
import dev.oribuin.gadgets.util.PersistenceUtil;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Create a new {@link NodeType} for the plugin to load in the {@link NodeFactory}
 *
 * @param identifier   The identifier for the node
 * @param serializer   The serializer method for the node
 * @param deserializer The deserializer method for the node
 * @param <T>          The node that is being serialized
 */
public record NodeType<T extends Node>(
        Class<T> nodeClass,
        String identifier,
        BiConsumer<? extends PersistentDataContainer, T> serializer,
        BiFunction<Block, PersistentDataContainer, T> deserializer
) {

    public interface Serializer {

        /**
         * Get the identifier of a node type
         *
         * @return The node type identifier
         */
        Supplier<String> getIdentifier();

        /**
         * Store a {@link Serializer} into a {@link PersistentDataContainer}
         *
         * @param container The container to store the serializer in
         */
        default <T extends PersistentDataContainer> void serialize(T container) {
            container.set(PersistenceUtil.NODE_TYPE.key(), DataType.STRING, this.getIdentifier().get());
        }
    }

}
