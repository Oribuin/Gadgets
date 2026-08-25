package dev.oribuin.gadgets.util;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

public class MessageHandler {

    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();
    public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    public static TagResolver RESOLVER = TagResolver.builder()
            .resolvers(
                    StandardTags.color(),
                    StandardTags.gradient(),
                    StandardTags.decorations(),
                    StandardTags.clickEvent(),
                    StandardTags.hoverEvent(),
                    StandardTags.shadowColor(),
                    StandardTags.reset(),
                    StandardTags.font(),
                    StandardTags.rainbow(),
                    StandardTags.nbt(),
                    StandardTags.pride()
            )
            .build();
    /**
     * Deserialize Legacy Ampersand Messages
     *
     * @param message Ampersand Message
     * @return {@link Component}
     */
    public static Component deserializeAmpersand(String message) {
        return LEGACY_SERIALIZER.deserialize(message);
    }

    /**
     * Deserialize a MiniMessage message
     * Serialized Messages use MiniMessage
     *
     * @param message MiniMessage serialized message
     * @return {@link Component}
     */
    public static Component deserialize(String message) {
        return MINI_MESSAGE.deserialize(message, RESOLVER).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Send a Component to a Players' Chat
     *
     * @param player  Player
     * @param message Chat Component
     */
    public static void message(Audience player, Component message) {
        player.sendMessage(message);
    }

    /**
     * Send a Component to every player on the server
     *
     * @param message Chat Component
     */
    public static void broadcast(Component message) {
        Bukkit.broadcast(message);
    }

    /**
     * Send a Component to every player on the server with a permission
     *
     * @param message    Chat Component
     * @param permission Permission
     */
    public static void broadcast(Component message, String permission) {
        Bukkit.broadcast(message, permission);
    }

    /**
     * Send a MiniMessage serialized message to a player
     *
     * @param player  Player
     * @param message MiniMessage serialized message
     */
    public static void message(Audience player, String message) {
        player.sendMessage(deserialize(message));
    }

    /**
     * Send a MiniMessage serialized message to a player
     *
     * @param player       Player
     * @param placeholders Varargs for MiniMessage parse replacements
     * @param message      MiniMessage serialized message
     */
    public static void message(Audience player, String message, Placeholders placeholders) {
        player.sendMessage(parse(message, placeholders));
    }

    /**
     * Send a MiniMessage serialized message to the entire server
     *
     * @param message Serialized Message
     */
    public static void broadcast(String message) {
        Bukkit.broadcast(deserialize(message), "");
    }

    public static Title buildTitle(String title, String subtitle) {
        return buildTitle(
            parse(title),
            parse(subtitle)
        );
    }

    /**
     * Build a title.
     *
     * @param title        The title of the title
     * @param subtitle     The subtitle of the title
     * @param placeholders Placeholders to be replaced in the title/subtitle
     * @return The built title.
     */
    public static Title buildTitle(String title, String subtitle, Placeholders placeholders) {
        return buildTitle(
            parse(title, placeholders),
            parse(subtitle, placeholders),
            10,
            100,
            10
        );
    }

    /**
     * Build a title.
     *
     * @param title        The title of the title
     * @param subtitle     The subtitle of the title
     * @param fadeInTicks  How long the title will take to fade in in ticks
     * @param stayTicks    How long the title should stay in ticks
     * @param fadeOutTicks How long the title will take to fade out in ticks
     * @param placeholders Placeholders to be replaced in the title/subtitle
     * @return The built title.
     */
    public static Title buildTitle(String title, String subtitle, long fadeInTicks, long stayTicks, long fadeOutTicks, Placeholders placeholders) {
        return buildTitle(
            parse(title, placeholders),
            parse(subtitle, placeholders),
            fadeInTicks,
            stayTicks,
            fadeOutTicks
        );
    }

    /**
     * Build a title.
     *
     * @param title    The title of the title
     * @param subtitle The subtitle of the title
     * @return The built title
     */
    public static Title buildTitle(Component title, Component subtitle) {
        return buildTitle(title, subtitle, 10, 100, 10);
    }

    /**
     * Build a title.
     *
     * @param title        The title of the title
     * @param subtitle     The subtitle of the title
     * @param fadeInTicks  How long (in ticks) to
     * @param stayTicks    How long the title should stay in ticks
     * @param fadeOutTicks How long the title will take to fade out in ticks
     * @return The built title
     */
    public static Title buildTitle(Component title, Component subtitle, long fadeInTicks, long stayTicks, long fadeOutTicks) {
        return Title.title(title, subtitle, Title.Times.times(
            Duration.ofMillis(fadeInTicks * 50),
            Duration.ofMillis(stayTicks * 50),
            Duration.ofMillis(fadeOutTicks * 50)
        ));
    }

    /**
     * Parse MiniMessage markdown into a component. This should be used rather than whenever
     * there are placeholders which are of non-String types. This allows cleaner code in implementation,
     * as it removes the need for all values to be a String.
     *
     * @param text         The MiniMessage syntax
     * @return The formatted component
     */
    public static Component parse(String text) {
        return parse(text, Placeholders.empty());
    }
    
    /**
     * Parse MiniMessage markdown into a component. This should be used rather than whenever
     * there are placeholders which are of non-String types. This allows cleaner code in implementation,
     * as it removes the need for all values to be a String.
     *
     * @param text         The MiniMessage syntax
     * @param placeholders A key, value array of placeholders.
     * @return The formatted component
     */
    public static Component parse(String text, @NotNull Placeholders placeholders) {
        return placeholders.apply(text);
    }

    /**
     * Makes a cool gradient using HSV - Allowing for unsaturated rainbows
     *
     * @param hue1       Hue of the first color
     * @param hue2       Hue of the second color
     * @param saturation Saturation of the gradient
     * @param text       The string to be colored
     * @return The colored Component
     */
    public static Component gradient(float hue1, float hue2, float saturation, String text) {
        TextComponent.Builder component = Component.text("").toBuilder();
        char[] chars = text.toCharArray();
        float h = hue1;
        float step = hue2 / chars.length;
        for (char a : chars) {
            component.append(Component.text(String.valueOf(a)).color(TextColor.color(HSVLike.hsvLike(h / 360, saturation, 1.0F))));
            h += step;
            if (h > hue2) h = hue1;
        }
        return component.build();
    }

    public static List<Component> parse(List<String> text) {
        return parse(text, Placeholders.empty());
    }
    
    public static List<Component> parse(List<String> text, @NotNull Placeholders placeholders) {
        List<Component> result = Lists.newArrayList();
        for (String str : text) {
            result.add(parse(str, placeholders));
        }
        return result;
    }
    
    public static String replaceLegacyCodes(String content) {
        return replaceSpecificLegacyCodes(content, LegacyChatColor.values());
    }

    public static String replaceLegacyColourCodes(String content) {
        return replaceSpecificLegacyCodes(content, LegacyChatColor.getColours());
    }

    /**
     * thnx phil
     **/
    public static String replaceSpecificLegacyCodes(String content, LegacyChatColor... legacyChatColors) {
        for (LegacyChatColor color : legacyChatColors) {
            content = content.replace(
                String.format("&%s", color.getCode()),
                String.format("<%s>", color.name())
            ).replace(
                String.format("§%s", color.getCode()),
                String.format("<%s>", color.name())
            );
        }
        return content;
    }

    /**
     * fuck placeholderapi
     *
     * @param player
     * @param component
     * @return
     */
    public static Component replacePlaceholderApiPlaceholders(Player player, Component component) {

        Pattern pattern = PlaceholderAPI.getPlaceholderPattern();

        TextReplacementConfig replacementConfig = TextReplacementConfig.builder().match(pattern).replacement(((matchResult, builder) -> {
            String placeholder = matchResult.group(0);
            String replaced = PlaceholderAPI.setPlaceholders(player, placeholder);
            return parse(replaceLegacyCodes(replaced));
        })).build();

        return component.replaceText(replacementConfig);
    }

    /**
     * Deserialize a List of Strings to Kyori Components
     *
     * @param lore Serialized Lore
     * @return List of Components
     */
    public static List<Component> deserialize(List<String> lore) {
        List<Component> list = Lists.newArrayList();

        for (String s : lore) {
            list.add(deserialize(s));
        }

        return list;
    }

    /**
     * thnx phil
     **/
    public enum LegacyChatColor {
        AQUA("b"),
        DARK_GREEN("2"),
        BLACK("0"),
        BLUE("9"),
        BOLD("l"),
        DARK_AQUA("3"),
        DARK_BLUE("1"),
        DARK_GRAY("8"),
        DARK_PURPLE("5"),
        DARK_RED("4"),
        GOLD("6"),
        GRAY("7"),
        GREEN("a"),
        ITALIC("o"),
        LIGHT_PURPLE("d"),
        OBFUSCATED("k"),
        RED("c"),
        RESET("r"),
        STRIKETHROUGH("m"),
        UNDERLINED("n"),
        WHITE("f"),
        YELLOW("e");

        private final String color;

        /**
         * Constructor for the Enum.
         *
         * @param color the legacy colour code
         */
        LegacyChatColor(String color) {
            this.color = color;
        }

        private static final LegacyChatColor[] COLORS = new LegacyChatColor[]{
            AQUA,
            DARK_GREEN,
            BLACK, BLUE,
            DARK_AQUA,
            DARK_BLUE,
            DARK_GRAY,
            DARK_PURPLE,
            DARK_RED,
            GOLD,
            GRAY,
            GREEN,
            LIGHT_PURPLE,
            RED, WHITE,
            YELLOW,
            BOLD
        };

        /**
         * Get an array of all COLORS (non formatting codes)
         *
         * @return the array of all colors
         */
        public static LegacyChatColor[] getColours() {
            return COLORS;
        }

        /**
         * Get an array of all colors + bold (all codes allowed for nicknames)
         *
         * @return the array of all colors + bold
         */
        public static LegacyChatColor[] getNicknameCodes() {
            return COLORS;
        }


        /**
         * Get the legacy colour code.
         *
         * @return the legacy colour code
         */
        public String getCode() {
            return this.color;
        }
    }

    public static String getNiceEnum(Enum<?> enumType) {
        return capitalize(enumType.toString().toLowerCase().replace("_", " "));
    }

    public static String capitalize(String arg) {
        if (arg.length() == 0) {
            return arg;
        }

        char[] buffer = arg.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < buffer.length; i++) {
            char ch = buffer[i];
            if (Character.isWhitespace(ch)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer[i] = Character.toTitleCase(ch);
                capitalizeNext = false;
            }
        }
        return new String(buffer);
    }
}

