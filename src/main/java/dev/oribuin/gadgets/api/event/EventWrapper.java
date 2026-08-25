package dev.oribuin.gadgets.api.event;

import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.function.Consumer;

/**
 * Wrapper for an event to be registered with a function
 *
 * @param event    The event to be registered
 * @param function The function to be called when the event is fired
 * @param order    The priority of the event
 * @param <T>      The event type to be registered with the function
 */
public record EventWrapper<T extends Event, Z>(Class<T> event, Consumer<ContextProvider<T, Z>> function, EventPriority order) {

    /**
     * Call the function that was registered with the event, This will cast the event to the correct type
     *
     * @param event The {@link Event} to call
     */
    public void accept(ContextProvider<T, Z> provider) {
        this.function.accept(provider);
    }

    /**
     * Get the event type that was registered
     *
     * @return The event that was registered
     */
    @Override
    public Class<T> event() {
        return event;
    }

    /**
     * Get the function that was registered
     *
     * @return The function that was registered
     */
    @Override
    public Consumer<ContextProvider<T, Z>> function() {
        return function;
    }

    /**
     * Get the priority of the event
     *
     * @return The priority of the event
     */
    @Override
    public EventPriority order() {
        return order;
    }

}