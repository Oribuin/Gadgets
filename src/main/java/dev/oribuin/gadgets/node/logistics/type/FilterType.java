package dev.oribuin.gadgets.node.logistics.type;

import dev.oribuin.gadgets.node.logistics.type.valued.Filtered;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiPredicate;

public enum FilterType {
    BLACKLIST((filtered, stack) -> {
        if (filtered.getFilterContent().isEmpty()) return true;

        return !Filtered.DOES_MATCH.test(filtered, stack);
    }),
    WHITELIST((filtered, stack) -> {
        if (filtered.getFilterContent().isEmpty()) return false;

        return Filtered.DOES_MATCH.test(filtered, stack);
    });

    private final BiPredicate<Filtered, ItemStack> predicate;

    FilterType(BiPredicate<Filtered, ItemStack> predicate) {
        this.predicate = predicate;
    }

    /**
     * Check if a filter can accept a specified type
     *
     * @param filtered The filter
     * @param stack    The stack being passed through the filter
     * @return The result of the check
     */
    public boolean canAccept(Filtered filtered, ItemStack stack) {
        return this.predicate.test(filtered, stack);
    }

}
