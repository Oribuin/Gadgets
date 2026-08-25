package dev.oribuin.gadgets.container.impl;

// region Unused - Decided against usage
public class CellStorageContainer {

//    private final StorageNode node;
//
//    /**
//     * Create a new cell container for the plugin
//     *
//     * @param node The block to open
//     */
//    public CellStorageContainer(StorageNode node) {
//        this.node = node;
//    }
//
//    /**
//     * Check if a provided {@link Block} is a valid container for the wrapper
//     *
//     * @param block The block provided
//     * @return if the container is valid
//     */
//    @Nullable
//    public static CellStorageContainer from(Block block) {
//        if (block == null || !NodeFactory.isTypeOf(block, NodeFactory.NODE_CELL)) return null;
//
//        return new CellStorageContainer(NodeFactory.from(block));
//    }
//
//    /**
//     * Check if a provided {@link Block} is a valid container for the wrapper
//     *
//     * @return if the container is valid
//     */
//    @Override
//    public boolean isValidContainer() {
//        return this.node != null;
//    }
//
//    /**
//     * Get the position the container is located at if available
//     *
//     * @return The returning block position
//     */
//    @Override
//    public @Nullable FinePosition getPosition() {
//        return this.node.getControllerPosition();
//    }
//
//    /**
//     * Retrieve a specified amount of {@link ItemStack} from the {@link ContainerWrapper}.
//     * This does not remove the {@link ItemStack} from the {@link ContainerWrapper}, see {@link #deposit(ItemStack)} to remove items
//     *
//     * @param stack  The {@link ItemStack} that is being taken from the {@link ContainerWrapper}
//     * @param amount The amount of items being removed
//     * @return The returning stack that was withdrawn
//     */
//    @Override
//    public boolean withdraw(@NotNull ItemStack stack, int amount) {
//        boolean result = false;
//        int current = 0;
//        Map<Integer, ItemStack> content = new HashMap<>(this.node.getValue(NODE_INVENTORY, new HashMap<>()));
//        int size = this.node.getValue(NODE_INVENTORY_ROWS, 0) * 9;
//        for (int i = 0; i < size; i++) {
//            ItemStack itemStack = content.get(i);
//
//            if (current == amount) break;
//            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
//            if (!itemStack.isSimilar(stack)) continue;
//
//            int remaining = amount - current;
//            int toSubtract;
//            if (remaining <= itemStack.getAmount()) {
//                toSubtract = Math.min(remaining, stack.getMaxStackSize());
//                itemStack.subtract(toSubtract);
//                content.put(i, itemStack);
//            } else {
//                toSubtract = itemStack.getAmount();
//                itemStack.setAmount(0);
//                content.remove(i);
//            }
//
//            current += toSubtract;
//            result = current == amount;
//        }
//
//        node.setValue(NODE_INVENTORY, content);
//        node.serialize();
//        return result;
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
//        // proper but also wank system
//        Map<Integer, ItemStack> content = new HashMap<>(this.node.getValue(NODE_INVENTORY, new HashMap<>()));
//        int size = this.node.getValue(NODE_INVENTORY_ROWS, 0) * 9;
//        int left = stack.getAmount();
//
//        boolean success = false;
//        for (int i = 0; i < size; i++) {
//            if (left <= 0) {
//                success = true;
//                break;
//            }
//
//            ItemStack current = content.get(i);
//            if (current == null || current.getType() == Material.AIR) {
//                content.put(i, stack.asQuantity(left));
//                success = true;
//                break;
//            }
//
//            if (current.getAmount() == current.getMaxStackSize()) continue; // check if amount is max size already
//            if (current.getType() != stack.getType()) continue; // Check type before all else
//            if (!current.isSimilar(stack)) continue; // check if items are similar
//
//            // TODO: This system is kind of a chop when it comes to validating whether the whole item was deposited, revisit this asap
//            // TODO: This will likely require a revisit on the return function
//            int combined = stack.getAmount() + current.getAmount();
//            if (combined > stack.getMaxStackSize()) {
//                current.setAmount(stack.getMaxStackSize());
//                left = combined - stack.getMaxStackSize();
//            } else {
//                current.setAmount(combined);
//                left -= current.getAmount();
//            }
//
//            content.put(i, current);
//        }
//
//        node.setValue(NODE_INVENTORY, content);
//        node.serialize();
//        return success;
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
//        return this.getStored().values().stream()
//                .filter(stack -> {
//                    if (stack == null || stack.getType() == Material.AIR) return false;
//                    if (filtered == null) return true;
//                    return filtered.getFilterType().canAccept(filtered, stack);
//                })
//                .map(ItemStack::clone)
//                .findFirst()
//                .orElse(null);
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
//        for (ItemStack stack : this.getStored().values()) {
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
//        return this.getStored().isEmpty();
//    }
//
//    /**
//     * Check if the {@link ContainerWrapper} has capped out on the maximum {@link ItemStack} that it can hold
//     *
//     * @return The result of the check
//     */
//    @Override
//    public boolean isContainerFull() {
//        return this.getStored().size() == this.node.getValue(NODE_INVENTORY_ROWS, 0) * 9;
//    }
//
//    private Map<Integer, ItemStack> getStored() {
//        return new HashMap<>(this.node.getValue(NODE_INVENTORY, new HashMap<>()));
//    }
}
