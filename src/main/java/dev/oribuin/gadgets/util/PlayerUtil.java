package dev.oribuin.gadgets.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.oribuin.gadgets.gadgets.Gadget;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class PlayerUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerUtil.class);

    /**
     * A method to return an itemstack with a specified amount to a player,
     * or drop it on the ground if their inventory is full
     */
    public static void giveOrDropItem(final Player player, final ItemStack item, String reason) {
        if (item == null || item.getType().isAir()) return;

        // todo: giveordropitem messages
        ItemStack toGive = item.clone();

        Map<Integer, ItemStack> notGiven = player.getInventory().addItem(toGive);
        if (!notGiven.isEmpty()) {
            for (ItemStack stack : notGiven.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), stack);
//                MessageConfig.get().getGivingItemBackInventoryFull().send(player,
//                        "item", MessageHandler.replaceLegacyCodes(MiniMessage.miniMessage().serialize(item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().displayName() : Component.translatable(item))),
//                        "reason", reason);
            }
        } else {
//            MessageConfig.get().getGivingItemBack().send(player,
//                    "item", MessageHandler.replaceLegacyCodes(MiniMessage.miniMessage().serialize(item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().displayName() : Component.translatable(item))),
//                    "reason", reason);
        }
    }

    /**
     * Checks if a player can interact at the specified location for the given item identifier
     *
     * @param player   The player to check
     * @param location The location
     * @param gadget   The gadget
     * @return Result of the interaction check
     */
    public static boolean playerCheck(Player player, Location location, Gadget gadget) {
        if (gadget.getDisabledWorlds().contains(location.getWorld().getName())) return false;
        if (!townySwitchCheck(player, location.getBlock())) return false;
        return checkRegion(location, gadget);
    }

    /**
     * Check if a gadget is disabled in a specified region
     *
     * @param location The location to check
     * @param gadget   The gadget to check
     * @return if the gadget is allowed to be used in that world
     */
    public static boolean checkRegion(Location location, Gadget gadget) {
        if (gadget.getDisabledRegions().isEmpty()) return true;

        com.sk89q.worldedit.util.Location adaptedLocation = BukkitAdapter.adapt(location);
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(location.getWorld());
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(adaptedWorld);
        if (manager == null) return true;

        ApplicableRegionSet regionSet = manager.getApplicableRegions(adaptedLocation.toVector().toBlockPoint(), RegionQuery.QueryOption.SORT);
        if (regionSet.getRegions().isEmpty()) return true;

        boolean allowed = true;
        for (ProtectedRegion region : regionSet.getRegions()) {
            if (!gadget.getDisabledRegions().contains(region.getId())) continue;
            allowed = false;
            break;
        }

        return allowed;
    }

//    /**
//     * Checks if a player can interact with the block in a blocks owning town
//     *
//     * @param player The player to check
//     * @param block  The block to check
//     * @return Result of the town check
//     */
//    public static boolean griefPreventionCheck(final Player player, final Block block) {
//        if (!Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) return true;
//
//        GriefPrevention plugin = GriefPrevention.instance;
//        Location location = block.getLocation();
//
//        if (plugin.dataStore.getClaimAt(location, true, null) == null) return true;
//        return plugin.dataStore.getClaimAt(location, true, null).allowAccess(player) == null;
//    }
//
//    /**
//     * Checks if a player can interact with the block in a blocks owning town
//     *
//     * @param player The player to check
//     * @param block  The block to check
//     * @return Result of the town check
//     */
//    public static boolean griefPreventionClaim(final Player player, final Block block) {
//        if (!Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) return true;
//
//        GriefPrevention plugin = GriefPrevention.instance;
//        Location location = block.getLocation();
//
//        return plugin.dataStore.getClaimAt(location, true, null) != null;
//    }

    /**
     * Checks if a player can interact with the block in a blocks owning town
     *
     * @param player The player to check
     * @param block  The block to check
     * @return Result of the town check
     */
    public static boolean townCheck(final Player player, final Block block) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Towny")) return true;
        if (TownyAPI.getInstance().isWilderness(block)) return true;

        return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.DESTROY);
    }

    /**
     * Checks if a player can interact with the block in a blocks owning town or nation.
     * Uses TownyAdvanced API.
     *
     * @param player The player to check
     * @param block  The block to check
     * @return Result of the town check
     */
    public static boolean townySwitchCheck(final Player player, final Block block) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Towny")) return true;
        if (TownyAPI.getInstance().isWilderness(block)) return true;

        return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.SWITCH);
    }

    /**
     * Checks if a player can interact with an item in a blocks owning town or nation.
     * Uses TownyAdvanced API.
     *
     * @param player The player to check
     * @param block  The block to check
     * @return Result of the town check
     */
    public static boolean townyItemCheck(final Player player, final Block block) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Towny")) return true;
        if (TownyAPI.getInstance().isWilderness(block)) return true;

        return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.ITEM_USE);
    }

    /**
     * Checks if the block is in a town or nation
     *
     * @param block The block to check
     * @return Result of the town check
     */
    public static boolean isTown(final Block block) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Towny")) return true;

        return !TownyAPI.getInstance().isWilderness(block);
    }
}
