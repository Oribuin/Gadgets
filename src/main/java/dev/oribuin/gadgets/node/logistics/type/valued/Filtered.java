package dev.oribuin.gadgets.node.logistics.type.valued;

import dev.oribuin.gadgets.node.logistics.type.FilterType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface Filtered {

    BiPredicate<Filtered, ItemStack> DOES_MATCH = (filtered, stack) -> {
        boolean isMetaImmune = filtered instanceof MetaImmune;
        Predicate<ItemStack> check = filtered.isIgnoreMeta() && !isMetaImmune
                ? x -> x.getType() == stack.getType()
                : x -> x.getType() == stack.getType() && x.isSimilar(stack);

        return filtered.getFilterContent().stream().anyMatch(x -> {
            if (x == null || x.getType() == Material.AIR) return false;
            return check.test(x); // Item can be found so it matches filter
        });
    };

    /**
     * Get all the filtered items inside the container
     *
     * @return The filtered itemstacks
     */
    Set<ItemStack> getFilterContent();

    /**
     * Set a list of filtered itemstacks
     *
     * @param filterContent The itemstacks that will be filtered
     */
    void setFilterContent(@NotNull Set<ItemStack> filterContent);

    /**
     * Get the type of filter
     *
     * @return The type of filter
     */
    @NotNull
    FilterType getFilterType();

    /**
     * Set a filter type for the filter
     *
     * @param filterType The type of filter being set
     */
    void setFilterType(@NotNull FilterType filterType);

    /**
     * If the filter should ignore meta (just go off type alone. This should not work with barrels at ALL)
     *
     * @return Whether the filter should ignore meta
     */
    boolean isIgnoreMeta();

    /**
     * Set whether the filter should ignore meta (just go off type alone)
     *
     * @param ignoreMeta Whether the item is ignoring meta
     */
    void setIgnoreMeta(boolean ignoreMeta);

    /**
     * Check if the filter accepts an itemstack
     *
     * @param stack The stack to check
     * @return The result of the test
     */
    default boolean testFilter(ItemStack stack) {
        return this.getFilterType().canAccept(this, stack);
    }

    /**
     * Marks a class as meta immune, meaning ignore meta does not work
     */
    interface MetaImmune {
    }
}
