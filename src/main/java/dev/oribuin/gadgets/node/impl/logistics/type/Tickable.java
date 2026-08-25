package dev.oribuin.gadgets.node.impl.logistics.type;

public interface Tickable {

    void tick();

    default boolean isAsync() {
        return true;
    }

}
