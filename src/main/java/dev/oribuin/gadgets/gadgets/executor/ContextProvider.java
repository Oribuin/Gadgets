package dev.oribuin.gadgets.gadgets.executor;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Create a context provider for the gadget event
 *
 * @param event  The event that was used for the context
 * @param player The player that set off the event
 * @param type   The type associated
 * @param <T>    The event type
 */
public record ContextProvider<T extends Event, Z>(@NotNull T event, @Nullable Player player, @NotNull Z type) {

}
