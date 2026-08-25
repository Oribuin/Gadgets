package dev.oribuin.gadgets.gadgets.impl;

import com.jeff_media.morepersistentdatatypes.DataType;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.TextMessage;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import dev.oribuin.gadgets.config.TextMessage;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class InfusedMagnet extends Gadget {

    private static final NamespacedKey VANISH_KEY = NamespacedKey.fromString("mango-core:vanish_status");
    private transient final Set<UUID> active = new HashSet<>();
    @Comment("The X/Y/Z pickup radius for all magnets.")
    private int pickupRadius = 10;
    @Comment("The comment sent when a player enables their magnet")
    private TextMessage enabledMagnet = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You have enabled your infused magnet")
            .actionbar("<white>[ <#3ACBE8>Magnet Enabled <white>]");
    @Comment("The message sent when a player disables their magnet.")
    private TextMessage disabledMagnet = new TextMessage("<#3ACBE8><bold>Gadgets</bold> <gray>| <white>You have disabled your infused magnet")
            .actionbar("<white>[ <#3ACBE8>Magnet Disabled <white>]");
    private transient BukkitTask task;

    public InfusedMagnet() {
        super("infused_magnet", ItemConstruct.of(Material.PAPER)
                .setName("<#93bc80><bold>Infused Magnet")
                .setLore("",
                        " <white>| <gray>Teleports all nearby items",
                        " <white>| <gray>within a 10x10x10 radius to you"
                )
        );

        this.register(PlayerToggleSneakEvent.class, this::handleSneak);

        // Run the scheduled task (We don't use Scheduler because then it will never cancel if the plugin is plugman'd)
        this.active.clear();
        if (this.task != null) {
            this.task.cancel();
        }

        this.task = GadgetsPlugin.get().getServer().getScheduler().runTaskTimer(
                GadgetsPlugin.get(),
                this::infusedMagnetTask,
                10, 10
        );
    }

    /**
     * Handles any functionality behind a player toggling their sneak on or off
     *
     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
     */
    @Override
    public void handleSneak(ContextProvider<PlayerToggleSneakEvent, ItemStack> provider) {
        PlayerToggleSneakEvent event = provider.event();
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            this.deactivateMagnet(player);
            return;
        }

        this.activateMagnet(player);
    }

    /**
     * Activate the magnet for the player
     *
     * @param player The player enabling their magnet
     */
    private void activateMagnet(Player player) {
        if (this.active.contains(player.getUniqueId())) return;

        this.enabledMagnet.send(player);
        this.active.add(player.getUniqueId());
    }

    /**
     * Deactivate the magnet for the player
     *
     * @param player The player disabling their magnet
     */
    private void deactivateMagnet(final Player player) {
        if (this.active.remove(player.getUniqueId())) {
            this.disabledMagnet.send(player);
        }
    }

    /**
     * Run the infused magnet task for the players with it active
     */
    private void infusedMagnetTask() {
        if (this.active.isEmpty()) return;

        try {
            for (UUID identifier : this.active) {
                Player player = Bukkit.getServer().getPlayer(identifier);
                boolean playSound = false;

                // Remove the player from the list of active if vanished
                if (player == null) {
                    this.active.remove(identifier);
                    continue;
                }

                // Don't use the magnet if the player is in vanish
                if (VANISH_KEY != null) {
                    Integer vanish = player.getPersistentDataContainer().get(VANISH_KEY, DataType.INTEGER);
                    if (vanish != null && vanish == 1) continue;
                }

                Location location = player.getLocation();
                for (Entity entity : player.getNearbyEntities(this.pickupRadius, this.pickupRadius, this.pickupRadius)) {
                    if (!(entity instanceof Item item)) continue;

                    if (item.getPickupDelay() <= 0 && location.distanceSquared(item.getLocation()) > 0.3) {
                        item.teleport(location);
                        playSound = true;
                    }
                }

                // Only play a sound if an Item was found
                if (playSound) {
                    player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.25F, 0.9F);
                }
            }
        } catch (final ConcurrentModificationException ignored) {
        }
    }

}
