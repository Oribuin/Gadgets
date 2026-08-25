package dev.oribuin.gadgets.gadgets.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import dev.oribuin.gadgets.config.TextMessage;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class TapeMeasure extends Gadget {

    private transient final Cache<UUID, Location> anchorPoints = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    private TextMessage setAnchor = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You have set an anchor point at <#3acbe8><x><gray>/<#3acbe8><y><gray>/<#3acbe8><z>");
    private TextMessage removedAnchor = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You have removed the original anchor point at <#3acbe8><x><gray>/<#3acbe8><y><gray>/<#3acbe8><z>");
    private TextMessage distance = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>The distance between both anchor points is <#3acbe8><distance>");
    private TextMessage noAnchor = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You do not have an anchor point set");
    private TextMessage wrongWorld = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You are in a different world from your anchor point, Please set it again");

    public TapeMeasure() {
        super("tape_measure", ItemConstruct.of(Material.PAPER)
                .setName("<#93bc80><bold>Tape Measure")
                .setLore("",
                        " <white>| <gray>Used to calculate the distance",
                        " <white>| <gray>between two blocks"
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
        Player player = event.getPlayer();
        if (!event.getAction().isRightClick()) return; // check if not right-click so it doesn't go off with pressure plates

        Location location = event.getClickedBlock() == null ? player.getLocation() : event.getClickedBlock().getLocation();

        // Sets the original anchor point if the player is sneaking
        if (player.isSneaking()) {
            Location current = this.anchorPoints.getIfPresent(player.getUniqueId());
            if (current != null) {
                this.removedAnchor.send(player, "x", current.getX(), "y", current.getY(), "z", current.getZ());
                this.anchorPoints.invalidate(player.getUniqueId());
                return;
            }

            // Add the anchor point
            this.anchorPoints.put(player.getUniqueId(), location);
            this.setAnchor.send(player, "x", location.getX(), "y", location.getY(), "z", location.getZ());
            return;
        }

        // Check if an anchor exists to be measured
        Location current = this.anchorPoints.getIfPresent(player.getUniqueId());
        if (current == null) {
            this.noAnchor.send(player);
            return;
        }

        // Check if the worlds are different (errors if we measure in different worlds)
        if (current.getWorld() != location.getWorld()) {
            this.anchorPoints.invalidate(player.getUniqueId());
            this.wrongWorld.send(player);
            return;
        }

        int distance = (int) Math.floor(location.distance(current)) + 1;
        this.distance.send(player, "distance", distance);
    }

}
