package dev.oribuin.gadgets.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import dev.oribuin.gadgets.gadgets.executor.ExecutorService;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.NodeType;
import dev.oribuin.gadgets.util.PersistenceUtil;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CustomBlockListener implements Listener {

    /**
     * Handle interacting with a node by right-clicking on it
     *
     * @param event The listened to event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block == null || block.getType() == Material.AIR) return;
        if (!event.getAction().isRightClick()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.useInteractedBlock() == Event.Result.DENY) return;

        CustomBlockData data = PersistenceUtil.accessBlockData(block);
        Node node = NodeFactory.from(block, data);
        if (node == null) return;

        if (node.isApplicable(event)) {
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);
            ExecutorService.handleForNode(event, player, node, block);
        }
    }

    /**
     * Handle the placing of a node
     *
     * @param event The listened to event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack stack = event.getItemInHand();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        Node node = NodeFactory.from(event.getBlock(), meta.getPersistentDataContainer());
        if (node == null) return;

        // todo: permission check
        // todo: nexo check blah blah

        node.setBlock(block);
        node.serialize(); // Move the data into the new block

        ExecutorService.handleForNode(event, event.getPlayer(), node, block);
    }

    /**
     * Handle the breaking of a node
     *
     * @param event The listened to event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        CustomBlockData data = PersistenceUtil.accessBlockData(block);
        Node node = NodeFactory.from(block, data);
        if (node == null) return;

        // todo: permission check
        // todo: nexo check blah blah

        event.setDropItems(false);

        ExecutorService.handleForNode(event, player, node, block);

        // Drop the node on the ground
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack stack = node.establishItem(1).clone();
            node.modifyNodeDrop().accept(stack);
            block.getWorld().dropItem(block.getLocation().toCenterLocation(), stack);
        }

        data.clear();
    }

    // todo: Nexo FurniturePlace

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onStartSmelt(FurnaceStartSmeltEvent event) {
        Block block = event.getBlock();
        Node node = NodeFactory.from(block);
        if (node == null) return;

        ExecutorService.handleForNode(event, null, node, block);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPickItem(PlayerPickBlockEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.CREATIVE) return;
        if (player.getInventory().getItem(event.getTargetSlot()) != null) return;

        Node node = NodeFactory.from(block);
        if (node == null) return;

        NodeType<?> type = NodeFactory.from(node.getIdentifier().get());
        ItemStack result = NodeFactory.create(type, 1);
        player.getInventory().addItem(result);
        event.setCancelled(true);
    }
//    private boolean handlePlace(final ItemStack item, final Block block, final Player player, final BlockState replaced) {
//        if (item.getItemMeta() == null) {
//            return false;
//        }
//
//        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
//        ItemSettingConfig config = ItemSettingConfig.get();
//
//        if (item.getType().name().contains("SLAB")) {
//            CustomBlockData data = PersistenceUtil.accessBlockData(block);
//            BlockType type = BlockType.getNullable(data.get(PersistenceUtil.Key.NODE_TYPE, DataType.STRING));
//
//            if (type == BlockType.HOLOGRAM_PROJECTOR) {
//                return townCheck(player, block);
//            }
//        }
//
//        BlockType type = BlockType.getNullable(container.get(PersistenceUtil.Key.NODE_TYPE, DataType.STRING));
//        if (type == null) {
//            // Fixes issue with Nexo items not having the block type set in their persistent data container
//            String nexoId = NexoItems.idFromItem(item);
//            if (nexoId == null) return false;
//            String bType = ItemSettingConfig.get().getBlockType(nexoId);
//            type = BlockType.getNullable(bType);
//            if (type == null) return false;
//            if (bType == null || BlockType.getNullable(bType) == null) return false;
//
//            BlockType.applyCustomMeta(item, bType, nexoId, true);
//        }
//
//        String identifier = container.get(PersistenceUtil.Key.BLOCK_IDENTIFIER.getKey(), DataType.STRING);
//        if (identifier == null) {
//            return false;
//        }
//
//        if (replaced != null) {
//            if (replaced.getType().name().contains("SLAB") && type == BlockType.HOLOGRAM_PROJECTOR) {
//                return true;
//            }
//        }
//
//        boolean nexo = container.getOrDefault(PersistenceUtil.Key.BLOCK_NEXO.getKey(), DataType.BOOLEAN, false);
//
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//        data.set(PersistenceUtil.Key.BLOCK_IDENTIFIER.getKey(), DataType.STRING, identifier);
//        data.set(PersistenceUtil.Key.BLOCK_NEXO.getKey(), DataType.BOOLEAN, nexo);
//
//        config.getSettingsForId(identifier).forEach((id, value) -> {
//            PersistenceUtil.Key key = PersistenceUtil.Key.getKeyForIdentifier(id);
//
//            if (key == PersistenceUtil.Key.BLOCK_MACHINE_STORAGE) return;
//            key.getConsumer().accept(data, key.getKey(), value);
//        });
//
//        Node handle = BlockRegisterable.getHandleForType(type);
//        if (handle == null) {
//            return false;
//        }
//
//        if (type == BlockType.MACHINE) {
//            ProcessExecutor.addMachinePosition(AutoMisc.getMachineIdentifier(data), block.getLocation());
//        }
//
//        if (!townCheck(player, block)) return false;
//
//        return handle.executeOnPlace(new Node.BlockHandleHolder(block, player));
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onBlockPlace(final BlockPlaceEvent event) {
//        if (handlePlace(event.getItemInHand(), event.getBlock(), event.getPlayer(), event.getBlockReplacedState())) {
//            event.setCancelled(true);
//        }
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onNexoFurniturePlace(final NexoFurniturePlaceEvent event) {
//        if (!townCheck(event.getPlayer(), event.getBlock()) || !griefPreventionCheck(event.getPlayer(), event.getBlock()))
//            return;
//
//        if (handlePlace(event.getPlayer().getInventory().getItemInMainHand(), event.getBlock(), event.getPlayer(), null)) {
//            event.setCancelled(true);
//        }
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onNexoBlockPlace(final NexoNoteBlockPlaceEvent event) {
//        if (!townCheck(event.getPlayer(), event.getBlock()) || !griefPreventionCheck(event.getPlayer(), event.getBlock()))
//            return;
//
//        if (handlePlace(event.getPlayer().getInventory().getItemInMainHand(), event.getBlock(), event.getPlayer(), null)) {
//            event.setCancelled(true);
//        }
//    }
//
//    private boolean handleBreak(final Block block, final Player player, final BlockBreakEvent event) {
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//        BlockType type = BlockType.getNullable(data.get(PersistenceUtil.Key.NODE_TYPE, DataType.STRING));
//        if (type == null) {
//            return false;
//        }
//
//        if (!townCheck(event.getPlayer(), event.getBlock())) return false;
//
//        if (OpenedMenuCache.isOpenedInventoryAt(block.getLocation())) {
////            MessageConfig.get().getMachineInUse().send(player); // todo message(in use)
//            return true;
//        }
//
//        Node.BlockHandleHolder holder = new Node.BlockHandleHolder(block, player);
//        ItemStack result = holder.getStack();
//        if (result == null) {
//            return false;
//        }
//
//        if (type == BlockType.MACHINE) {
//            if (ProcessExecutor.getProcess(AutoMisc.getMachineIdentifier(data)) != null) {
////                MessageConfig.get().getMachineProcessing().send(player); // todo message(processing)
//                return true;
//            }
//            ProcessExecutor.removeMachinePosition(AutoMisc.getMachineIdentifier(data));
//        }
//
//        Node handle = BlockRegisterable.getHandleForType(type);
//
//        if (handle != null) {
//            if (handle.executeOnBreak(holder)) {
//                return true;
//            }
//        }
//
//        if (player.getGameMode() != GameMode.CREATIVE) {
//            block.getWorld().dropItem(block.getLocation(), result);
//        }
//
//        event.setDropItems(false);
//        NexoFurniture.remove(block.getLocation());
//        data.clear();
//        return false;
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onBlockBreak(final BlockBreakEvent event) {
//        if (handleBreak(event.getBlock(), event.getPlayer(), event)) {
//            event.setCancelled(true);
//        }
//    }
//
//    @EventHandler
//    public void onBlockExplode(final EntityExplodeEvent event) {
//        List<Block> removables = event.blockList().stream().filter(it -> {
//            CustomBlockData data = PersistenceUtil.accessBlockData(it);
//            BlockType type = BlockType.getNullable(data.get(PersistenceUtil.Key.NODE_TYPE, DataType.STRING));
//
//            return type != null;
//        }).toList();
//
//        removables.forEach(event.blockList()::remove);
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onJump(PlayerJumpEvent event) {
//        Block below = event.getFrom().getBlock().getRelative(BlockFace.DOWN);
//        CustomBlockData data = PersistenceUtil.accessBlockData(below);
//        String type = data.get(PersistenceUtil.Key.NODE_TYPE, DataType.STRING);
//        if (type == null) return;
//
//        if (type.equalsIgnoreCase(BlockType.TELEPORTER.name())) {
//
//            // Make sure the location exists
//            String serialized = data.get(PersistenceUtil.Key.BLOCK_TELEPORTER_LOCATION.getKey(), DataType.STRING);
//            Location location = Teleporter.deserialize(serialized);
//            if (location == null) {
////                MessageConfig.get().getNotLinked().send(event.getPlayer()); // todo message(not linked)
//                return;
//            }
//
//            // Teleporter must be inside a town to be used
//            if (
//                    !PlayerUtil.isTown(below)
//                            || !PlayerUtil.isTown(location.getBlock())
//                            || !PlayerUtil.griefPreventionClaim(event.getPlayer(), below)
//                            || !PlayerUtil.griefPreventionClaim(event.getPlayer(), location.getBlock())
//            ) {
////                MessageConfig.get().getRequiresTown().send(event.getPlayer()); // todo message(requires town)
//                return;
//            }
//
//            // Require item use permission to use teleporter
//            if (!PlayerUtil.townyItemCheck(event.getPlayer(), below)) {
//                return;
//            }
//
//            // Make sure the other block is still actually a teleporter block
//            CustomBlockData exitData = PersistenceUtil.accessBlockData(location.getBlock());
//            String exitType = exitData.get(PersistenceUtil.Key.NODE_TYPE, DataType.STRING);
//            if (exitType == null || !exitType.equalsIgnoreCase(BlockType.TELEPORTER.name())) {
////                MessageConfig.get().getNotLinked().send(event.getPlayer()); // todo message(not linked)
//                return;
//            }
//
//            // Teleport the player to the linked location
//            Location cloned = location.clone().add(0.5, 1, 0.5);
//            cloned.setYaw(event.getPlayer().getLocation().getYaw());
//            cloned.setPitch(event.getPlayer().getLocation().getPitch());
//            event.getPlayer().teleportAsync(cloned);
//        }
//    }

}
