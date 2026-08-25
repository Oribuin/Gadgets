package dev.oribuin.gadgets.node.impl;

public class Teleporter {

//    private final Map<UUID, Location> linkingMap = new HashMap<>();
//
//    @Override
//    public boolean executeOnPlace(BlockHandleHolder event) {
//        // Teleporters must be placed inside a town
//        //            MessageConfig.get().getRequiresTown().send(event.getPlayer());
//        return !PlayerUtil.isTown(event.getBlock()) || !PlayerUtil.griefPreventionClaim(event.getPlayer(), event.getBlock());
//    }
//
//    @Override
//    public boolean executeOnClick(BlockHandleHolder event) {
//        Player player = event.getPlayer();
//        Block block = event.getBlock();
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//
//        if (!PlayerUtil.townySwitchCheck(player, block) || !PlayerUtil.griefPreventionCheck(player, block)) {
//            return true;
//        }
//
//        // Teleporters must be placed inside a town
//        if (!PlayerUtil.isTown(block) || !PlayerUtil.griefPreventionClaim(player, block)) {
////            MessageConfig.get().getRequiresTown().send(player); // todo message(requires town)
//            return true;
//        }
//
//        // Link the teleporter to another teleporter
//        Location toLink = this.linkingMap.get(player.getUniqueId());
//        if (toLink != null) {
//            this.linkTeleporter(player, toLink.getBlock(), block);
//            return true;
//        }
//
//        // Open the teleporter menu if the teleporter is linked
//        Location blockLocation = deserialize(data.get(PersistenceUtil.Key.BLOCK_TELEPORTER_LOCATION.getKey(), DataType.STRING));
//        if (blockLocation != null) {
////            MessageConfig.get().getTeleporterInfo().send(player, "location", String.format("%s, %s, %s, %s",
////                    blockLocation.getWorld().getName(),
////                    blockLocation.getX(),
////                    blockLocation.getY(),
////                    blockLocation.getZ()
////            ));
//            return true;
//        }
//
////        MessageConfig.get().getFindAnotherTeleporter().send(event.getPlayer());
//        this.linkingMap.put(event.getPlayer().getUniqueId(), block.getLocation());
//        return true;
//    }
//
//    /**
//     * Links two teleporters together by applying the exit location to the entry teleporter
//     *
//     * @param player The player linking the teleporters
//     * @param entry  The entry teleporter
//     * @param exit   The exit teleporter
//     */
//    private void linkTeleporter(final Player player, final Block entry, final Block exit) {
//
//        // Remove the linking map if the entry and exit are the same
//        if (entry.getLocation().equals(exit.getLocation())) {
////            MessageConfig.get().getCancelledLink().send(player);
//            this.linkingMap.remove(player.getUniqueId());
//            return;
//        }
//
//        CustomBlockData entryData = PersistenceUtil.accessBlockData(entry);
//        CustomBlockData exitData = PersistenceUtil.accessBlockData(exit);
//
//        // Cancel the link if they're not clicking on another teleporter.
//        String blockType = exitData.get(PersistenceUtil.Key.NODE_TYPE, DataType.STRING);
//        if (blockType == null || !blockType.equalsIgnoreCase(BlockType.TELEPORTER.name())) {
////            MessageConfig.get().getCancelledLink().send(player);
//            this.linkingMap.remove(player.getUniqueId());
//            return;
//        }
//
//        // Sets the entry teleporter to exit at the current block
//        entryData.set(PersistenceUtil.Key.BLOCK_TELEPORTER_LOCATION.getKey(), DataType.STRING, serialize(exit.getLocation()));
//
////        MessageConfig.get().getTeleporterLinked().send(player);
//        this.linkingMap.remove(player.getUniqueId());
//    }
//
//
//    /**
//     * Serializes a {@link Location} to a string format
//     *
//     * @param location The {@link Location} to serialize
//     * @return The serialized string
//     */
//    public static String serialize(final Location location) {
//        if (location == null) return ""; // Empty string for null locations
//        return String.format("%s,%s,%s,%s",
//                location.getWorld().getName(),
//                location.getX(),
//                location.getY(),
//                location.getZ()
//        );
//    }
//
//    /**
//     * Deserializes a {@link Location} from a serialized string format
//     *
//     * @param serialized The serialized string
//     * @return The deserialized {@link Location}
//     */
//    public static Location deserialize(final String serialized) {
//        if (serialized == null) return null;
//
//        String[] parts = serialized.split(",");
//        if (parts.length != 4) return null;
//
//        try {
//            String world = parts[0];
//            double x = Double.parseDouble(parts[1]);
//            double y = Double.parseDouble(parts[2]);
//            double z = Double.parseDouble(parts[3]);
//            return new Location(Bukkit.getWorld(world), x, y, z);
//        } catch (NumberFormatException e) {
//            return null;
//        }
//    }

}
