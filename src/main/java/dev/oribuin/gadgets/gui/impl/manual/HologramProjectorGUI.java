package dev.oribuin.gadgets.gui.impl.manual;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.config.gui.GuiIcon;
import dev.oribuin.gadgets.config.item.ConstructType;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.config.item.component.TooltipItemType;
import dev.oribuin.gadgets.gui.api.PluginMenu;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.function.Supplier;

public final class HologramProjectorGUI extends PluginMenu<HologramProjectorGUI.HologramProjectorGuiConfig, Gui> {

    /**
     * Creates a new menu for the plugin to use
     *
     * @param plugin The plugin instance
     */
    public HologramProjectorGUI(GadgetsPlugin plugin) {
        super(plugin, HologramProjectorGuiConfig.class);
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

    @ConfigSerializable
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class HologramProjectorGuiConfig extends GuiConfig {

        public HologramProjectorGuiConfig() {
            this.title = "Hologram Projector";
            this.rows = 6;

            this.dummyItems.add(ItemConstruct.of(Material.BLACK_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(0, 1, 2, 6, 7, 8,
                            9, 10, 11, 15, 16, 17,
                            18, 19, 20, 24, 25, 26
                    ));

            this.dummyItems.add(ItemConstruct.of(Material.LIME_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(                    3, 4, 5,
                    12, 14,
                    21, 22, 23
            ));
        }

        private GuiIcon depositSingle = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<green>Deposit 1")
                .setLore(
                        "",
                        "<white>⏩ <green>Left click<white> to deposit one item"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9865"))
                .asMenuItem(15);

        private GuiIcon depositStack = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<green>Deposit Stack")
                .setLore(
                        "",
                        "<white>⏩ <green>Right click<white> to deposit a stack"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9864"))
                .asMenuItem(16);

        private GuiIcon withdrawSingle = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<red>Withdraw 1")
                .setLore(
                        "",
                        "<white>⏩ <red>Left click<white> to withdraw one item"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9327"))
                .asMenuItem(11);

        private GuiIcon withdrawStack =ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<red>Withdraw Stack")
                .setLore(
                        "",
                        "<white>⏩ <red>Right click<white> to withdraw a stack"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9326"))
                .asMenuItem(10);

        private GuiIcon infoItem = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Barrel Information")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Current Stored Type: <#93bc80><type>",
                        "<#93bc80>▎ <white>Stored Amount: <#93bc80><amount>",
                        "<#93bc80>▎ <white>Max Amount: <#93bc80><max>",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-10153"))
                .asMenuItem(4);

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