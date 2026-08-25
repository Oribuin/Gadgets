package dev.oribuin.gadgets.gui.impl.manual;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.TextMessage;
import dev.oribuin.gadgets.config.gui.GuiConfig;
import dev.oribuin.gadgets.config.gui.GuiIcon;
import dev.oribuin.gadgets.config.item.ConstructType;
import dev.oribuin.gadgets.config.item.ItemConstruct;
import dev.oribuin.gadgets.config.item.component.TooltipItemType;
import dev.oribuin.gadgets.container.impl.DeepBarrelContainer;
import dev.oribuin.gadgets.gui.api.GuiTickable;
import dev.oribuin.gadgets.gui.api.PluginMenu;
import dev.oribuin.gadgets.node.impl.DeepStorageBarrel;
import dev.oribuin.gadgets.util.InventoryUtils;
import dev.oribuin.gadgets.util.MessageHandler;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.function.Supplier;

import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_AMOUNT;
import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_ITEM;
import static dev.oribuin.gadgets.util.PersistenceUtil.BARREL_MAX_AMOUNT;

public class BarrelGUI extends PluginMenu<BarrelGUI.BarrelGUIConfig, Gui> implements GuiTickable {

    private final Supplier<DeepStorageBarrel> barrel;

    public BarrelGUI(GadgetsPlugin plugin, Supplier<DeepStorageBarrel> barrel) {
        super(plugin, BarrelGUIConfig.class);
        this.barrel = barrel;
        this.gui = this.createMenu().get();

        // Set GUI Icons & Functionality
        this.setDummyIcons();

        ItemStack stored = this.barrel.get().getValue(BARREL_ITEM);
        boolean isEmpty = stored == null;

        if (isEmpty) {
            GuiIcon inputBelowIcon = this.getConfig().getInputBelowIcon();
            this.gui.setItem(inputBelowIcon.getSlots(), inputBelowIcon.asItem());

            GuiIcon inputAboveIcon = this.getConfig().getInputAboveIcon();
            this.gui.setItem(inputAboveIcon.getSlots(), inputAboveIcon.asItem());

            gui.enableItemPlace();
            gui.enableItemTake();

            // Disable interaction with every slot except the input slot
            int maxSlots = this.gui.getRows() * 9;
            for (int i = 0; i < maxSlots; i++) {
                if (i == this.getConfig().getInputSlot()) continue;
                this.gui.addSlotAction(i, event -> event.setCancelled(true));
            }

            GuiIcon setItem = this.getConfig().getSetTypeItem();
            this.gui.setItem(setItem.getSlots(), setItem.withAction(event -> {
                event.setCancelled(true);

                DeepStorageBarrel storageBarrel = this.barrel.get();
                if (storageBarrel.getValue(BARREL_ITEM) != null) return; // Barrel already has a stack, don't allow setting the type

                ItemStack inputStack = event.getInventory().getItem(this.getConfig().getInputSlot());

                if (inputStack == null || inputStack.getType() == Material.AIR) {
                    this.getConfig().getItemSlotEmpty().send(event.getWhoClicked());
                    return;
                }

                if (DeepStorageBarrel.isBlacklisted(inputStack)) {
                    this.getConfig().getBlacklistedItem().send(event.getWhoClicked());
                    return;
                }

                storageBarrel.setValue(BARREL_ITEM, inputStack.asQuantity(1));
                storageBarrel.setValue(BARREL_AMOUNT, inputStack.getAmount());
                storageBarrel.serialize();
                this.getConfig().getSetItem().send(event.getWhoClicked(),
                        "type", inputStack.displayName());
                inputStack.setAmount(0);
                event.getWhoClicked().closeInventory();
            }));

            return;
        }

        // Barrel not empty

        gui.setItem(this.getConfig().getInputSlot(), new GuiItem(stored));

        // region Deposit Items
        GuiIcon depositSingle = this.getConfig().getDepositSingle();
        gui.setItem(depositSingle.getSlots(), depositSingle.withAction(event -> deposit(event, 1)));

        GuiIcon depositStack = this.getConfig().getDepositStack();
        gui.setItem(depositStack.getSlots(), depositStack.withAction(event -> deposit(event, 64)));
        // endregion

        // region Withdraw Items
        GuiIcon withdrawSingle = this.getConfig().getWithdrawSingle();
        gui.setItem(withdrawSingle.getSlots(), withdrawSingle.withAction(event -> withdraw(event, 1)));

        GuiIcon withdrawStack = this.getConfig().getWithdrawStack();
        gui.setItem(withdrawStack.getSlots(), withdrawStack.withAction(event -> withdraw(event, 64)));
        // endregion

        // Misc Items
        GuiIcon infoItem = this.getConfig().getInfoItem();
        gui.setItem(infoItem.getSlots(), infoItem.asItem(this.barrel.get().getPlaceholders().get()));
        // endregion
    }

    /**
     * Deposit a specified amount into the gui
     *
     * @param event  The inventory click event
     * @param amount The amount being deposited into the barrel
     */
    private void deposit(InventoryClickEvent event, int amount) {
        Player player = (Player) event.getWhoClicked();
        PlayerInventory inventory = player.getInventory();
        DeepStorageBarrel storageBarrel = this.barrel.get();
        DeepBarrelContainer container = new DeepBarrelContainer(storageBarrel);
        int storedAmount = storageBarrel.getValue(BARREL_AMOUNT, 0);
        int maxAmount = storageBarrel.getValue(BARREL_MAX_AMOUNT, 4096);
        int depositAmount = Math.min(amount, maxAmount - storedAmount);
        ItemStack toDeposit = storageBarrel.getValue(BARREL_ITEM, new ItemStack(Material.AIR))
                .asQuantity(depositAmount);

        if (InventoryUtils.getStock(inventory, toDeposit) < amount) {
            this.getConfig().getNoItemsToDeposit().send(player);
            return;
        }

        if (container.deposit(toDeposit)) {
            inventory.removeItem(toDeposit);
            this.getConfig().getDepositSuccess().send(event.getWhoClicked(), "amount", toDeposit.getAmount());
            this.gui.update();
        }
    }

    /**
     * Withdraw a specific amount of items from the barrel
     *
     * @param event  The click event
     * @param amount The amount being withdrawn
     */
    private void withdraw(InventoryClickEvent event, int amount) {
        Player player = (Player) event.getWhoClicked();
        DeepStorageBarrel storageBarrel = this.barrel.get();
        ItemStack stack = storageBarrel.getValue(BARREL_ITEM);
        int storedAmount = storageBarrel.getValue(BARREL_AMOUNT, 0);
        int maxAmount = storageBarrel.getValue(BARREL_MAX_AMOUNT, 4096);
        if (maxAmount < amount || stack == null) return;

        DeepBarrelContainer container = new DeepBarrelContainer(storageBarrel);

        if (player.getInventory().firstEmpty() == -1) {
            this.getConfig().getWithdrawFailure().send(player);
            return;
        }

        int toWithdraw = Math.min(amount, storedAmount);
        if (container.withdraw(stack, amount)) {
            player.getInventory().addItem(stack.asQuantity(toWithdraw));
            this.getConfig().getWithdrawSuccess().send(player, "amount", toWithdraw);
            this.gui.update();
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

    /**
     * Creates a tickable task for a {@link PluginMenu}
     */
    @Override
    public void tick() {
        DeepStorageBarrel storageBarrel = this.barrel.get();
        if (storageBarrel.getValue(BARREL_ITEM) == null) return;
        int storedAmount = storageBarrel.getValue(BARREL_AMOUNT, 0);

        // Misc Items
        GuiIcon infoItem = this.getConfig().getInfoItem();
        this.gui.setItem(infoItem.getSlots(), infoItem.asItem(storageBarrel.getPlaceholders().get()));

        if (storedAmount <= 0) {
            GuiIcon clearType = this.getConfig().getClearStorageItem();
            this.gui.setItem(clearType.getSlots(), clearType.withAction(
                    storageBarrel.getPlaceholders().get() ,
                    event -> {
                        this.getConfig().getClearedBarrel().send(event.getWhoClicked());
                        storageBarrel.setValue(BARREL_AMOUNT, 0);
                        storageBarrel.setValue(BARREL_ITEM, null);
                        storageBarrel.serialize();
                        event.getWhoClicked().closeInventory();
                    }));
        }

        // endregion
        this.gui.update();
    }

    @ConfigSerializable
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class BarrelGUIConfig extends GuiConfig {

        public BarrelGUIConfig() {
            this.title = "Deep Storage Barrel";
            this.rows = 3;

            this.dummyItems.add(ItemConstruct.of(Material.BLACK_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(
                            0, 1, 2, 6, 7, 8,
                            9, 10, 11, 15, 16, 17,
                            18, 19, 20, 24, 25, 26
                    ));

            this.dummyItems.add(ItemConstruct.of(Material.LIME_STAINED_GLASS_PANE)
                    .setProperty(ConstructType.TOOLTIP, TooltipItemType.HIDDEN)
                    .asMenuItem(
                            3, 4, 5,
                            12, 14,
                            21, 22, 23
                    ));
        }

        private int inputSlot = 13;
        private TextMessage itemSlotEmpty = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>Barrel is empty, place an item in the input slot to set the barrel type");
        private TextMessage setItem = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>Barrel type set to <#93bc80><type><white> - re-open the barrel to deposit items");
        private TextMessage upgradedBarrel = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>You have upgraded the barrel to <#93bc80>x<amount><white> items");
        private TextMessage noItemsToDeposit = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>You have no items to deposit");
        private TextMessage depositSuccess = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>You have deposited <#93bc80>x<amount><white> items");
        private TextMessage withdrawSuccess = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>You have withdrawn <#93bc80>x<amount><white> items");
        private TextMessage withdrawFailure = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>Withdraw unsuccessful, make sure you have enough space in your inventory and that the barrel has enough items stored");
        private TextMessage clearedBarrel = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>You have cleared the stored type - re-open the barrel to set the type again");
        private TextMessage blacklistedItem = new TextMessage("<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>You cannot deposit this type of item");

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

        private GuiIcon clearStorageItem =ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<#93bc80>Clear Stored Type")
                .setLore(
                        "",
                        "<#93bc80>▎ <white>Clears the current stored type",
                        "<#93bc80>▎ <white>Remove all stored items before using this",
                        "",
                        "<white>⏩ <#93bc80>Left click<white> proceed"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9865"))
                .asMenuItem(15);
        
        private GuiIcon inputBelowIcon = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<green>Input Item Below")
                .setLore(
                        "",
                        "<green>▎ <white>Drag and drop an item below to set the barrel type",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9875"))
                .asMenuItem(4);

        private GuiIcon inputAboveIcon = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<green>Input Item Above")
                .setLore(
                        "",
                        "<green>▎ <white>Remove all stored items before using this",
                        "",
                        "<white>⏩ <green>Left click<white> proceed",
                        ""
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-9868"))
                .asMenuItem(22);

        private GuiIcon setTypeItem = ItemConstruct.of(Material.PLAYER_HEAD)
                .setName("<green>Set Barrel Type")
                .setLore(
                        "",
                        "<green>▎ <white>Sets the item type to what item",
                        "<green>▎ <white>is in the input slot",
                        "",
                        "<white>⏩ <green>Left click<white> proceed"
                )
                .setProperty(ConstructType.TEXTURE, x -> x.setValue("hdb-21771"))
                .asMenuItem(16);

        public int getInputSlot() {
            return inputSlot;
        }

        public TextMessage getItemSlotEmpty() {
            return itemSlotEmpty;
        }

        public TextMessage getSetItem() {
            return setItem;
        }

        public TextMessage getUpgradedBarrel() {
            return upgradedBarrel;
        }

        public TextMessage getNoItemsToDeposit() {
            return noItemsToDeposit;
        }

        public TextMessage getDepositSuccess() {
            return depositSuccess;
        }

        public TextMessage getWithdrawSuccess() {
            return withdrawSuccess;
        }

        public TextMessage getWithdrawFailure() {
            return withdrawFailure;
        }

        public TextMessage getClearedBarrel() {
            return clearedBarrel;
        }

        public GuiIcon getDepositSingle() {
            return depositSingle;
        }

        public GuiIcon getDepositStack() {
            return depositStack;
        }

        public GuiIcon getWithdrawSingle() {
            return withdrawSingle;
        }

        public GuiIcon getWithdrawStack() {
            return withdrawStack;
        }

        public GuiIcon getInfoItem() {
            return infoItem;
        }

        public GuiIcon getClearStorageItem() {
            return clearStorageItem;
        }

        public GuiIcon getInputBelowIcon() {
            return inputBelowIcon;
        }

        public GuiIcon getInputAboveIcon() {
            return inputAboveIcon;
        }

        public GuiIcon getSetTypeItem() {
            return setTypeItem;
        }

        public TextMessage getBlacklistedItem() {
            return blacklistedItem;
        }
    }
}
