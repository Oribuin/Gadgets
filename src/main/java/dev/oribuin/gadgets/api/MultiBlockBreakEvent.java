package dev.oribuin.gadgets.api;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import dev.oribuin.gadgets.util.PersistenceUtil;
import dev.oribuin.gadgets.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;

import static dev.oribuin.gadgets.util.PersistenceUtil.NODE_TYPE;

public final class MultiBlockBreakEvent extends Event {

    private static final SplittableRandom RANDOM = new SplittableRandom();
    private static final Set<Material> PERMITTED_EXPERIENCE = EnumSet.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE
    );
    private final Map<Block, Boolean> blocks = new HashMap<>();
    private final Player player;

    private int damagePerBlock = 1;

    public MultiBlockBreakEvent(final Player player) {
        this.player = player;
    }

    /**
     * Adds a block to the event
     *
     * @param block The block to add
     */
    public void addBlock(final Block block) {
        CustomBlockData data = PersistenceUtil.accessBlockData(block);
        String type = data.get(NODE_TYPE.key(), DataType.STRING);
        if (type != null && !type.isEmpty()) return;

        if (!PlayerUtil.townCheck(player, block)) return;
        if (Tag.SIGNS.isTagged(block.getType())) return;

        this.blocks.put(block, true);
    }

    public void removeBlock(final Block block) {
        this.blocks.remove(block);
    }

    /**
     * Adds a collection of blocks to the event
     *
     * @param blocks The blocks to add
     * @return this event with updated contents
     */
    public MultiBlockBreakEvent addBlocks(final Collection<Block> blocks) {
        blocks.forEach(this::addBlock);
        return this;
    }

    /**
     * If the base event should drop block drops
     *
     * @return the current status of block drops
     */
    public boolean shouldDrop(final Block block) {
        return this.blocks.get(block);
    }

    /**
     * Set the outcome of breaking blocks
     *
     * @param value whether blocks should drop drops
     */
    public void setDropStatus(final Block block, final boolean value) {
        this.blocks.put(block, value);
    }

    /**
     * Updates the amount of damage the involved item should take
     *
     * @param value the damage for the item to take
     */
    public void setDamagePerBlock(final int value) {
        this.damagePerBlock = value;
    }

    /**
     * Applies the current event damage to the specified ItemStack
     *
     * @param stack the stack to apply the damage to
     */
    public void applyDamage(final ItemStack stack) {
        Damageable meta = (Damageable) stack.getItemMeta();
        int level = meta.getEnchantLevel(Enchantment.UNBREAKING);
        int percentage = level == 0 ? 100 : (100 / (level + 1));

        if (percentage >= RANDOM.nextDouble(0, 100)) {
            meta.setDamage(meta.getDamage() + this.damagePerBlock);
            stack.setItemMeta(meta);
        }
    }

    /**
     * Drops a random amount of experience at the broken block
     *
     * @param block the block related for this drop
     */
    public void dropExperience(final Block block) {
        if (!PERMITTED_EXPERIENCE.contains(block.getType())) return;

        block.getLocation().getWorld().spawnEntity(block.getLocation(), EntityType.EXPERIENCE_ORB, CreatureSpawnEvent.SpawnReason.CUSTOM, (entity) -> {
            ExperienceOrb orb = (ExperienceOrb) entity;

            orb.setExperience(RANDOM.nextInt(1, 6));
        });
    }

    /**
     * @return Involved player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return The blocks involved in this event
     */
    public Map<Block, Boolean> getBlocks() {
        return this.blocks;
    }

    // Default Event Stuff

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
