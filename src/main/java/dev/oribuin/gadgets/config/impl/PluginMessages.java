package dev.oribuin.gadgets.config.impl;

import dev.oribuin.gadgets.GadgetsPlugin;
import dev.oribuin.gadgets.config.TextMessage;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class PluginMessages {

    public static final String PREFIX = "<#94bc80><b>Gadgets</b> <dark_gray>▎ <white>";

    public static PluginMessages get() {
        return GadgetsPlugin.get().getLoader().get(PluginMessages.class);
    }

    @Comment("The message sent when a user reloads the plugin")
    private TextMessage reload = new TextMessage(PREFIX + "You have reloaded the plugin in <#93bc80><time><white>ms");

    @Comment("The message sent when a player does not have permission to do something.")
    private TextMessage noPermission = new TextMessage(PREFIX + "You do not have permission to do this");

    @Comment("The message sent when a player does not have permission to do something.")
    private TextMessage requirePlayer = new TextMessage(PREFIX + "You need to be sender type of <#94bc80><sender><white> to run this command");

    @Comment("The message sent when a player gets the syntax for a message wrong")
    private TextMessage invalidSyntax = new TextMessage(PREFIX + "You have provided invalid syntax. The correct usage is: <#94bc80><syntax>");

    @Comment("The target has a full inventory")
    private TextMessage fullInventory = new TextMessage(PREFIX + "The target you have provided has a full inventory.");

    @Comment("The target has been given a specified item")
    private TextMessage givenItem = new TextMessage(PREFIX + "You have provided the <#94bc80><target><white> with [<#94bc80>x<amount> <type><white>]: <white><name>");

    public TextMessage getReload() {
        return reload;
    }

    public TextMessage getNoPermission() {
        return noPermission;
    }

    public TextMessage getRequirePlayer() {
        return requirePlayer;
    }

    public TextMessage getInvalidSyntax() {
        return invalidSyntax;
    }

    public TextMessage getFullInventory() {
        return fullInventory;
    }

}
