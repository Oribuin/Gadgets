package dev.oribuin.gadgets.node.impl;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class ChunkHopper {
//
//    /**
//     * Handles the ticking of the block, picking up the items and depositing them into the {@link HopperGUI}
//     *
//     * @param identifier The identifier of the block
//     * @param position   The position of the block
//     */
//    @Override
//    public void tick(UUID identifier, BlockPosition position) {
//        if (!position.asBukkitLocation().isChunkLoaded()) return;
//        Location location = position.asBukkitLocation();
//
//        if (OpenedMenuCache.isOpenedInventoryAt(location))
//            return;
//
//        for (final Entity entity : location.getChunk().getEntities()) {
//            if (entity.getType() != EntityType.ITEM)
//                continue;
//
//            HopperGUI.deposit((Item) entity, location.getBlock());
//        }
//    }
//
//    @Override
//    public boolean executeOnClick(final BlockHandleHolder event) {
//        Block block = event.getBlock();
//        if (block == null) {
//            return false;
//        }
//
//        if (OpenedMenuCache.isOpenedInventoryAt(block.getLocation())) {
////            MessageConfig.get().getHopperInUse().send(event.getPlayer()); // todo message(hopper in use)
//            return true;
//        }
//
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//        Location location = block.getLocation();
//        UUID identifier = TickableMisc.getHopperIdentifier(data);
//
//        if (identifier != null && !ProcessTicker.isTicking(identifier)) {
//            ProcessTicker.addTickable(this, identifier, new BlockPosition(
//                    location.getWorld().getName(),
//                    location.getBlockX(),
//                    location.getBlockY(),
//                    location.getBlockZ()
//            ));
//        }
//
//        OpenedMenuCache.addOpenedInventory(block.getLocation());
//        HopperGUI.open(event.getPlayer(), block);
//        return true;
//    }
//
//    @Override
//    public boolean executeOnPlace(final BlockHandleHolder event) {
//        Block block = event.getBlock();
//        if (block == null) {
//            return false;
//        }
//
//        if (this.checkChunkForHoppers(block.getChunk())) {
////            MessageConfig.get().getChunkHasHopper().send(event.getPlayer()); // todo message(chunk has hopper)
//            return true;
//        }
//
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//        Location location = block.getLocation();
//        UUID identifier = TickableMisc.getHopperIdentifier(data);
//
//        if (identifier == null) {
//            UUID random = UUID.randomUUID();
//            ProcessTicker.addTickable(this, random, new BlockPosition(
//                    location.getWorld().getName(),
//                    location.getBlockX(),
//                    location.getBlockY(),
//                    location.getBlockZ()
//            ));
//
//            TickableMisc.setIdentifier(data, random);
//            return false;
//        }
//
//        return false;
//    }
//
//    @Override
//    public boolean executeOnBreak(final BlockHandleHolder event) {
//        Block block = event.getBlock();
//        if (block == null) {
//            return false;
//        }
//
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//        Location location = block.getLocation();
//        UUID identifier = TickableMisc.getHopperIdentifier(data);
//        if (identifier == null) {
//            return false;
//        }
//
//        HopperInventory.access(data, PersistenceUtil.Key.BLOCK_HOPPER_INVENTORY).forEach((slot, stack) -> this.drop(location, stack));
//        ProcessTicker.removeTickable(identifier);
//
//        return false;
//    }
//
//    /**
//     * Checks if there is a chunk hopper already in the chunk
//     *
//     * @param chunk The chunk to check
//     * @return If there is a chunk hopper in the chunk
//     */
//    private boolean checkChunkForHoppers(final Chunk chunk) {
//        return ProcessTicker.getChunkHoppers().values().stream().anyMatch(position -> {
//            if (!position.asBukkitLocation().isChunkLoaded()) return false;
//            Chunk hopperChunk = position.asBukkitLocation().getChunk();
//            return hopperChunk.getX() == chunk.getX() && hopperChunk.getZ() == chunk.getZ();
//        });
//    }
//
//    private void drop(final Location location, final ItemStack stack) {
//        if (stack == null) return;
//
//        location.getWorld().dropItem(location, stack);
//    }
//
//    @Override
//    public void tickAsync(UUID identifier, BlockPosition position) {
//        // Unused
//    }

}