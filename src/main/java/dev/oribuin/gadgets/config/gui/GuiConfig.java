package dev.oribuin.gadgets.config.gui;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public abstract class GuiConfig {

    protected String title = "Plugin Menu";
    protected int rows = 5;
    protected List<GuiIcon> dummyItems = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public List<GuiIcon> getDummyItems() {
        return dummyItems;
    }

}
