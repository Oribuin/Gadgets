package dev.oribuin.gadgets.gadgets.impl.explosive;


import com.jeff_media.morepersistentdatatypes.DataType;
import dev.oribuin.gadgets.api.MultiBlockBreakEvent;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.PlayerUtil;
import dev.oribuin.gadgets.hook.PayoutUtil;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.Damageable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public abstract class ExplosiveItem extends Gadget {

    protected int radius = 3;
    protected final Set<Material> blacklistTypes = EnumSet.of(
            Material.BEDROCK,
            Material.END_PORTAL,
            Material.END_PORTAL_FRAME,
            Material.BEACON,
            Material.ANCIENT_DEBRIS,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST,
            Material.SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.DISPENSER,
            Material.DROPPER,
            Material.HOPPER,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.BREWING_STAND,
            Material.JUKEBOX,
            Material.BARREL,
            Material.NOTE_BLOCK
    );


    public ExplosiveItem(String identifier) {
        super(identifier, ItemConstruct.of(Material.PAPER));
    }

    public ExplosiveItem(String identifier, ItemConstruct stack) {
        super(identifier, stack);
    }


    /**
     * Explodes blocks in a given radius
     *
     * @param player    The owning player
     * @param event     The related event
     * @param radius    The explosion radius
     * @param blacklist The material blacklist
     */
    public final void explodeBlock(Player player, BlockBreakEvent event, int radius, Set<Material> blacklist) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        AtomicBoolean exploded = new AtomicBoolean(false);

        if (!PlayerUtil.townCheck(player, block)) {
            return;
        }

        double X = location.getBlockX();
        double Y = location.getBlockY();
        double Z = location.getBlockZ();
        double minX = X - (double) radius;
        double maxX = X + (double) radius + 1.0D;
        double minY = Y - (double) radius;
        double maxY = Y + (double) radius + 1.0D;
        double minZ = Z - (double) radius;
        double maxZ = Z + (double) radius + 1.0D;

        MultiBlockBreakEvent multiBlockBreakEvent = new MultiBlockBreakEvent(player);

        this.triConsumer((x, y, z) -> this.explodeSingleBlock(
                        new Location(block.getWorld(), x, y, z),
                        player,
                        multiBlockBreakEvent,
                        exploded,
                        blacklist,
                        true
                ), new double[]{minX, maxX, minY, maxY, minZ, maxZ}
        );

        if (multiBlockBreakEvent.getBlocks().isEmpty()) return;

        Map<Material, Integer> mapped = new HashMap<>();
        multiBlockBreakEvent.getBlocks().forEach((it, drop) -> {
            // Check if the block is in the blacklist
            if (blacklist.contains(it.getType())) return;

            int current = mapped.getOrDefault(it.getType(), 0);
            mapped.put(it.getType(), current + 1);
        });

        multiBlockBreakEvent.callEvent();
        Map<Material, Integer> adjusted = PayoutUtil.getAdjusted(mapped);
        PayoutUtil.addMcMMOXp(player, adjusted);
    }

    /**
     * Explodes farm blocks in a given radius
     *
     * @param player  The owning player
     * @param event   The related event
     * @param radius  The explosion radius
     * @param allowed The allowed farm materials
     */
    public final void explodeFarm(final Player player, final BlockBreakEvent event, final int radius, final Set<Material> allowed) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        AtomicBoolean exploded = new AtomicBoolean(false);

        if (!PlayerUtil.townCheck(player, block)) {
            return;
        }

        double X = location.getBlockX();
        double Z = location.getBlockZ();
        double minX = X - (double) radius;
        double maxX = X + (double) radius + 1.0D;
        double minZ = Z - (double) radius;
        double maxZ = Z + (double) radius + 1.0D;

        MultiBlockBreakEvent multiBlockBreakEvent = new MultiBlockBreakEvent(player);

        biConsumer((x, z) -> explodeSingleBlock(new Location(block.getWorld(), x, block.getY(), z), player, multiBlockBreakEvent, exploded, allowed, false), new double[]{minX, maxX, minZ, maxZ});

        multiBlockBreakEvent.removeBlock(block);
        if (multiBlockBreakEvent.getBlocks().isEmpty()) return;

        Map<Material, Integer> adjusted = new HashMap<>();
        multiBlockBreakEvent.getBlocks().forEach((it, drop) -> {
            // Check if the block is in the allowed list
            if (!allowed.contains(it.getType())) return;

            int current = adjusted.getOrDefault(it.getType(), 0);
            adjusted.put(it.getType(), current + 1);
        });

        multiBlockBreakEvent.callEvent();
    }

    /**
     * Till all the area around a block in a given radius
     *
     * @param player The owning player
     * @param event  The related event
     * @param radius The radius of the till
     */
    public final void tillFarm(final Player player, final PlayerInteractEvent event, final int radius) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        Location location = block.getLocation();
        if (!PlayerUtil.townCheck(player, block)) {
            return;
        }

        double X = location.getBlockX();
        double Z = location.getBlockZ();
        double minX = X - (double) radius;
        double maxX = X + (double) radius + 1.0D;
        double minZ = Z - (double) radius;
        double maxZ = Z + (double) radius + 1.0D;

        if (event.getItem() != null && event.getItem() instanceof Damageable)
            event.getItem().damage(1, player);

        for (double x = minX; x < maxX; x++) {
            for (double z = minZ; z < maxZ; z++) {
                Location loc = new Location(block.getWorld(), x, block.getY(), z);
                Block blockRetrieved = loc.getBlock();

                // Convert grass or dirt blocks into farmland if they are in the allowed list
                if (blockRetrieved.getType() == Material.GRASS_BLOCK || blockRetrieved.getType() == Material.DIRT) {
                    if (!blockRetrieved.getRelative(BlockFace.UP).getType().isAir()) continue;

                    if (!PlayerUtil.townCheck(player, blockRetrieved)) return;


                    blockRetrieved.setType(Material.FARMLAND);
                }
            }
        }
    }

    /**
     * Explodes a single block for that position, if a block is exploded adds it to the event
     * If the blocks is the first to explode, creates an explosion effect
     *
     * @param location  The location of the block to explode
     * @param player    The owning player
     * @param event     The related event
     * @param exploded  The exploded status
     * @param filter    The material filter
     * @param blacklist The blacklist status
     */
    public final void explodeSingleBlock(final Location location, final Player player, final MultiBlockBreakEvent event, final AtomicBoolean exploded, final Set<Material> filter, final boolean blacklist) {
        Block blockRetrieved = location.getBlock();
        if ((blacklist && filter.contains(blockRetrieved.getType()))
                || (!blacklist && !filter.contains(blockRetrieved.getType()))) {
            return;
        }

        // Check if the block has an identifier, if it does have one, do not explode
        if (PersistenceUtil.accessBlockData(blockRetrieved).has(PersistenceUtil.NODE_TYPE.key(), DataType.STRING)) {
            return;
        }

//        if (!PlayerUtil.playerCheck(player, blockRetrieved.getLocation(), this.handlerId)) return; // todo: player check
        if (blockRetrieved.getType().isAir() || this.blacklistTypes.contains(blockRetrieved.getType())) return;

        event.addBlock(blockRetrieved);

        blockRetrieved.breakNaturally();
        if (exploded.get()) return;

        exploded.set(true);
        player.getWorld().playSound(location, "entity.generic.explode", 0.5F, 1.0F);
    }

    /**
     * Provides a nicer implementation of a triple for loop
     *
     * @param consumer   The consuming values
     * @param boundaries The boundaries for the consumer
     */
    private void triConsumer(final TriConsumer<Double, Double, Double> consumer, final double[] boundaries) {
        for (double a = boundaries[0]; a < boundaries[1]; a++) {
            for (double b = boundaries[2]; b < boundaries[3]; b++) {
                for (double c = boundaries[4]; c < boundaries[5]; c++) {
                    consumer.accept(a, b, c);
                }
            }
        }
    }

    /**
     * Provides a nicer implementation of a double for loop
     *
     * @param consumer   The consuming values
     * @param boundaries The boundaries for the consumer
     */
    private void biConsumer(final BiConsumer<Double, Double> consumer, final double[] boundaries) {
        for (double a = boundaries[0]; a < boundaries[1]; a++) {
            for (double b = boundaries[2]; b < boundaries[3]; b++) {
                consumer.accept(a, b);
            }
        }
    }
}
