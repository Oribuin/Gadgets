package dev.oribuin.gadgets.gadgets.impl;

import com.destroystokyo.paper.ParticleBuilder;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import dev.oribuin.gadgets.config.TextMessage;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class WindStaff extends Gadget {

    private static final ParticleBuilder STAFF_EFFECT = new ParticleBuilder(Particle.CLOUD)
            .count(10)
            .extra(0)
            .offset(0.5, 0.0, 0.5);

    private double launchPower = 5;
    private int hungerCost = 1;
    private boolean particles = true;

    @Comment("The message sent when a player does not have enough hunger to use the item")
    private TextMessage noHunger = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You are too hungry to use this item.");

    public WindStaff() {
        super("wind_staff", ItemConstruct.of(Material.PAPER)
                .setName("<#93bc80><bold>Wind Staff")
                .setLore("",
                        " <white>| <gray>Consumes a hunger bar to",
                        " <white>| <gray>accelerate the user forward"
                )
        );

        this.register(PlayerInteractEvent.class, this::handleInteract);
    }

    /**
     * Handles any functionality behind a player interacting with an item in any capacity
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleInteract(ContextProvider<PlayerInteractEvent, ItemStack> provider) {
        PlayerInteractEvent event = provider.event();
        if (event.useItemInHand() == Event.Result.DENY) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();

        if (player.getFoodLevel() - this.hungerCost <= 0) {
            this.noHunger.send(player);
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setFoodLevel(player.getFoodLevel() - hungerCost);
        }

        player.setVelocity(player.getLocation().getDirection().multiply(launchPower));
        player.setVelocity(new Vector(player.getVelocity().getX(), player.getVelocity().getY(), player.getVelocity().getZ()));

        if (this.particles) {
            Bukkit.getScheduler().runTaskAsynchronously(GadgetsPlugin.get(), () -> STAFF_EFFECT.clone()
                    .location(player.getLocation().clone().subtract(0, 1, 0))
                    .source(player)
                    .spawn());
        }
    }

}
