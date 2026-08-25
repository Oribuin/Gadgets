package dev.oribuin.gadgets.node.impl.logistics;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class NodeCell { // extends StorageNode {

    // region NodCell - Decided against it
//    /**
//     * Establish a new type of {@link Node} within the plugin
//     *
//     * @param block The block being used to define the node
//     * @param data  The data of the block
//     */
//    public NodeCell(@Nullable Block block, @Nullable CustomBlockData data) {
//        super(block, data);
//
//        this.item = MangoItem.of(this.getIdentifier().get(), Material.HONEYCOMB_BLOCK)
//                .name("<white>[<#93bc80><bold>" + this.getIdentifier().get() + "</bold><white>]")
//                .lore("", " <white>| <gray>Does Something!");
//    }
//
//    /**
//     * Handles any functionality behind a player breaking a block
//     *
//     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
//     */
//    @Override
//    public void handleBlockBreak(ContextProvider<BlockBreakEvent, Block> provider) {
//        super.handleBlockBreak(provider);
//
//        Block block = provider.type();
//
//        Map<Integer, ItemStack> content = this.getValue(NODE_INVENTORY, new HashMap<>());
//        content.forEach((integer, stack) -> block.getWorld().dropItem(block.getLocation(), stack));
//    }
//
//    /**
//     * Handles any functionality behind a player interacting with an item in any capacity
//     *
//     * @param provider The provider for the associated event, giving the {@link Event}, {@link Player} and the utilised {@link ItemStack}
//     */
//    @Override
//    public void handleInteract(ContextProvider<PlayerInteractEvent, Block> provider) {
//        super.handleInteract(provider);
//        PlayerInteractEvent event = provider.event();
//        Block clickedBlock = provider.type();
//
//        // Check if the inventory is open 
//        Location location = clickedBlock.getLocation();
//        if (!OpenedMenuCache.isOpenedInventoryAt(location)) {
//            OpenedMenuCache.addOpenedInventory(block.getLocation());
//        }
//
//        OpenedMenuCache.addViewer(location, event.getPlayer());
//        GenericStorageGUI storageGUI = new GenericStorageGUI(MangoGadgetsPlugin.get(), () ->
//                NodeFactory.from(clickedBlock)
//        );
//        storageGUI.open(event.getPlayer());
//    }
//
//    /**
//     * Get the identifier of a node type
//     *
//     * @return The node type identifier
//     */
//    @Override
//    public Supplier<String> getIdentifier() {
//        return NodeFactory.NODE_CELL::identifier;
//    }
//
//    /**
//     * Load a {@link NodeType.Serializer} from a {@link PersistentDataContainer} into the required type. This method should be static
//     *
//     * @param container The container to load from
//     * @return The resulting type if available
//     */
//    public static NodeCell deserialize(Block block, PersistentDataContainer container) {
//        NodeCell cell = new NodeCell(block, PersistenceUtil.accessBlockData(container));
//        if (container != null) cell.loadFrom(container);
//        return cell;
//    }
    // endregion
}
