package dev.oribuin.gadgets.container;

import org.bukkit.inventory.ItemStack;

public interface FueledWrapper {

    /**
     * Remove a specific {@link ItemStack} of fuel from the container
     *
     * @param stack The stack being added
     */
    void addFuel(ItemStack stack);

    /**
     * Remove a specific {@link ItemStack} of fuel from the container
     *
     * @param stack The stack being removed
     */
    void removeFuel(ItemStack stack);

    /**
     * Check how much {@link ItemStack} fuel is stored inside the container
     *
     * @return The total fuel available
     */
    int getTotalFuel();

}
