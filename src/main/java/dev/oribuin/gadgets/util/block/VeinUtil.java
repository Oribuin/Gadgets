package dev.oribuin.gadgets.util.block;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public final class VeinUtil {

    /**
     * This method gives you a List of all Blocks
     * that are directly or indirectly connected to the given Block
     * and share the same Material as the given Block.
     *
     * @param block The Block to start with
     * @param limit The max amount of Blocks to expand into
     * @return A List of all Blocks
     */
    public static List<Block> find(final Block block, final int limit) {
        return find(block, limit, found -> found.getType() == block.getType());
    }

    /**
     * This method gives you a List of all Blocks
     * that are directly or indirectly connected to the given Block
     * and pass the given Predicate.
     *
     * @param block     The Block to start with
     * @param limit     The max amount of Blocks to expand into
     * @param predicate A Predicate describing what Blocks to count
     * @return A List of all Blocks
     */
    public static List<Block> find(final Block block, final int limit, final Predicate<Block> predicate) {
        List<Block> list = new LinkedList<>();
        expand(block, list, limit, predicate);
        return list;
    }

    private static void expand(final Block anchor, final List<Block> list, final int limit, final Predicate<Block> predicate) {
        if (list.size() >= limit) {
            return;
        }

        list.add(anchor);

        for (final SimpleBlockFace face : SimpleBlockFace.VALUES) {
            Block next = anchor.getRelative(face.face);

            if (!list.contains(next) && predicate.test(next)) {
                expand(next, list, limit, predicate);
            }
        }
    }

    /**
     * Provides a simplified Direction interface only
     * using the important faces
     */
    public enum SimpleBlockFace {
        NORTH(BlockFace.NORTH),
        EAST(BlockFace.EAST),
        SOUTH(BlockFace.SOUTH),
        WEST(BlockFace.WEST),
        UP(BlockFace.UP),
        DOWN(BlockFace.DOWN);

        private static final SimpleBlockFace[] VALUES = values();
        private final BlockFace face;

        SimpleBlockFace(final BlockFace face) {
            this.face = face;
        }

        public static SimpleBlockFace getFacing(final BlockFace facing) {
            SimpleBlockFace result = SimpleBlockFace.NORTH;

            for (final SimpleBlockFace face : VALUES) {
                if (face.face != facing) continue;

                result = face;
                break;
            }

            return result;
        }

    }

    private VeinUtil() {
        throw new UnsupportedOperationException();
    }

}
