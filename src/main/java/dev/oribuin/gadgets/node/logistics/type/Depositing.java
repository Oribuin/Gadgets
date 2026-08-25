package dev.oribuin.gadgets.node.logistics.type;

import dev.oribuin.gadgets.container.transaction.ItemTransferRequest;
import dev.oribuin.gadgets.node.Node;
import dev.oribuin.gadgets.node.logistics.type.valued.Channelled;
import dev.oribuin.gadgets.node.logistics.type.valued.Filtered;

/**
 * Class to mark a {@link Node} as depositing items into a container
 */
public interface Depositing extends Filtered, Channelled {

    /**
     * Deposit an item from a {@link ItemTransferRequest} into a container
     */
    boolean deposit();

}
