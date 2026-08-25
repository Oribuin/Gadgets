package dev.oribuin.gadgets.gadgets.impl;

import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import dev.oribuin.gadgets.config.TextMessage;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class MagicalZombiePills extends Gadget {

    @Comment("The message sent when a player cures a zombie")
    private TextMessage curedVillager = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You have cured the zombie villager");

    public MagicalZombiePills() {
        super("magic_zombie_pills", ItemConstruct.of(Material.PAPER)
                .setName("<#93bc80><bold>Magic Zombie Pills")
                .setLore("",
                        " <white>| <gray>Reduces the time it takes for a",
                        " <white>| <gray>zombie villager to become human",
                        " <white>| <gray>to 3 seconds"
                )
        );

        this.register(PlayerInteractAtEntityEvent.class, this::handleEntityInteract);
    }

    /**
     * Handles any functionality behind a player interacting with an entity in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleEntityInteract(ContextProvider<PlayerInteractAtEntityEvent, ItemStack> provider) {
        PlayerInteractAtEntityEvent event = provider.event();
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof ZombieVillager villager)) return;
        if (villager.isConverting()) return;

        itemStack.setAmount(itemStack.getAmount() - 1);
        player.getInventory().setItemInMainHand(itemStack);
        villager.setConversionTime(60, true);
        villager.setConversionPlayer(player);

        curedVillager.send(player);
    }

}
