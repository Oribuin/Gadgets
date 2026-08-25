package dev.oribuin.gadgets.gadgets;

import dev.oribuin.gadgets.api.event.EventHandler;
import dev.oribuin.gadgets.api.event.GadgetEvents;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.util.Placeholders;
import dev.oribuin.gadgets.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.oribuin.gadgets.util.PersistenceUtil.GADGET_IDENTIFIER;

/**
 * Creates a new hand held gadget type for the plugin
 */
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public abstract class Gadget extends EventHandler implements GadgetEvents {

    protected final transient String identifier;
    protected boolean enabled;
    protected List<String> disabledWorlds;
    protected List<String> disabledRegions;
    protected Map<String, ItemConstruct> items;

    public Gadget(String identifier, ItemConstruct stack) {
        this.identifier = identifier;
        this.enabled = true;
        this.disabledWorlds = new ArrayList<>(List.of("blank"));
        this.disabledRegions = new ArrayList<>();
        this.items = new HashMap<>(Map.of(identifier, stack));
    }

    /**
     * Get an itemstack from the gadget plugin
     *
     * @param identifier The identifier of the item
     * @return The itemstack if available
     */
    @Nullable
    public ItemStack getItem(@NotNull String identifier, Placeholders placeholders) {
        ItemConstruct stack = this.items.get(identifier);
        if (stack == null) return null;

        return stack.createCustom(null, placeholders, item -> {
            item.editPersistentDataContainer(container -> container.set(
                    GADGET_IDENTIFIER.key(),
                    GADGET_IDENTIFIER,
                    identifier
            ));
        });
    }

    /**
     * Check if the gadget can be used from the plugin
     *
     * @param player   The player using the gadget
     * @param location The location it's being used at
     * @return Whether the gadget can be used
     */
    public boolean canUse(Player player, Location location) {
        if (!this.enabled) return false;
        if (this.disabledWorlds.contains(location.getWorld().getName())) return false;
        if (!PlayerUtil.checkRegion(location, this)) return false;

        return PlayerUtil.townySwitchCheck(player, location.getBlock());
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public List<String> getDisabledRegions() {
        return disabledRegions;
    }

    public Map<String, ItemConstruct> getItems() {
        return items;
    }
}
