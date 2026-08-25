package dev.oribuin.gadgets.gadgets.impl;

import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import io.papermc.paper.entity.Shearable;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class MultiTool extends Gadget {

    private transient final Map<UUID, Long> interactCooldown = new HashMap<>();

    @Comment("Cooldown between changing from interactions (prevents load)")
    private Duration interactionCooldown = Duration.ofMillis(500);

    public MultiTool() {
        super("multi_tool", ItemConstruct.of(Material.NETHERITE_PICKAXE)
                .setName("<#93bc80><bold>Multi Tool")
                .setLore("",
                        " <white>| <gray>A tool that changes it's type",
                        " <white>| <gray>depending on what the",
                        " <white>| <gray>best tool for a job is"
                )
        );

        this.register(BlockDamageEvent.class, this::handleBlockDamage); // Block Breaking
        this.register(PlayerInteractEvent.class, this::handleInteract); // Wood Stripping / Paths
        this.register(EntityDamageByEntityEvent.class, this::handleEntityDamage); // Damages mobs
//        this.register(PlayerInteractAtEntityEvent.class, this::handleShear); // Shears sheep
    }


    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, ItemStack> provider) {
        PlayerInteractEvent event = provider.event();
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null || !event.getAction().isRightClick()) return;

        // Add a mini cooldown for swapping types to prevent swap load
        if (this.interactionCooldown.toMillis() > 0) {
            long current = System.currentTimeMillis();
            long duration = this.interactCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (duration + this.interactionCooldown.toMillis() < current) return;
            this.interactCooldown.put(player.getUniqueId(), current);
        }

        this.handleChange(player, event.getHand(), block.getType(), false);
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
        Material blockType = event.getBlock().getType();

        this.handleChange(player, EquipmentSlot.HAND, blockType, true);
    }

    /**
     * Handles any functionality behind a player damaging an entity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleEntityDamage(ContextProvider<EntityDamageByEntityEvent, ItemStack> provider) {
        EntityDamageByEntityEvent event = provider.event();
        if (!(event.getDamager() instanceof Player player)) return;

        // Add a mini cooldown for swapping types to prevent swap load
        if (this.interactionCooldown.toMillis() > 0) {
            long current = System.currentTimeMillis();
            long duration = this.interactCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (duration + this.interactionCooldown.toMillis() < current) return;
            this.interactCooldown.put(player.getUniqueId(), current);
        }

        this.migrateType(player, EquipmentSlot.HAND, Material.NETHERITE_AXE);
    }

    /**
     * Handle the shearing of sheep/bogged for the multi tool
     *
     * @param event The shearing event
     */
    public void handleShear(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Shearable)) return; // Make sure the entity is shearable

        Player player = event.getPlayer();

        // Add a mini cooldown for swapping types to prevent swap load
        if (this.interactionCooldown.toMillis() > 0) {
            long current = System.currentTimeMillis();
            long duration = this.interactCooldown.getOrDefault(player.getUniqueId(), 0L);
            if (duration + this.interactionCooldown.toMillis() < current) return;
            this.interactCooldown.put(player.getUniqueId(), current);
        }

        this.migrateType(player, event.getHand(), Material.SHEARS);
    }

    private void handleChange(Player player, EquipmentSlot slot, Material material, boolean isBlockDamage) {
        boolean playerIsSneaking = player.isSneaking();

        if (Tag.MINEABLE_AXE.isTagged(material)) {
            this.migrateType(player, slot, Material.NETHERITE_AXE);
        } else if ((Tag.DIRT.isTagged(material) && !playerIsSneaking && !isBlockDamage) || Tag.MINEABLE_HOE.isTagged(material)) {
            this.migrateType(player, slot, Material.NETHERITE_HOE);
        } else if (Tag.MINEABLE_SHOVEL.isTagged(material)) {
            this.migrateType(player, slot, Material.NETHERITE_SHOVEL);
//        } else if (Tag.WOOL.isTagged(material)) {
//            this.migrateType(player, slot, Material.SHEARS);
        } else {
            this.migrateType(player, slot, Material.NETHERITE_PICKAXE);
        }
    }

    /**
     * Migrate the itemstack type with a new itemstack (setType is deprecated now )
     *
     * @param player   The player holding the item
     * @param slot     The slot the item is in
     * @param material The material of the item
     */
    private void migrateType(Player player, EquipmentSlot slot, Material material) {
        ItemStack stack = player.getInventory().getItem(slot);
        if (stack.getType() == material) return; // don't migrate types if they're already the same block 
        player.getInventory().setItem(slot, stack.withType(material));
    }

}
