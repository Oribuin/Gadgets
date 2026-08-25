package dev.oribuin.gadgets.hook;

import dev.oribuin.gadgets.GadgetsPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPIProvider extends PlaceholderExpansion {

    private final GadgetsPlugin plugin;
    private static Boolean enabled;

    public PAPIProvider(GadgetsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "Gadgets";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oribuin";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    /**
     * Request placeholder results from the plugin
     *
     * @param player The player who's requested
     * @param params The fishing parameters
     * @return The returning placeholders
     */
    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return null;
    }

    /**
     * Returns true when PlaceholderAPI is enabled on the server, otherwise false
     */
    public static boolean isEnabled() {
        if (enabled == null) enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        return enabled;
    }

    /**
     * Apply the PlaceholderAPI to the given text. If the player is null, it will return the text as is.
     *
     * @param player The player to apply the PlaceholderAPI to.
     * @param text   The text to apply the PlaceholderAPI to.
     * @return The text with the PlaceholderAPI applied.
     */
    public static String apply(@Nullable OfflinePlayer player, String text) {
        if (!isEnabled()) return text;

        return PlaceholderAPI.setPlaceholders(player, text);
    }

    /**
     * Apply the PlaceholderAPI to the given text. If the player is null, it will return the text as is.
     *
     * @param first  The first player to apply the PlaceholderAPI to.
     * @param second The second player to apply the PlaceholderAPI to.
     * @param text   The text to apply the PlaceholderAPI to.
     * @return The text with the PlaceholderAPI applied.
     */
    public static String applyRelational(@Nullable Player first, Player second, String text) {
        if (!isEnabled()) return text;

        return PlaceholderAPI.setRelationalPlaceholders(first, second, text);
    }

}
