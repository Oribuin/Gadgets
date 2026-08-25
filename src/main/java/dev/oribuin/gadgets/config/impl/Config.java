package dev.oribuin.gadgets.config.impl;

import dev.oribuin.gadgets.GadgetsPlugin;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Config {

    public static Config get() {
        return GadgetsPlugin.get().getLoader().get(Config.class);
    }

}
