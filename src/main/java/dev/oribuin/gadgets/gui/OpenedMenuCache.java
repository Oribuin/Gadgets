package dev.oribuin.gadgets.gui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class OpenedMenuCache {

    private static final Map<Vector3D, InventoryHolder> INVENTORY_CACHE = new HashMap<>();
    private static final Set<Vector3D> OPENED_INVENTORIES = new HashSet<>();

    /**
     * Adds a viewer to the given inventory location
     *
     * @param location The desired location
     * @param player   The viewing player
     * @return The status of viewer addition
     */
    public static boolean addViewer(final Location location, final Player player) {
        InventoryHolder holder = INVENTORY_CACHE.get(asVector(location));
        if (holder == null) {
            return false;
        }

        if (holder.viewers.isEmpty()) {
            return false;
        }

        holder.viewers.add(player.getUniqueId());
        player.openInventory(holder.inventory);
        INVENTORY_CACHE.put(asVector(location), holder);
        return true;
    }

    /**
     * Gets the inventory viewers for a given location
     *
     * @param location The desired Location
     * @return The Set of viewing players
     */
    public static Set<Player> getViewers(final Location location) {
        return INVENTORY_CACHE.get(asVector(location)).viewers.stream().map(Bukkit::getPlayer).collect(Collectors.toSet());
    }

    /**
     * Removes an inventory viewer
     * (Multi-viewing)
     *
     * @param location The desired Location
     * @param player   The player to remove
     * @return Status of whether the viewer was removed
     */
    public static boolean removeViewer(final Location location, final Player player) {
        InventoryHolder holder = INVENTORY_CACHE.get(asVector(location));
        if (holder == null) {
            return false;
        }

        if (holder.initialViewer.getUniqueId().equals(player.getUniqueId())) {
            holder.viewers.stream().filter(it -> !it.equals(player.getUniqueId())).map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(Player::closeInventory);
            INVENTORY_CACHE.remove(asVector(location));
            return true;
        }

        holder.viewers.remove(player.getUniqueId());
        INVENTORY_CACHE.put(asVector(location), holder);
        return false;
    }

    /**
     * Adds an inventory to cache
     *
     * @param location  The inventory location
     * @param inventory The inventory
     * @param player    The owning player
     */
    public static void addInventoryToCache(final Location location, final Inventory inventory, final Player player) {
        INVENTORY_CACHE.put(asVector(location), new InventoryHolder(inventory, player));
    }

    /**
     * Adds an opened inventory for the given location
     *
     * @param location The location to remove
     */
    public static void addOpenedInventory(final Location location) {
        OPENED_INVENTORIES.add(asVector(location));
    }

    /**
     * Removes an opened inventory for the given location
     *
     * @param location The location to remove
     */
    public static void removeOpenedInventory(final Location location) {
        OPENED_INVENTORIES.remove(asVector(location));
    }

    /**
     * Checks if the location contains an opened Inventory
     *
     * @param location The location to check
     * @return Result of the check
     */
    public static boolean isOpenedInventoryAt(final Location location) {
        return OPENED_INVENTORIES.contains(asVector(location));
    }

    /**
     * Returns a location as a vector
     *
     * @param location Location to transform
     * @return Transformed vector location
     */
    private static Vector3D asVector(final Location location) {
        return new Vector3D(location);
    }

    private static class InventoryHolder {

        private final Set<UUID> viewers = new HashSet<>();
        private final Inventory inventory;
        private final Player initialViewer;

        InventoryHolder(final Inventory inventory, final Player initialViewer) {
            this.inventory = inventory;
            this.viewers.add(initialViewer.getUniqueId());
            this.initialViewer = initialViewer;
        }

    }

    public static class Vector3D {

        public final int x;
        public final int y;
        public final int z;
        public final String world;

        public Vector3D(final Location location) {
            this.x = location.getBlockX();
            this.y = location.getBlockY();
            this.z = location.getBlockZ();
            this.world = location.getWorld().getName();
        }

        public Location asLocation() {
            return new Location(
                    Bukkit.getWorld(this.world),
                    this.x,
                    this.y,
                    this.z
            );
        }

        public boolean isLoadedChunkFor() {
            World world = Bukkit.getWorld(this.world);
            if (world == null) return false;

            return world.isChunkLoaded(this.x >> 4, this.z >> 4);
        }

        @Override
        public String toString() {
            return "Vector3D[x=" + x + ", y=" + y + ", z=" + z + ", world='" + world + "']";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Vector3D vector3D = (Vector3D) o;

            if (x != vector3D.x) return false;
            if (y != vector3D.y) return false;
            if (z != vector3D.z) return false;
            return world.equals(vector3D.world);
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            result = 31 * result + world.hashCode();
            return result;
        }
    }

}
