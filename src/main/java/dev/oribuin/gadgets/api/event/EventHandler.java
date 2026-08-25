package dev.oribuin.gadgets.api.event;

import dev.oribuin.gadgets.gadgets.executor.ContextProvider;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A global handler to parse any related events, used to modify the outcome and add additional functionality
 */
public abstract class EventHandler {

    private transient final Map<Class<? extends Event>, EventWrapper<?, ?>> events = new HashMap<>();

    /**
     * Call an event from the handler's registered events, This will not take priority into account.
     *
     * @param provider The {@link ContextProvider} to call
     * @param <T>      The event type to call
     */
    @SuppressWarnings("unchecked")
    public <T extends Event, Z> void callEvent(ContextProvider<T, Z> provider) {
        EventWrapper<?, ?> wrapper = this.events.get(provider.event().getClass());
        if (wrapper == null) return;

        ((EventWrapper<T, Z>) wrapper).function().accept(provider);
    }

    /**
     * Register a function to be called when an {@link Event} is fired for the specified event with {@link EventPriority#NORMAL} priority
     *
     * @param event    The event to register the function for
     * @param function The function to be called when the event is fired
     * @param <T>      The event type to register
     */
    public <T extends Event, Z> void register(Class<T> event, Consumer<ContextProvider<T, Z>> function) {
        this.register(new EventWrapper<>(event, function, EventPriority.NORMAL));
    }

    /**
     * Register a function to be called when an {@link Event} is fired for the specified event
     *
     * @param event    The event to register the function for
     * @param function The function to be called when the event is fired
     * @param order    The priority of the event, used to determine when it is called relative to other events. Uses the {@link EventPriority} enum and behaves like Bukkit's event priority
     * @param <T>      The event type to register
     */
    public <T extends Event, Z> void register(Class<T> event, Consumer<ContextProvider<T, Z>> function, EventPriority order) {
        this.register(new EventWrapper<>(event, function, order));
    }

    /**
     * Register a function to be called when an {@link Event} is fired for the specified event
     *
     * @param wrapper The event to register the function for
     * @param <T>     The event type to register
     */
    public <T extends Event, Z> void register(EventWrapper<T, Z> wrapper) {
        this.events.put(wrapper.event(), wrapper);
    }

    /**
     * Get all the events that are registered with the handler
     *
     * @return A map of all the events that are registered
     */
    public Map<Class<? extends Event>, EventWrapper<?, ?>> getEvents() {
        return events;
    }

    /**
     * Check if an {@link Event} is applicable to the handler and has a function registered
     *
     * @param event The event to check
     * @return If the event is applicable
     */
    public boolean isApplicable(Event event) {
        return this.events.containsKey(event.getClass());
    }

    /**
     * Get the wrapper for an event
     *
     * @param event The event to get the wrapper for
     * @param <T>   The event type to get the wrapper for
     * @return The wrapper for the event
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> EventWrapper<T, ?> getWrapper(Class<T> event) {
        if (!this.events.containsKey(event)) return null;

        return (EventWrapper<T, ?>) this.events.get(event);
    }

}
