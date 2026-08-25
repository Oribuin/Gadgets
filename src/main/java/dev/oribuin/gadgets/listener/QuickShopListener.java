package dev.oribuin.gadgets.listener;

import org.bukkit.event.Listener;

/**
 * likely uses mango-chestshops instead <3
 */
public class QuickShopListener implements Listener {

//    /**
//     * Cancels {@link ShopPreCreateEvent} if the matching {@link Block} is a {@link BlockType#BARREL}
//     *
//     * @param event The event
//     */
//    @EventHandler
//    public void onShopCreate(final ShopPreCreateEvent event) {
//        Block block = event.getLocation().getBlock();
//        if (block.getType() != Material.BARREL) {
//            return;
//        }
//
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//        BlockType type = BlockType.getNullable(data.get(PersistenceUtil.Key.NODE_TYPE, DataType.STRING));
//        if (type != BlockType.BARREL) {
//            return;
//        }
//
//        MessageConfig.get().getShopOnCustomBarrelDeny().send(event.getCreator().getBukkitPlayer().get());
//        event.setCancelled(true, "You cannot create a shop on a custom barrel!");
//    }

}
