package dev.oribuin.gadgets.util.block;

import org.bukkit.Location;

public final class NodePipePath {

    public static boolean isLoaded(FinePosition position) {
        return isLoaded(position.toLocation());
    }

    public static boolean isLoaded(final Location location) {
        // Loaded chunk checks
        if (!location.isChunkLoaded()) {
            return false;
        }

        // Check if the block is on the edge of a chunk and if the adjacent chunk is loaded
        final int x = location.getBlockX();
        final int z = location.getBlockZ();
        if (x % 16 == 0 || x % 16 == 15 || z % 16 == 0 || z % 16 == 15) {
            int xOffset = (x % 16 == 0) ? -1 : (x % 16 == 15) ? 1 : 0;
            int zOffset = (z % 16 == 0) ? -1 : (z % 16 == 15) ? 1 : 0;

            final Location newLocation = new Location(location.getWorld(), x + xOffset, location.getBlockY(), z + zOffset);
            return newLocation.isChunkLoaded();
        }

        return true;
    }

//    public static Location getRelativeLocation(Location location, SimpleBlockFace face, int range) {
//        int x = location.getBlockX();
//        int y = location.getBlockY();
//        int z = location.getBlockZ();
//
//        switch (face) {
//            case NORTH:
//                z -= range;
//                break;
//            case EAST:
//                x += range;
//                break;
//            case SOUTH:
//                z += range;
//                break;
//            case WEST:
//                x -= range;
//                break;
//            case UP:
//                y += range;
//                break;
//            case DOWN:
//                y -= range;
//                break;
//        }
//
//        return new Location(location.getWorld(), x, y, z);
//    }
}
