package dev.oribuin.gadgets.hook;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class PayoutUtil {

    private static final Double PERCENTAGE_AMOUNT = 0.3;
    private static final PrimarySkillType[] PRIMARY_SKILL_TYPES = PrimarySkillType.values();
    
    /**
     * Adjust the given materials map to decrease amounts to the given percentage
     *
     * @param materials The materials map to reduce
     * @return The reduced map of amounts
     */
    public static Map<Material, Integer> getAdjusted(final Map<Material, Integer> materials) {
        Map<Material, Integer> result = new HashMap<>();

        for (final Map.Entry<Material, Integer> entry : materials.entrySet()) {
            result.put(entry.getKey(), (int) (entry.getValue() * PERCENTAGE_AMOUNT));
        }

        return result;
    }

    /**
     * Awards McMMO xp to a player
     *
     * @param player   the player
     * @param adjusted the adjusted materials map
     */
    public static void addMcMMOXp(final Player player, final Map<Material, Integer> adjusted) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) return;

        for (final Map.Entry<Material, Integer> entry : adjusted.entrySet()) {
            Material material = entry.getKey();
            int xp = entry.getValue();

            for (final PrimarySkillType skillType : PRIMARY_SKILL_TYPES) {
                int mcMMOXp = ExperienceConfig.getInstance().getXp(skillType, material);
                if (mcMMOXp > 0) {
                    mcMMOPlayer.applyXpGain(skillType, xp, XPGainReason.PVE, XPGainSource.CUSTOM);
                }
            }
        }
    }
}
