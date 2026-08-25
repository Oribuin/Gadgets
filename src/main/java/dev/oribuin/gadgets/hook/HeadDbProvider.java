package dev.oribuin.gadgets.hook;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.oribuin.gadgets.GadgetsPlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class HeadDbProvider implements Listener {

    private static Boolean enabled;
    private static HeadDatabaseAPI api;
    private static final boolean loaded = false;

    /**
     * Returns true when HeadDatabase is enabled on the server, otherwise false
     */
    public static boolean isEnabled() {
        if (enabled == null) enabled = Bukkit.getPluginManager().isPluginEnabled("HeadDatabase");
        return enabled;
    }

    @EventHandler
    private void onLoad(DatabaseLoadEvent event) {
        api = new HeadDatabaseAPI();
    }

    public static HeadDatabaseAPI getApi() {
        return api;
    }

    /**
     * Create a new head from a base64 texture id
     *
     * @param provided The provided base64 id
     * @return The returning itemstack if available
     */
    public static ItemStack getTextured(String provided) {
        return getTextured(provided, null);
    }

    /**
     * Create a new head from a base64 texture id
     *
     * @param provided The provided base64 id
     * @param existing Any existing itemstack to modify
     * @return The returning itemstack if available
     */
    public static ItemStack getTextured(String provided, ItemStack existing) {
        ItemStack target = existing;
        if (target == null) target = new ItemStack(Material.PLAYER_HEAD);

        ResolvableProfile profile = fromBase64(provided);
        target.setData(DataComponentTypes.PROFILE, profile);
        return target;
    }

    /**
     * Create a {@link ResolvableProfile} from a base64 texture
     *
     * @param provided The base64 texture link
     * @return The {@link ResolvableProfile} if available, empty otherwise
     */
    public static ResolvableProfile fromBase64(String provided) {
        try {
            PlayerProfile playerProfile = Bukkit.createProfile(UUID.nameUUIDFromBytes(provided.getBytes()), "");
            PlayerTextures playerTextures = playerProfile.getTextures();

            String decodedTextureJson = new String(Base64.getDecoder().decode(provided));
            String decodedTextureUrl = decodedTextureJson.substring(28, decodedTextureJson.length() - 4);

            playerTextures.setSkin(new URL(decodedTextureUrl));
            playerProfile.setTextures(playerTextures);

            return ResolvableProfile.resolvableProfile(playerProfile);
        } catch (MalformedURLException | NullPointerException | IllegalArgumentException ex) {
            GadgetsPlugin.get().getLogger().warning("Failed to establish resolvable profile from [" + provided + "] due to: " + ex.getMessage());
            return ResolvableProfile.resolvableProfile().build();
        }
    }

}
