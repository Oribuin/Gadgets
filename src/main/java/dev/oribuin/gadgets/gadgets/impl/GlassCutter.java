package dev.oribuin.gadgets.gadgets.impl;

import com.destroystokyo.paper.MaterialTags;
import dev.oribuin.gadgets.config.item.ConstructType;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.config.item.component.GlowingItemType;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "UnstableApiUsage"})
public final class GlassCutter extends Gadget {

    public GlassCutter() {
        super("glass_cutter", ItemConstruct.of(Material.PAPER)
                .setName("<#93bc80><bold>Glass Cutter")
                .setLore("",
                        " <white>| <gray>Can be used to instantly destroy",
                        " <white>| <gray>and collect glass"
                )
                .setProperty(ConstructType.GLOWING, GlowingItemType.ENABLED)
        );

        this.register(BlockDamageEvent.class, this::handleBlockDamage);
    }

    /**
     * Handles any functionality behind a player initially damaging a block
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleBlockDamage(ContextProvider<BlockDamageEvent, ItemStack> provider) {
        BlockDamageEvent event = provider.event();
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Only act if the block being damaged is a glass block
        if (MaterialTags.GLASS.isTagged(block) || MaterialTags.GLASS_PANES.isTagged(block)) {
            // Break the block immediately
            event.setInstaBreak(true);

            // Produce block break effects, as setting InstaBreak to true prevents them from appearing
            player.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
        }
    }

}
