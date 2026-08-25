package dev.oribuin.gadgets.node.logistics.type;

public interface Tickable {

    void tick();

    default boolean isAsync() {
        return true;
    }

}
