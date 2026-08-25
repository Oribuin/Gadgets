package dev.oribuin.gadgets.config.gui;

import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.util.Placeholders;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class GuiIcon {

    private ItemConstruct item;
    private List<Integer> slots;

    public GuiIcon() {
        this.item = new ItemConstruct();
        this.slots = new ArrayList<>();
    }

    public GuiIcon(ItemConstruct construct, Integer... slots) {
        this.item = construct;
        this.slots = new ArrayList<>(List.of(slots));
        this.slots.removeIf(integer -> integer < 0 || integer >= 54);
    }

    @NotNull
    public GuiItem asItem() {
        return this.withAction(Placeholders.empty(), x -> {
            // empty
        });
    }

    @NotNull
    public GuiItem asItem(Placeholders placeholders) {
        return this.withAction(placeholders, x -> {
            // empty
        });
    }
    
    @NotNull
    public GuiItem asItem(Supplier<Placeholders> placeholders) {
        return this.withAction(placeholders.get(), x -> {
            // empty
        });
    }

    @NotNull
    public GuiItem asItem(Object... placeholders) {
        return this.withAction(Placeholders.of(placeholders), x -> {
            // empty
        });
    }

    public GuiItem withAction(GuiAction<InventoryClickEvent> eventConsumer) {
        return this.withAction(Placeholders.empty(), eventConsumer);
    }

    public GuiItem withAction(Placeholders placeholders, GuiAction<InventoryClickEvent> eventConsumer) {
        ItemStack stack = this.item.create(placeholders);
        return new GuiItem(stack != null ? stack : new ItemStack(Material.STONE), eventConsumer);
    }

    public ItemConstruct getItem() {
        return item;
    }

    public List<Integer> getSlots() {
        return slots;
    }

}
