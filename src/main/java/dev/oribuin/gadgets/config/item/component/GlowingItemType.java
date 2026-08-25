package dev.oribuin.gadgets.config.item.component;

import dev.oribuin.gadgets.config.item.ConstructComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.function.Consumer;

import static io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "UnstableApiUsage"})
public final class GlowingItemType extends ConstructComponent<Boolean> {

    public static final Consumer<GlowingItemType> ENABLED = x -> x.setEnabled(true);

    /**
     * Create a new item component type from the plugin
     *
     * @return item component type
     */
    @Override
    public Boolean establish() {
        return this.enabled;
    }

    /**
     * Apply an {@link ConstructComponent} to an ItemStack
     *
     * @param stack The ItemStack to apply to
     */
    @Override
    public void apply(@NotNull ItemStack stack) {
        if (this.enabled) stack.setData(ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    /**
     * Clear an {@link ConstructComponent} from an ItemStack
     *
     * @param stack The ItemStack to apply to
     */
    @Override
    public void clear(@NotNull ItemStack stack) {
        stack.unsetData(ENCHANTMENT_GLINT_OVERRIDE);
    }

}