package dev.oribuin.gadgets.gadgets.impl;

import dev.oribuin.gadgets.api.MultiBlockBreakEvent;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.util.PlayerUtil;
import dev.oribuin.gadgets.util.block.VeinUtil;
import dev.oribuin.gadgets.hook.PayoutUtil;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;


@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class LumberAxe extends Gadget {

    @Comment("The upper limit of the amount of blocks that can be broken in a single turn")
    private int breakLimit = 100;
    @Comment("The upper limit of the amount of blocks that can be stripped in a single turn")
    private int stripLimit = 40;
    @Comment("Should we take durability for each block broken by the lumber axe (probably not it sucks to play with)")
    private boolean damagePerBlock = false;
    @Comment("Should only the target of the lumber axe be broken in the lineup")
    private boolean breakOnlyTarget = false;
    @Comment("The list of whitelisted blocks that are not usually tagged under Logs")
    private Set<Material> whitelisted = Set.of(Material.MANGROVE_ROOTS, Material.MUSHROOM_STEM, Material.BROWN_MUSHROOM_BLOCK, Material.RED_MUSHROOM_BLOCK);

    public LumberAxe() {
        super("lumber_axe", ItemConstruct.of(Material.DIAMOND_AXE)
                .setName("<#93bc80><bold>Lumber Axe")
                .setLore("",
                        " <white>| <gray>Allows the user to destroy or strip",
                        " <white>| <gray>all logs connected to a tree"
                )
        );

        this.register(BlockBreakEvent.class, this::handleBlockBreak); // handles breaking
        this.register(PlayerInteractEvent.class, this::handleInteract); // handles stripping 
    }

    /**
     * Handles any functionality behind a player breaking a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockBreak(ContextProvider<BlockBreakEvent, ItemStack> provider) {
        BlockBreakEvent event = provider.event();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player.isSneaking()) return;
        if (!Tag.LOGS.isTagged(block.getType())) return;

        Predicate<Block> onlyTarget = this.breakOnlyTarget
                ? b -> b.getType().equals(block.getType())
                : b -> Tag.LOGS.isTagged(b.getType()) || this.whitelisted.contains(b.getType());

        List<Block> logs = VeinUtil.find(block, this.breakLimit, onlyTarget);
        Map<Material, Integer> mapped = new HashMap<>();
        if (logs.size() <= 1) return;

        logs.remove(block);
        logs.forEach(it -> {
            int current = mapped.getOrDefault(it.getType(), 0);
            mapped.put(it.getType(), current + 1);
        });

        if (this.damagePerBlock) {
            ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
            stack.damage(logs.size(), event.getPlayer());
        }

        MultiBlockBreakEvent multiBlockBreakEvent = new MultiBlockBreakEvent(player).addBlocks(logs);
        multiBlockBreakEvent.callEvent();

        Map<Material, Integer> adjusted = PayoutUtil.getAdjusted(mapped);
//        PayoutUtil.getMultiBreakObjective().grant(player, adjusted);
        PayoutUtil.addMcMMOXp(player, adjusted);
        logs.forEach(Block::breakNaturally);
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, ItemStack> provider) {
        PlayerInteractEvent event = provider.event();
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (player.isSneaking()) return;
        if (block == null) return;
        if (!Tag.LOGS.isTagged(block.getType())) return;
        if (!PlayerUtil.townCheck(player, block) || !PlayerUtil.playerCheck(player, block.getLocation(), this)) {
            return;
        }

        List<Block> logs = VeinUtil.find(block, this.stripLimit, b -> Tag.LOGS.isTagged(b.getType()) && !b.getType().name().contains("STRIPPED"));
        if (logs.size() <= 1) return;

        logs.remove(block);
        logs.forEach(it -> {
            Orientable orientable = (Orientable) it.getBlockData();
            Material found = Material.getMaterial("STRIPPED_" + it.getType().name());
            if (found == null) {
                return;
            }

            if (!PlayerUtil.playerCheck(player, block.getLocation(), this)) {
                return;
            }

            Axis axis = orientable.getAxis();
            it.setType(found);
            Orientable updated = (Orientable) it.getBlockData();

            updated.setAxis(axis);
            it.setBlockData(updated);
        });

        if (this.damagePerBlock) {
            ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
            stack.damage(logs.size(), event.getPlayer());
        }
    }

}
