package dev.oribuin.gadgets.gui.impl.manual;

public final class HopperGUI  {

//    private static final String ID = "cytitems:chunk_hopper";
//    private final CustomBlockData data;
//    private final Block block;
//
//    public HopperGUI(Block block) {
//        this.block = block;
//        this.data = PersistenceUtil.accessBlockData(block);
//    }
//
//    public static void open(final Player player, final Block block) {
//        CustomBlockData blockData = PersistenceUtil.accessBlockData(block);
//        HopperGuiWrapper config = HopperGuiWrapper.get();
//        String type = TickableMisc.getHopperType(blockData);
//        GuiWrapper wrapper = config.getGuiWrapper(type);
//
//        wrapper.title(wrapper.getTitle());
//        SmartInventory inventory = SmartInventoryBuilder.builder(wrapper)
//                .id(ID)
//                .provider(new HopperGUI(block))
//                .updateFrequency(8)
//                .listener(new InventoryListener<>(InventoryCloseEvent.class, event -> {
//                    save(blockData, event.getInventory());
//                    OpenedMenuCache.removeOpenedInventory(block.getLocation());
//                }))
//                .build();
//
//        SmartInventoryBuilder.open(inventory, player);
//    }
//
//    @Override
//    public void init(Player player, InventoryContents inventoryContents) {
//        HopperGuiWrapper config = HopperGuiWrapper.get();
//        GuiWrapper wrapper = config.getGuiWrapper(TickableMisc.getHopperType(data));
//
//        inventoryContents.setProperty("allowShift", true);
//
//        for (int slot = 0; slot < wrapper.getRows() * 9; slot++) {
//            inventoryContents.setEditable(SlotUtil.toSlot(slot), true);
//        }
//
//        this.update(player, inventoryContents);
//        this.createGui(wrapper, player, inventoryContents);
//    }
//
//    @Override
//    public void createGui(IGuiWrapper guiWrapper, Player player, InventoryContents inventoryContents) {
//        Map<Integer, ItemStack> contents = HopperInventory.access(data, PersistenceUtil.Key.BLOCK_HOPPER_INVENTORY);
//
//        for (final Map.Entry<Integer, ItemStack> entry : contents.entrySet()) {
//            int slot = entry.getKey();
//            ItemStack itemStack = entry.getValue().clone();
//
//            if (itemStack.getType().isAir() || itemStack.getAmount() <= 0) {
//                return;
//            }
//
//            inventoryContents.set(slot, ClickableItem.empty(itemStack));
//        }
//    }
//
//    /**
//     * Save an inventory to the hopper
//     *
//     * @param data      The block data
//     * @param inventory The inventory to save
//     */
//    private static void save(CustomBlockData data, Inventory inventory) {
//        if (inventory == null)
//            return;
//
//        Map<Integer, ItemStack> newContent = new HashMap<>();
//        for (int i = 0; i < inventory.getSize(); i++) {
//            ItemStack itemStack = inventory.getItem(i);
//            if (itemStack == null || itemStack.getType() == Material.AIR)
//                continue;
//
//            newContent.put(i, itemStack);
//        }
//
//        HopperInventory.store(data, newContent);
//    }
//
//
//    /**
//     * Deposit items into the hopper
//     *
//     * @param item  The item to deposit
//     * @param block The block to deposit the item into
//     */
//    public static void deposit(Item item, Block block) {
//        CustomBlockData blockData = PersistenceUtil.accessBlockData(block);
//        ItemStack copy = item.getItemStack().clone();
//        int copyAmount = copy.getAmount();
//        if (copyAmount <= 0) return;
//
//        // Add item to hopper
//        Debugger.log("hopper_deposit", "Adding " + copy.getAmount() + " " + copy.getType().name() + " to hopper");
//
//        Map<Integer, ItemStack> contents = new HashMap<>(HopperInventory.access(blockData, PersistenceUtil.Key.BLOCK_HOPPER_INVENTORY));
//
//        // Add item into map at the first available slot
//        for (int slot = 0; slot < slots(blockData); slot++) {
//
//            ItemStack slotItem = contents.get(slot);
//
//            // Add the item into the empty slot
//            if (slotItem == null || slotItem.getType() == Material.AIR) {
//                contents.put(slot, copy);
//                item.remove();
//                break;
//            }
//
//            // Add the item into the slot if it is similar
//            if (slotItem.isSimilar(copy)) {
//                int amount = Math.min(slotItem.getMaxStackSize() - slotItem.getAmount(), copy.getAmount());
//                if (amount < 0) return;
//
//                slotItem.setAmount(slotItem.getAmount() + amount);
//                if (copy.getAmount() - amount <= 0) {
//                    item.remove();
//                }
//
//                copyAmount -= amount;
//                copy.setAmount(copyAmount);
//
//                contents.put(slot, slotItem);
//                item.setItemStack(copy);
//            }
//        }
//
//        HopperInventory.store(blockData, contents);
//    }
//
//    /**
//     * Deposit items into the hopper (Used by Logistics)
//     *
//     * @param item  The item to deposit
//     * @param block The block to deposit the item into
//     */
//    @SuppressWarnings("unused")
//    public static void deposit(ItemStack item, Block block) {
//        CustomBlockData blockData = PersistenceUtil.accessBlockData(block);
//        ItemStack copy = item.clone();
//        int copyAmount = copy.getAmount();
//        if (copyAmount <= 0) return;
//
//        // Add item to hopper
//        Debugger.log("hopper_deposit", "Adding " + copy.getAmount() + " " + copy.getType().name() + " to hopper");
//
//        Map<Integer, ItemStack> contents = new HashMap<>(HopperInventory.access(blockData, PersistenceUtil.Key.BLOCK_HOPPER_INVENTORY));
//
//        // Add item into map at the first available slot
//        for (int slot = 0; slot < slots(blockData); slot++) {
//
//            ItemStack slotItem = contents.get(slot);
//
//            // Add the item into the empty slot
//            if (slotItem == null || slotItem.getType() == Material.AIR) {
//                contents.put(slot, copy);
//                item.setAmount(0);
//                break;
//            }
//
//            // Add the item into the slot if it is similar
//            if (slotItem.isSimilar(copy)) {
//                int amount = Math.min(slotItem.getMaxStackSize() - slotItem.getAmount(), copy.getAmount());
//                if (amount < 0) return;
//
//                slotItem.setAmount(slotItem.getAmount() + amount);
//                if (copy.getAmount() - amount <= 0) {
//                    item.setAmount(0);
//                }
//
//                copyAmount -= amount;
//                copy.setAmount(copyAmount);
//
//                contents.put(slot, slotItem);
//                item = copy;
//            }
//
//        }
//
//        HopperInventory.store(blockData, contents);
//    }
//
//    /**
//     * Withdraw items from the hopper (Used by Logistics)
//     *
//     * @param item  The item to withdraw
//     * @param block The block to withdraw the item from
//     */
//    @SuppressWarnings("unused")
//    public static void withdraw(ItemStack item, Block block) {
//        CustomBlockData blockData = PersistenceUtil.accessBlockData(block);
//
//        // Add item to hopper
//        Debugger.log("hopper_withdraw", "Taking " + item.getAmount() + " " + item.getType().name() + " from the hopper");
//
//        Map<Integer, ItemStack> contents = HopperInventory.access(blockData, PersistenceUtil.Key.BLOCK_HOPPER_INVENTORY);
//
//        if (contents.isEmpty() || contents.values().stream().noneMatch(stack -> stack.isSimilar(item)))
//            return;
//
//        for (final Map.Entry<Integer, ItemStack> entry : contents.entrySet()) {
//            Integer slot = entry.getKey();
//            ItemStack stack = entry.getValue();
//
//            if (stack == null || !stack.isSimilar(item))
//                continue;
//
//            int amount = Math.min(stack.getAmount(), item.getAmount());
//            if (amount <= 0)
//                continue;
//
//            stack.setAmount(stack.getAmount() - amount);
//            item.setAmount(item.getAmount() - amount);
//
//            if (stack.getAmount() <= 0) {
//                contents.remove(slot);
//                break;
//            } else {
//                contents.put(slot, stack);
//            }
//        }
//
//        HopperInventory.store(blockData, contents);
//    }
//
//    /**
//     * Get the contents of the hopper
//     *
//     * @return The contents of the hopper
//     */
//    public Map<Integer, ItemStack> getContents() {
//        return HopperInventory.access(this.data, PersistenceUtil.Key.BLOCK_HOPPER_INVENTORY);
//    }
//
//    /**
//     * Get the slots of the hopper
//     *
//     * @param data The block data
//     * @return The slots of the hopper
//     */
//    private static int slots(CustomBlockData data) {
//        HopperGuiWrapper config = HopperGuiWrapper.get();
//        GuiWrapper wrapper = config.getGuiWrapper(TickableMisc.getHopperType(data));
//
//        return wrapper.getRows() * 9;
//    }
//
//    /**
//     * Check if the hopper can fit the item (Used by Logistics)
//     *
//     * @param itemStack The item to check
//     * @return If the hopper can fit the item
//     */
//    @SuppressWarnings("unused")
//    public static boolean canFitItem(ItemStack itemStack, Map<Integer, ItemStack> contents) {
//        for (Map.Entry<Integer, ItemStack> entry : contents.entrySet()) {
//            ItemStack stack = entry.getValue();
//            if (stack == null || stack.getType() == Material.AIR)
//                return true;
//
//            if (stack.isSimilar(itemStack) && stack.getAmount() < stack.getMaxStackSize())
//                return true;
//        }
//
//        return false;
//    }

}
