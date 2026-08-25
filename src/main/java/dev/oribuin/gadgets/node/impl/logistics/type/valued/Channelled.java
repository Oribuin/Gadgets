package dev.oribuin.gadgets.node.impl.logistics.type.valued;

public interface Channelled {

    /**
     * Get the channel id for the node
     *
     * @return The node channel id
     */
    int getChannel();

    /**
     * Set a node's priority
     *
     * @param channel The channel id
     */
    void setChannel(int channel);
}
