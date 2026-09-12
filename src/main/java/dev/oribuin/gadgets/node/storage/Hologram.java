package dev.oribuin.gadgets.node.storage;

import dev.oribuin.gadgets.node.impl.HologramProjector;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.UUID;

import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_ALIGNMENT;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_BACKGROUND;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_BILLBOARD;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_ENTITY;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_POSITION;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_ROTATION;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_SCALE;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_SHADOW;
import static dev.oribuin.gadgets.util.PersistenceUtil.HOLOGRAM_TEXT;

public class Hologram {

    private static final Color DEFAULT_BACKGROUND = Color.fromARGB(1073741824);
    private static final TagResolver RESOLVER = TagResolver.resolver(
            StandardTags.color(),
            StandardTags.decorations(TextDecoration.BOLD),
            StandardTags.decorations(TextDecoration.ITALIC),
            StandardTags.decorations(TextDecoration.UNDERLINED),
            StandardTags.decorations(TextDecoration.STRIKETHROUGH),
            StandardTags.gradient(),
            StandardTags.pride(),
            StandardTags.reset()
    );

    private Location location;
    private String text;
    private Boolean background;
    private Display.Billboard billboard;
    private TextDisplay.TextAlignment alignment;
    private Double scale;
    private Boolean textShadow;
    private Rotation rotation;
    private TextDisplay display;

    /**
     * Create a new hologram at a designated location
     *
     * @param location The location of the hologram
     * @param text     The text on display
     */
    public Hologram(Location location, String text) {
        this.location = location;
        this.text = text;
        this.background = true;
        this.billboard = Display.Billboard.VERTICAL;
        this.alignment = TextDisplay.TextAlignment.CENTER;
        this.rotation = Rotation.NORTH;
        this.scale = 1.0;
        this.textShadow = true;
        this.display = null;
    }
    
    public static Hologram from(HologramProjector projector) {
        Location position = projector.getValue(HOLOGRAM_POSITION);
        String text = projector.getValue(HOLOGRAM_TEXT);
        Hologram hologram = new Hologram(position, text);
        hologram.setBackground(projector.getValue(HOLOGRAM_BACKGROUND, true));
        hologram.setBillboard(projector.getValue(HOLOGRAM_BILLBOARD, Display.Billboard.VERTICAL));
        hologram.setAlignment(projector.getValue(HOLOGRAM_ALIGNMENT, TextDisplay.TextAlignment.CENTER));
        hologram.setScale(projector.getValue(HOLOGRAM_SCALE, 1.0));
        hologram.setTextShadow(projector.getValue(HOLOGRAM_SHADOW, true));
        hologram.setRotation(projector.getValue(HOLOGRAM_ROTATION, Rotation.NORTH));

        // Add the existing display
        UUID display = projector.getValue(HOLOGRAM_ENTITY);
        if (display != null && position != null) {
            Entity entity = position.getWorld().getEntity(display);
            if (entity instanceof TextDisplay textDisplay) hologram.setDisplay(textDisplay);
        }

        return hologram;
    }

    public void update() {
        if (this.location == null) return;

        if (this.display == null || this.display.isDead()) {
            this.display = this.location.getWorld().spawn(this.location, TextDisplay.class);
        }
        
        this.rotation.apply(this.location);
        this.display.teleport(this.location);
        this.display.text(MiniMessage.builder().tags(RESOLVER).build().deserialize(this.text));
        this.display.setDefaultBackground(this.background);
        this.display.setBillboard(this.billboard);
        this.display.setDisplayWidth((float) Math.clamp(this.scale, 0.5, 2.0));
        this.display.setDisplayHeight((float) Math.clamp(this.scale, 0.5, 2.0));
        this.display.setShadowed(this.textShadow);
        this.display.setAlignment(this.alignment);
    }

    public enum Rotation {
        NORTH(180),
        NORTH_EAST(-135),
        EAST(-90),
        SOUTH_EAST(-45),
        SOUTH(0),
        SOUTH_WEST(45),
        WEST(90),
        NORTH_WEST(135);

        private final float rotation;

        Rotation(float rotation) {
            this.rotation = rotation;
        }

        public void apply(Location location) {
            location.setRotation(this.rotation, 0);
        }

        public float getRotation() {
            return rotation;
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getBackground() {
        return background;
    }

    public void setBackground(Boolean background) {
        this.background = background;
    }

    public Display.Billboard getBillboard() {
        return billboard;
    }

    public void setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
    }

    public TextDisplay.TextAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(TextDisplay.TextAlignment alignment) {
        this.alignment = alignment;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Boolean getTextShadow() {
        return textShadow;
    }

    public void setTextShadow(Boolean textShadow) {
        this.textShadow = textShadow;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public TextDisplay getDisplay() {
        return display;
    }

    public void setDisplay(TextDisplay display) {
        this.display = display;
    }
}
