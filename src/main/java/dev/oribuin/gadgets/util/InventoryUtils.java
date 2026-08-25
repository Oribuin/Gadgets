package dev.oribuin.gadgets.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.bukkit.block.BlockFace.DOWN;
import static org.bukkit.block.BlockFace.EAST;
import static org.bukkit.block.BlockFace.NORTH;
import static org.bukkit.block.BlockFace.SOUTH;
import static org.bukkit.block.BlockFace.UP;
import static org.bukkit.block.BlockFace.WEST;

public final class InventoryUtils {

    public static final Set<BlockFace> VALID_FACES = EnumSet.of(UP, DOWN, NORTH, EAST, SOUTH, WEST);

    /**
     * Get the current supply of items inside a {@link Inventory}
     *
     * @param container The {@link Inventory} the item is being stored in
     * @param stack     The {@link ItemStack} that is being checked
     * @return The returning stock of the container
     */
    public static int getStock(Inventory container, ItemStack stack) {
        int total = 0;

        for (ItemStack itemStack : container.getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (itemStack.isSimilar(stack)) total += itemStack.getAmount();
        }

        return total;
    }

    /**
     * Get the remaining space left in the {@link Inventory}
     *
     * @param container The {@link Inventory} to check
     * @param stack     The {@link ItemStack} that is being checked
     * @return The returning remaining space inside the container
     */
    public static int getRemainingSpace(Inventory container, ItemStack stack) {
        int total = 0;

        for (ItemStack itemStack : container.getContents()) {
            if (itemStack == null || itemStack.getType().isAir()) {
                total += stack.getMaxStackSize();
                continue;
            }

            if (itemStack.isSimilar(stack)) {
                total += itemStack.getMaxStackSize() - itemStack.getAmount();
            }
        }

        return total;
    }

    /**
     * Get the empty face of a block (the face that is air)
     *
     * @param block     The block
     * @param preferred The preferred face
     * @return The empty face
     */
    public static BlockFace getEmptyFace(Block block, BlockFace preferred) {
        if (preferred != null && block.getRelative(preferred).getType() == Material.AIR)
            return preferred;

        List<BlockFace> allowed = List.of(
                BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST
        );

        for (BlockFace face : allowed) {
            Block relative = block.getRelative(face);
            if (relative.getType() == Material.AIR)
                return face;
        }

        return BlockFace.SELF;
    }

    /**
     * Check if a provided block is a double chest
     *
     * @param block The block to check
     * @return true if the chest is a double
     */
    public static boolean isDoubleChest(Block block) {
        if (!(block.getBlockData() instanceof Chest chest)) return false;
        return chest.getType() != Chest.Type.SINGLE;
    }


    /**
     * Get the other half of the chest if available, null otherwise
     *
     * @param block The block to check
     * @return The returning block or null
     */
    public static Block getOtherHalf(Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Chest chest)) return null;
        if (chest.getType() == Chest.Type.SINGLE) return null;

        BlockFace towardsLeft = getRightSide(chest.getFacing());
        BlockFace actuallyBlockFace = chest.getType() == Chest.Type.LEFT ? towardsLeft : towardsLeft.getOppositeFace();
        return block.getRelative(actuallyBlockFace);
    }

    /**
     * Get the right side of a block
     *
     * @param blockFace The original face to check
     * @return The resulting blockface
     */
    public static BlockFace getRightSide(@NotNull BlockFace blockFace) {
        return switch (blockFace) {
            case EAST -> BlockFace.SOUTH;
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> blockFace;
        };
    }

    public static BlockFace getDirection(Block original, Block target) {
        if (original == null || target == null) return null;

        for (BlockFace face : VALID_FACES) {
            Block relative = original.getRelative(face);
            if (relative.getLocation().equals(target.getLocation())) return face;
        }

        return null;
    }
}
