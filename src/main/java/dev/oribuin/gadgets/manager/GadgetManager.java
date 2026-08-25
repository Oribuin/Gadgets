package dev.oribuin.gadgets.manager;

import com.sk89q.worldedit.command.HistoryCommands;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.ConfigLoader;
import dev.oribuin.gadgets.gadgets.Gadget;
import dev.oribuin.gadgets.gadgets.impl.GlassCutter;
import dev.oribuin.gadgets.gadgets.impl.InfusedMagnet;
import dev.oribuin.gadgets.gadgets.impl.LumberAxe;
import dev.oribuin.gadgets.gadgets.impl.MagicalZombiePills;
import dev.oribuin.gadgets.gadgets.impl.MultiTool;
import dev.oribuin.gadgets.gadgets.impl.SwordOfBeheading;
import dev.oribuin.gadgets.gadgets.impl.TapeMeasure;
import dev.oribuin.gadgets.gadgets.impl.VeinMiner;
import dev.oribuin.gadgets.gadgets.impl.WindStaff;
import dev.oribuin.gadgets.gadgets.impl.explosive.ExplosiveHoe;
import dev.oribuin.gadgets.gadgets.impl.explosive.ExplosiveTool;
import dev.oribuin.gadgets.gadgets.impl.explosive.UpgradedExplosiveTool;
import dev.oribuin.gadgets.gadgets.impl.portable.PortableAnvil;
import dev.oribuin.gadgets.gadgets.impl.portable.PortableCrafter;
import dev.oribuin.gadgets.gadgets.impl.portable.PortableDustBin;
import dev.oribuin.gadgets.gadgets.impl.portable.PortableEnderChest;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.GADGET_IDENTIFIER;

public class GadgetManager implements Manager {

    private final Map<Class<?>, Gadget> cachedGadgets = new HashMap<>();
    private final Map<String, Class<? extends Gadget>> cachedIds = new HashMap<>();
    private static final Path GADGETS_PATH = GadgetsPlugin.get().getDataPath().resolve("Gadgets");
    private final GadgetsPlugin plugin;

    public GadgetManager(GadgetsPlugin plugin) {
        this.plugin = plugin;
        this.reload(plugin);
    }

    /**
     * The task that runs when the plugin is loaded/reloaded
     *
     * @param plugin The plugin reloading
     */
    @Override
    public void reload(GadgetsPlugin plugin) {
        cachedGadgets.clear();
        cachedIds.clear();
        register(ExplosiveTool::new);
        register(UpgradedExplosiveTool::new);
        register(ExplosiveHoe::new);
        register(PortableAnvil::new);
        register(PortableCrafter::new);
        register(PortableDustBin::new);
        register(PortableEnderChest::new);
        register(GlassCutter::new);
        register(InfusedMagnet::new);
        register(LumberAxe::new);
        register(MagicalZombiePills::new);
        register(MultiTool::new);
        register(LumberAxe::new);
        register(SwordOfBeheading::new);
        register(TapeMeasure::new);
        register(VeinMiner::new);
        register(WindStaff::new);
    }

    /**
     * The task that runs when the plugin is disabled, usually takes priority over {@link Manager#reload(GadgetsPlugin)}
     *
     * @param plugin The plugin being disabled
     */
    @Override
    public void disable(GadgetsPlugin plugin) {
        this.cachedGadgets.clear();
        this.cachedIds.clear();
    }


    /**
     * Register a new gadget into the plugin
     *
     * @param supplier The gadget to register
     */
    public void register(Supplier<Gadget> supplier) {
        Gadget gadget = supplier.get();

        ConfigLoader loader = this.plugin.getLoader();
        loader.loadConfig(gadget.getClass(), Path.of("gadgets"));
        this.cachedGadgets.put(gadget.getClass(), loader.getClone(gadget.getClass()));
    }

    /**
     * Get the gadget from an id
     *
     * @param id The id to use
     * @return The returning gadget if available
     */
    @Nullable
    public Gadget from(@Nullable String id) {
        if (id == null || id.isEmpty()) return null;

        // check for the cache first <3
        Class<? extends Gadget> cached = this.cachedIds.get(id.toLowerCase());
        if (cached != null) return this.cachedGadgets.get(cached);

        for (Gadget gadget : cachedGadgets.values()) {
            if (!gadget.getItems().containsKey(id.toLowerCase())) continue;

            cachedIds.put(id.toLowerCase(), gadget.getClass());
            return gadget;
        }

        return null;
    }

    /**
     * Get the gadget from an itemstack
     *
     * @param stack The stack to use
     * @return The returning gadget if available
     */
    @Nullable
    public Gadget from(@Nullable ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return null;
        if (!stack.hasItemMeta()) return null;

        String id = stack.getPersistentDataContainer().get(GADGET_IDENTIFIER.key(), GADGET_IDENTIFIER);
        if (id == null) return null;

        return from(id);
    }

    /**
     * Check all the gadget ids and see if any applicable ids clash
     */
    private void checkClashing() {
        for (Gadget gadget : this.cachedGadgets.values()) {
            if (!gadget.isEnabled()) continue; // gadget is disabled, we don't care
            if (gadget.getItems().isEmpty()) continue; // no applicable ids, we don't care
            
            for (Gadget additional : this.cachedGadgets.values()) {
                if (gadget == additional) continue;
                if (!additional.isEnabled()) continue; // gadget is disabled, we double don't care

                for (String additionalId : additional.getItems().keySet()) {
                    if (gadget.getItems().containsKey(additionalId)) {
                        GadgetsPlugin.get().getLogger().warning("Gadget["
                                + gadget.getClass().getSimpleName() + "] has clashing id["
                                + additionalId + "] with Gadget["
                                + additionalId.getClass().getSimpleName() + "] " +
                                "One will always take priority over the other."
                        );
                    }
                }
            }
        }
    }

    public Map<Class<?>, Gadget> getCachedGadgets() {
        return cachedGadgets;
    }

    public Map<String, Class<? extends Gadget>> getCachedIds() {
        return cachedIds;
    }
    
}
