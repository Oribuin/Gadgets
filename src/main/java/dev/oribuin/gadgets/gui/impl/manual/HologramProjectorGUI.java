package dev.oribuin.gadgets.gui.impl.manual;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.TextMessage;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.config.gui.GuiIcon;
import dev.oribuin.gadgets.config.item.ConstructType;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.config.item.component.TooltipItemType;
import dev.oribuin.gadgets.gui.api.PluginMenu;
import dev.oribuin.gadgets.node.NodeFactory;
import dev.oribuin.gadgets.node.impl.HologramProjector;
import dev.oribuin.gadgets.node.storage.Hologram;
import dev.oribuin.gadgets.scheduler.PluginScheduler;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.oribuin.gadgets.util.Placeholders;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_TEXT;

public final class HologramProjectorGUI extends PluginMenu<HologramProjectorGUI.HologramProjectorGuiConfig, Gui> {

    private static final Cache<UUID, Block> pendingHologram = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private final Supplier<HologramProjector> projectorSupplier;

    static {
        Bukkit.getPluginManager().registerEvents(
                new HologramTextListener(GadgetsPlugin.get()),
                GadgetsPlugin.get()
        );
    }

    /**
     * Creates a new menu for the plugin to use
     *
     * @param plugin The plugin instance
     */
    public HologramProjectorGUI(GadgetsPlugin plugin, Supplier<HologramProjector> projectorSupplier) {
        super(plugin, HologramProjectorGuiConfig.class);
        this.projectorSupplier = projectorSupplier;
        this.gui = this.createMenu().get();

        // Set GUI Icons & Functionality
        this.setDummyIcons();

        Placeholders placeholders = projectorSupplier.get().getPlaceholders().get();

        // region Change Hologram Text
        GuiIcon textChange = this.getConfig().getTextChange();
        gui.setItem(textChange.getSlots(), textChange.withAction(placeholders, event -> {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            HologramProjector projector = this.projectorSupplier.get();
            Block block = projector.getBlock();
            if (block == null) {
                player.closeInventory();
                return;
            }

            pendingHologram.put(player.getUniqueId(), block);
            this.getConfig().getChangeText().send(player);
            player.closeInventory();
        }));
        // endregion
        // region Change Text Shadow
        GuiIcon textShadow = this.getConfig().getTextShadow();
        gui.setItem(textShadow.getSlots(), textShadow.withAction(placeholders, event -> {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            HologramProjector projector = this.projectorSupplier.get();
            Block block = projector.getBlock();
            if (block == null) {
                player.closeInventory();
                return;
            }

            Hologram hologram = projector.getHologram();
            hologram.setTextShadow(!hologram.getTextShadow());
            projector.update(hologram);
            player.closeInventory();
        }));
        // endregion
        // region Change Text Background
        GuiIcon textBackground = this.getConfig().getTextBackground();
        gui.setItem(textBackground.getSlots(), textBackground.withAction(placeholders, event -> {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            HologramProjector projector = this.projectorSupplier.get();
            Block block = projector.getBlock();
            if (block == null) {
                player.closeInventory();
                return;
            }

            Hologram hologram = projector.getHologram();
            hologram.setBackground(!hologram.getBackground());
            projector.update(hologram);
            player.closeInventory();
        }));
        // endregion
        // region Change Text Billboard
        GuiIcon textBillboard = this.getConfig().getTextBillboard();
        gui.setItem(textBillboard.getSlots(), textBillboard.withAction(placeholders, event -> {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            HologramProjector projector = this.projectorSupplier.get();
            Block block = projector.getBlock();
            if (block == null) {
                player.closeInventory();
                return;
            }

            Hologram hologram = projector.getHologram();
            Display.Billboard current = hologram.getBillboard();
            Display.Billboard[] values = Display.Billboard.values();
            int nextOrdinal = (current.ordinal() + 1) % values.length;
            Display.Billboard next = values[nextOrdinal];
            hologram.setBillboard(next);
            projector.update(hologram);
            player.closeInventory();
        }));
        // endregion
        // region Change Text Scale
        GuiIcon textScale = this.getConfig().getTextScale();
        gui.setItem(textScale.getSlots(), textScale.withAction(placeholders, event -> {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            HologramProjector projector = this.projectorSupplier.get();
            Block block = projector.getBlock();
            if (block == null) {
                player.closeInventory();
                return;
            }

            Hologram hologram = projector.getHologram();
            float scale = hologram.getScale();

            if (event.isLeftClick()) scale = Math.min(scale + 0.1f, 2.0f);
            if (event.isRightClick()) scale = Math.max(scale - 0.1f, 0.5f);
            hologram.setScale(scale);
            projector.update(hologram);

            new HologramProjectorGUI(plugin, this.projectorSupplier).open(player);
        }));
        // endregion
        // region Change Text Rotation
        GuiIcon textRotation = this.getConfig().getTextRotation();
        gui.setItem(textRotation.getSlots(), textRotation.withAction(placeholders, event -> {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            HologramProjector projector = this.projectorSupplier.get();
            Block block = projector.getBlock();
            if (block == null) {
                player.closeInventory();
                return;
            }

            Hologram hologram = projector.getHologram();
            Hologram.Rotation current = hologram.getRotation();
            Hologram.Rotation[] values = Hologram.Rotation.values();
            int nextOrdinal = (current.ordinal() + 1) % values.length;
            Hologram.Rotation next = values[nextOrdinal];
            hologram.setRotation(next);
            projector.update(hologram);
            player.closeInventory();
        }));
        // endregion
        // region place reposition type
        this.reposition(this.getConfig().getUpPositionX(), placeholders, RepositionType.X, 0.1, 0.5);
        this.reposition(this.getConfig().getUpPositionY(), placeholders, RepositionType.Y, 0.1, 0.5);
        this.reposition(this.getConfig().getUpPositionZ(), placeholders, RepositionType.Z, 0.1, 0.5);
        // negative values
        this.reposition(this.getConfig().getDownPositionX(), placeholders, RepositionType.X, -0.1, -0.5);
        this.reposition(this.getConfig().getDownPositionY(), placeholders, RepositionType.Y, -0.1, -0.5);
        this.reposition(this.getConfig().getDownPositionZ(), placeholders, RepositionType.Z, -0.1, -0.5);
        // endregion
    }

    /**
     * Place an item for the repositioning of the hologram
     *
     * @param icon           The icon to place
     * @param placeholders   The placeholders for the icon
     * @param repositionType Whether moving on the x/y/z axis
     * @param leftAmount     The amount to increase/decrease if left-clicked
     * @param rightAmount    The amount to increase/decrease if right-clicked
     */
    private void reposition(GuiIcon icon, Placeholders placeholders, RepositionType repositionType, double leftAmount, double rightAmount) {
        gui.setItem(icon.getSlots(), icon.withAction(placeholders, event -> {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            HologramProjector projector = this.projectorSupplier.get();
            Block block = projector.getBlock();
            if (block == null) {
                player.closeInventory();
                return;
            }

            double amount = event.isLeftClick() ? leftAmount : rightAmount;
            double x = repositionType == RepositionType.X ? amount : 0;
            double y = repositionType == RepositionType.Y ? amount : 0;
            double z = repositionType == RepositionType.Z ? amount : 0;
            Hologram hologram = projector.getHologram();

            Location clone = hologram.getLocation().clone();
            clone.add(x, y, z);

            // don't let the hologram move too far
            if (clone.distance(projector.getBlock().getLocation()) >= 3) {
                this.getConfig().getMaxDistance().send(player);
                player.closeInventory();
                return;
            }

            hologram.setLocation(clone);
            projector.update(hologram);

            new HologramProjectorGUI(plugin, this.projectorSupplier).open(player);
        }));
    }

    private enum RepositionType {
        X, Y, Z
    }

    public static class HologramTextListener implements Listener {

        private final GadgetsPlugin plugin;

        public HologramTextListener(GadgetsPlugin plugin) {
            this.plugin = plugin;
        }

        /**
         * Listen for chat events for holograms being read
         *
         * @param event The chat event
         */
        @SuppressWarnings("deprecation")
        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event) {
            Block awaitedProjector = pendingHologram.getIfPresent(event.getPlayer().getUniqueId());
            if (awaitedProjector == null) return;

            // Cancel the event
            event.setCancelled(true);

            removePending(event.getPlayer().getUniqueId());
            if (!(NodeFactory.from(awaitedProjector) instanceof HologramProjector projector)) return;

            // Check if the message is "cancel"
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                this.getConfig().getCancelledText().send(event.getPlayer());
                return;
            }

            Hologram hologram = projector.getHologram();
            PluginScheduler.get().runTaskAtLocation(hologram.getLocation(), () -> {
                hologram.setText(event.getMessage());
                projector.setValue(HOLOGRAM_TEXT, hologram.getText());
                projector.update(hologram);
            });
            this.getConfig().getChangedText().send(event.getPlayer(), "text", event.getMessage());
        }

        public HologramProjectorGuiConfig getConfig() {
            return this.plugin.getLoader().get(HologramProjectorGuiConfig.class);
        }
    }

    /**
     * Creates the menu for the plugin
     *
     * @return the resulting menu
     */
    @Override
    public Supplier<Gui> createMenu() {
        return () -> Gui.gui()
                .title(MessageHandler.parse(this.getConfig().getTitle()))
                .rows(this.getConfig().getRows())
                .disableAllInteractions()
                .create();
    }

    public static void removePending(UUID uuid) {
        pendingHologram.invalidate(uuid);
    }

    @ConfigSerializable
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class HologramProjectorGuiConfig extends GuiConfig {

        public HologramProjectorGuiConfig() {
            this.title = "Hologram Projector";
            this.rows = 5;

            this.dummyItems.add(ItemConstruct.of(Material.BLACK_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(0, 1, 2, 3, 4, 5, 6, 7, 8,
                            9, 17,
                            18, 26,
                            27, 35,
                            36, 37, 38, 39, 40, 41, 42, 43, 44
                    ));
        }

        private TextMessage cancelledText = new TextMessage("<#93bc80><b>Hologram</b> <dark_gray>▎ <white>You have cancelled setting the hologram text");
        private TextMessage changedText = new TextMessage("<#93bc80><b>Hologram</b> <dark_gray>▎ <white>You have changed the hologram text: <#93bc80><text>").papi(false);
        private TextMessage changeText = new TextMessage("<#93bc80><b>Hologram</b> <dark_gray>▎ <white>Enter your desired hologram text in the chat");
        private TextMessage maxDistance = new TextMessage("<#93bc80><b>Hologram</b> <dark_gray>▎ <white>The hologram cannot move further than this");

        private GuiIcon textChange = ItemConstruct.of(Material.NAME_TAG)
                .setName("<#93bc80><b>Change Text")
                .setLore(
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to change the hologram text"
                )
                .asMenuItem(10);

        private GuiIcon textRotation = ItemConstruct.of(Material.COMPASS)
                .setName("<#93bc80><b>Text Rotation")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Current: <#93bc80><rotation>",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to rotate the hologram"
                )
                .asMenuItem(11);

        private GuiIcon textShadow = ItemConstruct.of(Material.TINTED_GLASS)
                .setName("<#93bc80><b>Text Shadow")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Current: <#93bc80><shadow>",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to toggle the text shadow"
                )
                .asMenuItem(19);

        private GuiIcon textBackground = ItemConstruct.of(Material.BLACK_DYE)
                .setName("<#93bc80><b>Background")
                .setLore(
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to toggle the background"
                )
                .asMenuItem(20);

        private GuiIcon textScale = ItemConstruct.of(Material.SLIME_BALL)
                .setName("<#93bc80><b>Scale")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Current: <#93bc80><scale>",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> increase the scale",
                        "<white>⏩ <#93bc80>Right click<white> decrease the scale"
                )
                .asMenuItem(28);

        private GuiIcon textBillboard = ItemConstruct.of(Material.OAK_HANGING_SIGN)
                .setName("<#93bc80><b>Billboard")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Current: <#93bc80><billboard>",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> change the billboard"
                )
                .asMenuItem(29);

        // region Position Stuff
        private GuiIcon upPositionX = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<white>⬆ <#93bc80><b>Position X</b> <white>⬆")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Moves the hologram +0.1 on the X Axis",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to move by +0.1",
                        "<white>⏩ <#93bc80>Right click<white> to move by +0.5"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("base64-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQ5Mjg3NjE2MzQzZDgzM2U5ZTczMTcxNTljYWEyY2IzZTU5NzQ1MTEzOTYyYzEzNzkwNTJjZTQ3ODg4NGZhIn19fQ=="))
                .asMenuItem(14);

        private GuiIcon downPositionX = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<white>⬇ <#93bc80><b>Position X</b> <white>⬇")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Moves the hologram",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to move by -0.1",
                        "<white>⏩ <#93bc80>Right click<white> to move by -0.5"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("base64-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM4NTJiZjYxNmYzMWVkNjdjMzdkZTRiMGJhYTJjNWY4ZDhmY2E4MmU3MmRiY2FmY2JhNjY5NTZhODFjNCJ9fX0="))
                .asMenuItem(32);

        private GuiIcon upPositionY = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<white>⬆ <#93bc80><b>Position Y</b> <white>⬆")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Moves the hologram",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to move by +0.1",
                        "<white>⏩ <#93bc80>Right click<white> to move by +0.5"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("base64-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRhMDI3NDc3MTk3YzZmZDdhZDMzMDE0NTQ2ZGUzOTJiNGE1MWM2MzRlYTY4YzhiN2JjYzAxMzFjODNlM2YifX19"))
                .asMenuItem(15);

        private GuiIcon downPositionY = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<white>⬇ <#93bc80><b>Position Y</b> <white>⬇")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Moves the hologram",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to move by -0.1",
                        "<white>⏩ <#93bc80>Right click<white> to move by -0.5"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("base64-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY3NDE2Y2U5ZTgyNmU0ODk5YjI4NGJiMGFiOTQ4NDNhOGY3NTg2ZTUyYjcxZmMzMTI1ZTAyODZmOTI2YSJ9fX0="))
                .asMenuItem(33);

        private GuiIcon upPositionZ = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<white>⬆ <#93bc80><b>Position Z</b> <white>⬆")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Moves the hologram",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to move by +0.1",
                        "<white>⏩ <#93bc80>Right click<white> to move by +0.5"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("base64-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJmYzIzODY2NTIzY2FhYThhOTUzNDU2NjEyN2E2ZjgzODlhZjNlNzZiOGUzYzMzYzI0NzNjYmE2ODg5YzQifX19"))
                .asMenuItem(16);

        private GuiIcon downPositionZ = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<white>⬇ <#93bc80><b>Position Z</b> <white>⬇")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Moves the hologram",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> to move by -0.1",
                        "<white>⏩ <#93bc80>Right click<white> to move by -0.5"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("base64-eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjBkMWRmODA0NmYwYjVkOTM0YzNlMDU3OThlYWNmZWVhNmQ3YjU5NWRiZTI2ZGViZjdkYjlhY2M4YzRmYTc5OCJ9fX0="))
                .asMenuItem(34);
        // endregion

        public TextMessage getCancelledText() {
            return cancelledText;
        }

        public TextMessage getChangedText() {
            return changedText;
        }

        public GuiIcon getTextChange() {
            return textChange;
        }

        public TextMessage getChangeText() {
            return changeText;
        }

        public TextMessage getMaxDistance() {
            return maxDistance;
        }

        public GuiIcon getTextShadow() {
            return textShadow;
        }

        public GuiIcon getTextBackground() {
            return textBackground;
        }

        public GuiIcon getTextBillboard() {
            return textBillboard;
        }

        public GuiIcon getTextScale() {
            return textScale;
        }

        public GuiIcon getTextRotation() {
            return textRotation;
        }

        public GuiIcon getUpPositionX() {
            return upPositionX;
        }

        public GuiIcon getDownPositionX() {
            return downPositionX;
        }

        public GuiIcon getUpPositionY() {
            return upPositionY;
        }

        public GuiIcon getDownPositionY() {
            return downPositionY;
        }

        public GuiIcon getUpPositionZ() {
            return upPositionZ;
        }

        public GuiIcon getDownPositionZ() {
            return downPositionZ;
        }
    }
//
//    private static final Pattern PLACEHOLDER_API = Pattern.compile("%(\\w+)%");
//    private static final String ID = "cytitems:hologram_projector";
//    private final ConversationFactory conversationFactory = new ConversationFactory(MangoItemsPlugin.get());
//
//    private final CustomBlockData data;
//
//    private final Map<ClickableItem, List<Integer>> guiItems = new HashMap<>();
//
//    public HologramProjectorGUI(Player player, Block block) {
//        this.data = PersistenceUtil.accessBlockData(block);
//        conversationFactory.withFirstPrompt(new TextPrompt(this.data));
//
//        HologramProjectorWrapper config = HologramProjectorWrapper.get();
//        config.getItems().forEach((id, item) ->
//                this.guiItems.put(ClickableItem.from(item.getItem(
//                        "offset", this.data.getOrDefault(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_Y.getKey(), DataType.DOUBLE, 0D),
//                        "offset_x", this.data.getOrDefault(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_X.getKey(), DataType.DOUBLE, 0D),
//                        "offset_z", this.data.getOrDefault(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_Z.getKey(), DataType.DOUBLE, 0D),
//                        "text", this.data.getOrDefault(PersistenceUtil.Key.BLOCK_HOLOGRAM_TEXT.getKey(), DataType.STRING, "Click the projector to set the text")
//                ), click -> {
//                    InventoryClickEvent event = (InventoryClickEvent) click.getEvent();
//                    InventoryAction action = event.getAction();
//
//                    switch (item.getAction()) {
//                        case "offset_x" -> {
//                            this.offsetHologram(action == InventoryAction.PICKUP_ALL ? 0.1 : -0.1, 0, 0);
//                            HologramProjectorGUI.open(player, block);
//                        }
//                        case "offset" -> {
//                            this.offsetHologram(0, action == InventoryAction.PICKUP_ALL ? 0.1 : -0.1, 0);
//                            HologramProjectorGUI.open(player, block);
//                        }
//                        case "offset_z" -> {
//                            this.offsetHologram(0, 0, action == InventoryAction.PICKUP_ALL ? 0.1 : -0.1);
//                            HologramProjectorGUI.open(player, block);
//                        }
//                        case "text" -> {
//                            player.getOpenInventory().close();
//                            conversationFactory.buildConversation(player).begin();
//                        }
//                        case "toggle_display" -> toggleDisplayName();
//                    }
//                }), item.getSlots())
//        );
//    }
//
//    public static void open(final Player player, final Block block) {
//        GuiWrapper wrapper = HologramProjectorWrapper.get().getGuiWrapper();
//        SmartInventory inv = SmartInventoryBuilder.builder(wrapper)
//                .id(ID)
//                .provider(new HologramProjectorGUI(player, block))
//                .build();
//        SmartInventoryBuilder.open(inv, player);
//    }
//
//    @Override
//    public void init(final Player player, final InventoryContents inventoryContents) {
//        GuiWrapper wrapper = HologramProjectorWrapper.get().getGuiWrapper();
//        createGui(
//                wrapper,
//                player,
//                inventoryContents
//        );
//
//        this.guiItems.forEach((item, slots) -> slots.forEach(slot -> inventoryContents.set(slot, item)));
//    }
//
//    @Override
//    public void update(final Player player, final InventoryContents inventoryContents) {
//    }
//
//    private void toggleDisplayName() {
//        Hologram hologram = getHologram();
//        if (hologram == null) {
//            return;
//        }
//
//        if (hologram.isDisabled()) hologram.enable();
//        else hologram.disable();
//
//        HologramPersistence.saveToFile(hologram);
//    }
//
//    private void offsetHologram(final double x, final double y, final double z) {
//        Double offsetX = this.data.get(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_X.getKey(), DataType.DOUBLE);
//        Double offsetY = this.data.get(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_Y.getKey(), DataType.DOUBLE);
//        Double offsetZ = this.data.get(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_Z.getKey(), DataType.DOUBLE);
//
//        if (offsetY == null) return;
//        if (offsetX == null) offsetX = 0.0;
//        if (offsetZ == null) offsetZ = 0.0;
//
//        Hologram hologram = getHologram();
//        if (hologram == null) {
//            return;
//        }
//
//        DHAPI.moveHologram(hologram.getName(), hologram.getLocation().add(x, y, z));
//        this.data.set(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_X.getKey(), DataType.DOUBLE, MiscUtil.round(offsetX + x, 1));
//        this.data.set(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_Y.getKey(), DataType.DOUBLE, MiscUtil.round(offsetY + y, 1));
//        this.data.set(PersistenceUtil.Key.BLOCK_HOLOGRAM_OFFSET_Z.getKey(), DataType.DOUBLE, MiscUtil.round(offsetZ + z, 1));
//        HologramPersistence.saveToFile(hologram);
//    }
//
//    private Hologram getHologram() {
//        UUID identifier = data.get(PersistenceUtil.Key.BLOCK_HOLOGRAM_ENTITY_UUID.getKey(), DataType.UUID);
//        if (identifier == null) {
//            return null;
//        }
//
//        return DHAPI.getHologram(identifier.toString());
//    }
//
//    private static class TextPrompt extends StringPrompt {
//
//        private final CustomBlockData data;
//
//        TextPrompt(final CustomBlockData data) {
//            this.data = data;
//        }
//
//        /**
//         * Gets the text to display to the user when this prompt is first
//         * presented.
//         *
//         * @param context Context information about the conversation.
//         * @return The text to display.
//         */
//        @Override
//        public @NotNull String getPromptText(@NotNull ConversationContext context) {
//            MessageConfig config = MessageConfig.get();
////            config.getHologramSet().send((Audience) context.getForWhom());
////            return config.getHologramPromptText();
//            return "todo message(hologramPromptText)";
//        }
//
//        /**
//         * Accepts and processes input from the user. Using the input, the next
//         * Prompt in the prompt graph is returned.
//         *
//         * @param context Context information about the conversation.
//         * @param input   The input text from the user.
//         * @return The next Prompt in the prompt graph.
//         */
//        @Override
//        public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
//            if (input == null) {
//                return null;
//            }
//
//            if (input.equalsIgnoreCase("cancel")) {
////                MessageConfig.get().getHologramPromptCancelled().send((Audience) context.getForWhom()); // todo message(prompt cancel)
//                return null;
//            }
//
//            this.data.set(PersistenceUtil.Key.BLOCK_HOLOGRAM_TEXT.getKey(), DataType.STRING, input);
//            UUID identifier = data.get(PersistenceUtil.Key.BLOCK_HOLOGRAM_ENTITY_UUID.getKey(), DataType.UUID);
//            if (identifier == null) {
//                return null;
//            }
//
//            Hologram hologram = DHAPI.getHologram(identifier.toString());
//            if (hologram != null) {
//                hologram.getPage(0).setLine(0, ColorUtil.applyLegacyColors(PLACEHOLDER_API.matcher(input).replaceAll("")));
//                HologramPersistence.saveToFile(hologram);
//            }
//
//            return null;
//        }
//
//    }

}