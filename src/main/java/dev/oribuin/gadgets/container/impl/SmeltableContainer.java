package dev.oribuin.gadgets.container.impl;

// TODO: Redo this later
public final class SmeltableContainer { // implements ContainerWrapper, FueledWrapper, VanillaWrapper

//    private final Block block;
//    private final BlockPosition position;
//    private FurnaceInventory inventory;
//
//    /**
//     * Create a new smeltable container for the plugin
//     *
//     * @param block The block to open
//     */
//    public SmeltableContainer(Block block) {
//        this.block = block;
//        this.position = new BlockPosition(block.getLocation());
//
//        if (this.isValidContainer()) {
//            this.inventory = ((Furnace) block.getState()).getInventory();
//        }
//    }
//
//    /**
//     * Check if a provided {@link Block} is a valid container for the wrapper
//     *
//     * @param block The block provided
//     * @return if the container is valid
//     */
//    public static SmeltableContainer from(Block block) {
//        SmeltableContainer container = new SmeltableContainer(block);
//        return container.isValidContainer() ? container : null;
//    }
//
//    /**
//     * Check if a provided {@link Block} is a valid container for the wrapper
//     *
//     * @return if the container is valid
//     */
//    @Override
//    public boolean isValidContainer() {
//        return block.getState() instanceof Furnace;
//    }
//
//    /**
//     * Get the position the container is located at if available
//     *
//     * @return The returning block position
//     */
//    @Override
//    public @Nullable BlockPosition getPosition() {
//        return this.position;
//    }
//
//    /**
//     * Retrieve a specified amount of {@link ItemStack} from the {@link ContainerWrapper}.
//     * This does not remove the {@link ItemStack} from the {@link ContainerWrapper}, see {@link #deposit(ItemStack)} to remove items
//     *
//     * @param stack The {@link ItemStack} that is being taken from the {@link ContainerWrapper}
//     * @return Whether the plugin was able to retrieve that much of the item
//     */
//    @Override
//    public boolean withdraw(@NotNull ItemStack stack, int amount) {
//        ItemStack result = this.inventory.getResult();
//        if (result == null || !stack.isSimilar(result)) return false;
//
//        int total = InventoryUtils.getStock(this.inventory, stack);
//        if (total < amount) return false;
//
//        int totalStacks = (int) Math.ceil((double) total / stack.getMaxStackSize());
//        List<ItemStack> stacks = new ArrayList<>();
//        for (int i = 0; i < totalStacks; i++) {
//            int size = Math.min(totalStacks, stack.getMaxStackSize());
//            ItemStack withdrawn = stack.asQuantity(size);
//            stacks.add(withdrawn);
//        }
//
//        ItemStack first = stacks.getFirst();
//        this.inventory.setResult(first.asQuantity(first.getMaxStackSize() - first.getAmount()));
//        return true;
//    }
//
//    /**
//     * Deposit an itemstack into the {@link ContainerWrapper}'s Inventory, Adding to the total amount
//     *
//     * @param stack The stack being deposited
//     * @return whether the item was successful in being deposited
//     */
//    @Override
//    public boolean deposit(@NotNull ItemStack stack) {
//        ItemStack smelting = this.inventory.getSmelting();
//        if (smelting == null) {
//            this.inventory.setSmelting(stack);
//            return true;
//        }
//
//        if (smelting.isSimilar(stack) && (smelting.getAmount() + stack.getAmount() <= stack.getMaxStackSize())) {
//            smelting.setAmount(smelting.getAmount() + stack.getAmount());
//            this.inventory.setSmelting(smelting);
//            return true;
//        }
//
//        return false;
//    }
//
//    /**
//     * Get the first {@link ItemStack} inside of a {@link ContainerWrapper}'s Inventory
//     *
//     * @param filtered Any filters on the item that needs to be grabbed
//     * @return The resulting itemstack if available
//     */
//    @Override
//    public @Nullable ItemStack getFirst(@Nullable Filtered filtered) {
//        return this.inventory.getResult();
//    }
//
//    /**
//     * Get all the content inside the contnainer wrapper
//     *
//     * @return The resulting item contennts
//     */
//    @Override
//    public @NotNull Map<ItemStack, Integer> getContent() {
//        Map<ItemStack, Integer> result = new HashMap<>();
//        for (ItemStack stack : this.inventory.getStorageContents()) {
//            if (stack == null || stack.getType() == Material.AIR) continue;
//
//            ItemStack single = stack.asQuantity(1);
//            int current = result.getOrDefault(single, 0);
//            current += single.getAmount();
//            result.put(single, current);
//        }
//
//        return result;
//    }
//
//    /**
//     * Check if the provided container is empty
//     *
//     * @return The container to check
//     */
//    @Override
//    public boolean isEmpty() {
//        return this.inventory.isEmpty();
//    }
//
//    /**
//     * Check if the {@link ContainerWrapper} has capped out on the maximum {@link ItemStack} that it can hold
//     *
//     * @return The result of the check
//     */
//    @Override
//    public boolean isContainerFull() {
//        return this.inventory.firstEmpty() == -1; // todo: better check for this, less optimal though
//    }
//
//    /**
//     * Remove a specific {@link ItemStack} of fuel from the container
//     *
//     * @param stack The stack being added
//     */
//    @Override
//    public void addFuel(ItemStack stack) {
//        ItemStack fuel = this.inventory.getFuel();
//        if (fuel == null) {
//            this.inventory.setFuel(stack);
//            return;
//        }
//
//        if (fuel.isSimilar(stack) && (fuel.getAmount() + stack.getAmount() <= stack.getMaxStackSize())) {
//            fuel.setAmount(fuel.getAmount() + stack.getAmount());
//            this.inventory.setFuel(fuel);
//        }
//    }
//
//    /**
//     * Remove a specific {@link ItemStack} of fuel from the container
//     *
//     * @param stack The stack being removed
//     */
//    @Override
//    public void removeFuel(ItemStack stack) {
//        ItemStack result = this.inventory.getFuel();
//        int amount = stack.getMaxStackSize();
//        if (result == null || !stack.isSimilar(result)) return;
//
//        int total = InventoryUtils.getStock(this.inventory, stack);
//        if (total < amount) return;
//
//        int totalStacks = (int) Math.ceil((double) total / stack.getMaxStackSize());
//        List<ItemStack> stacks = new ArrayList<>();
//        for (int i = 0; i < totalStacks; i++) {
//            int size = Math.min(totalStacks, stack.getMaxStackSize());
//            ItemStack withdrawn = stack.asQuantity(size);
//            stacks.add(withdrawn);
//        }
//
//        ItemStack first = stacks.getFirst();
//        this.inventory.setResult(first.asQuantity(first.getMaxStackSize() - first.getAmount()));
//    }
//
//    /**
//     * Check how much {@link ItemStack} fuel is stored inside the container
//     *
//     * @return The total fuel available
//     */
//    @Override
//    public int getTotalFuel() {
//        ItemStack fuel = this.inventory.getFuel();
//        if (fuel == null || fuel.getType() == Material.AIR) return 0;
//        return fuel.getAmount();
//    }


}