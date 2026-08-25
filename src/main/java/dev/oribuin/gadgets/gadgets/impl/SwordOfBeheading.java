package dev.oribuin.gadgets.gadgets.impl;

import dev.oribuin.gadgets.config.TextMessage;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Map;
import java.util.Random;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class SwordOfBeheading extends Gadget {

    private static final Random RANDOM = new Random();
    private double lootingBonus = 5;
    private TextMessage playerBeheadAnother = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You have taken off <#3ACBE8><target><white>'s head");
    private TextMessage playerBeheaded = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>Your head has been taken off by <#3ACBE8><killer>");

    private Map<EntityType, Double> entityDropChance = Map.of(
            EntityType.PLAYER, 25.0,
            EntityType.ZOMBIE, 5.0,
            EntityType.CREEPER, 5.0,
            EntityType.WITHER_SKELETON, 5.0,
            EntityType.SKELETON, 5.0
    );

    private Map<EntityType, Material> headMapping = Map.of(
            EntityType.SKELETON, Material.SKELETON_SKULL,
            EntityType.ZOMBIE, Material.ZOMBIE_HEAD,
            EntityType.CREEPER, Material.CREEPER_HEAD,
            EntityType.WITHER_SKELETON, Material.WITHER_SKELETON_SKULL
    );

    public SwordOfBeheading() {
        super("sword_of_beheading", ItemConstruct.of(Material.IRON_SWORD)
                .setName("<#93bc80><bold>Sword Of Beheading")
                .setLore("",
                        " <white>| <gray>Increases the chance of getting",
                        " <white>| <gray>a head from a mob substantially"
                )
        );

        this.register(EntityDeathEvent.class, this::handleEntityDeath);
    }

    /**
     * Handle the beheading of entities, this one will also require a bit of event fuckery
     *
     * @param provider The event provider being handled
     */
    public void handleEntityDeath(ContextProvider<EntityDeathEvent, ItemStack> provider) {
        EntityDeathEvent event = provider.event();
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        this.runBeheadingTask(event.getEntity(), killer);
    }

    /**
     * Drop the head on the ground for the person that was killed
     *
     * @param entity The entity being beheaded
     * @param killer The person who beheaded the entity
     */
    private void runBeheadingTask(Entity entity, Player killer) {
        double generated = RANDOM.nextDouble(0, 100);
        double multiplier = killer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOTING);

        Double dropChance = this.entityDropChance.get(entity.getType());
        if (dropChance == null || dropChance < 0) return;

        double required = dropChance + (multiplier * this.lootingBonus);
        if (generated > required) return;

        ItemStack drop = this.createSkull(entity);
        if (drop == null) return;

        entity.getWorld().dropItem(entity.getLocation(), drop.asQuantity(1));
        this.playerBeheadAnother.send(killer, "killer", entity.getName(), "target", entity.getName());

        if (entity instanceof Player) {
            this.playerBeheaded.send(entity, "killer", entity.getName(), "target", entity.getName());
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Nullable
    private ItemStack createSkull(final Entity owner) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);

        // players are unique <3
        if (owner instanceof Player player) {
            skull.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
        }

        Material skullType = this.headMapping.get(owner.getType());
        return skullType != null ? new ItemStack(skullType) : null;
    }

}
