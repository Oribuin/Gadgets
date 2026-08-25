package dev.oribuin.gadgets.node.logistics.type.valued;

public interface Priority {

    /**
     * Get the priority order for the node
     *
     * @return The node priority
     */
    int getPriority();

    /**
     * Set a node's priority
     *
     * @param priority The node priority
     */
    void setPriority(int priority);
}
