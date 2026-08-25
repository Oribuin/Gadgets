package dev.oribuin.gadgets.node.impl.machine;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

public abstract class Machine {

//    @Override
//    public boolean executeOnClick(final BlockHandleHolder event) {
//        Block block = event.getBlock();
//        if (block == null) {
//            return false;
//        }
//
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//        Player player = event.getPlayer();
//        String type = data.get(PersistenceUtil.Key.BLOCK_MACHINE_TYPE.getKey(), DataType.STRING);
//        if (type == null) {
//            return false;
//        }
//
//        UUID identifier = AutoMisc.getMachineIdentifier(data);
//        String blockIdentifier = data.get(PersistenceUtil.Key.BLOCK_IDENTIFIER.getKey(), DataType.STRING);
//        if (data.has(PersistenceUtil.Key.BLOCK_MACHINE_STORAGE.getKey())) {
//            boolean nexo = data.getOrDefault(PersistenceUtil.Key.BLOCK_NEXO.getKey(), DataType.BOOLEAN, false);
//            ItemStack result = nexo ?
//                    BlockType.constructNexo(blockIdentifier) :
//                    BlockType.construct(blockIdentifier);
//
//            if (result == null) {
//                player.sendMessage(Component.text("Please contact an administrator. (" + blockIdentifier + ")"));
//                return true;
//            }
//
//            MachineInventory inventory = (MachineInventory) data.get(PersistenceUtil.Key.BLOCK_MACHINE_STORAGE.getKey(), DataType.CONFIGURATION_SERIALIZABLE);
//            if (inventory != null) inventory.drop(block.getLocation());
//
//            if (player.getGameMode() != GameMode.CREATIVE) {
//                block.getWorld().dropItem(block.getLocation(), result);
//            }
//
//            ProcessExecutor.removeMachinePosition(identifier);
//            NexoFurniture.remove(block.getLocation());
//            data.clear();
//            player.sendMessage(MessageHandler.parse("<yellow><bold>This machine block has been updated, it is intended that it drops, along with its contents. Now it has been updated, this will not happen again!"));
//            return true;
//        }
//
//        ItemStack stack = player.getInventory().getItemInMainHand();
//        if (stack.getType() != Material.AIR) {
//            MachineWrapper wrapper = AutoMisc.wrapperFor(type);
//            if (wrapper != null) {
//                if (FuelMisc.handleFuel(ProcessExecutor.getOrCreate(identifier, block), stack, wrapper, player)) {
//                    return true;
//                }
//            }
//        }
//
//        ProcessProvider process = ProcessExecutor.getProcess(identifier);
//        if (process != null && process.ticking() && process.duration() <= 0) {
////            MessageConfig.get().getMachineFinishingProcess().send(player); // todo message(finishing process)
//            return true;
//        }
//
//        // TODO: Change smart-invs behavior
//        //ProcessExecutor.addMachinePosition(identifier, block.getLocation());
//        //if (OpenedMenuCache.addViewer(block.getLocation(), player)) {
//        //    return true;
//        //}
//
//        if (OpenedMenuCache.isOpenedInventoryAt(block.getLocation())) {
////            MessageConfig.get().getMachineInUse().send(player); // todo message(machine in use)
//            return true;
//        }
//
//        // TODO: Add constructor to open shop GUI
//        switch (type.toLowerCase()) {
//            case "drier" -> DrierGUI.open(player, identifier, block);
//            case "enchanter" -> EnchanterGUI.open(player, identifier, block);
//            case "disenchanter" -> DisenchanterGUI.open(player, identifier, block);
//            case "crusher" -> CrusherGUI.open(player, identifier, block);
//            case "producer" -> ProducerGUI.open(player, identifier, block);
//            case "growth_cell" -> GrowthCellGUI.open(player, identifier, block);
//            case "concrete_factory" -> ConcreteFactoryGUI.open(player, identifier, block);
//            case "constructor" -> ConstructorGUI.open(player, identifier, block);
//            case "refinery" -> RefineryGUI.open(player, identifier, block);
//            case "table_saw" -> TableSawGUI.open(player, identifier, block);
//        }
//
//        // TODO: Remove if-statement when fixed
//        if (!type.equalsIgnoreCase("enchanter") && !type.equalsIgnoreCase("disenchanter")) {
//            OpenedMenuCache.addOpenedInventory(block.getLocation());
//        }
//
//        return true;
//    }
//
//    @Override
//    public boolean executeOnBreak(final BlockHandleHolder event) {
//        Block block = event.getBlock();
//        CustomBlockData data = PersistenceUtil.accessBlockData(block);
//        Location location = block.getLocation();
//
//        MachineInventory.access(data, PersistenceUtil.Key.BLOCK_MACHINE_INPUTS).forEach((slot, stack) -> drop(location, stack));
//        MachineInventory.access(data, PersistenceUtil.Key.BLOCK_MACHINE_OUTPUTS).forEach((slot, stack) -> drop(location, stack));
//        MachineInventory.access(data, PersistenceUtil.Key.BLOCK_MACHINE_FUEL_STACKS).forEach((slot, stack) -> drop(location, stack));
//        MachineInventory.access(data, PersistenceUtil.Key.BLOCK_MACHINE_UPGRADES).forEach((slot, stack) -> drop(location, stack));
//        return false;
//    }
//
//    private void drop(final Location location, final ItemStack stack) {
//        if (stack == null) return;
//
//        location.getWorld().dropItem(location, stack);
//    }

}