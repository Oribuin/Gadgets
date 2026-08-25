package dev.oribuin.gadgets.gadgets.impl;

import com.destroystokyo.paper.MaterialTags;
import dev.oribuin.gadgets.api.MultiBlockBreakEvent;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import dev.oribuin.gadgets.util.block.VeinUtil;
import dev.oribuin.gadgets.hook.PayoutUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class VeinMiner extends Gadget {

    private int limit = 100;

    public VeinMiner() {
        super("vein_miner", ItemConstruct.of(Material.DIAMOND_PICKAXE)
                .setName("<#93bc80><bold>Vein Miner")
                .setLore("",
                        " <white>| <gray>Used to destroy a vein of ores",
                        " <white>| <gray>in a single swing"
                )
        );

        this.register(BlockBreakEvent.class, this::handleBlockBreak);
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
        if (player.isSneaking()) return;

        Block block = event.getBlock();
        if (!MaterialTags.ORES.isTagged(block)) return;

        List<Block> ores = VeinUtil.find(block, this.limit, b -> b.getType() == block.getType());
        if (ores.size() <= 1 || !ores.remove(block)) return;

        MultiBlockBreakEvent multiBlockBreakEvent = new MultiBlockBreakEvent(player).addBlocks(ores);

        Map<Material, Integer> mapped = new HashMap<>();
        multiBlockBreakEvent.getBlocks().forEach((it, drop) -> {
            int current = mapped.getOrDefault(it.getType(), 0);
            mapped.put(it.getType(), current + 1);
        });

        multiBlockBreakEvent.callEvent();
        Map<Material, Integer> adjusted = PayoutUtil.getAdjusted(mapped);
        PayoutUtil.addMcMMOXp(player, adjusted);
    }
}
