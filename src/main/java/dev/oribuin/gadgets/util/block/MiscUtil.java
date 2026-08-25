package dev.oribuin.gadgets.util.block;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.SplittableRandom;

public final class MiscUtil {

    private static final SplittableRandom RANDOM = new SplittableRandom();

    /**
     * Round a double to the desired decimal positions
     *
     * @param value  The value to adjust
     * @param places The decimal places to use
     * @return An adjusted value with desired decimal places
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static SplittableRandom getRandom() {
        return RANDOM;
    }

    /**
     * Calculates the players total experience using Minecrafts formula
     *
     * @param player The player whose experience should be retrieved
     * @return The players experience
     */
    public static int getTotalExperience(final Player player) {
        var exp = Math.round(getExpAtLevel(player.getLevel()) * player.getExp());
        var currentLevel = player.getLevel();
        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    /**
     * Uses Minecraft's formula to calculate the experience for a given level
     *
     * @param level The desired level
     * @return The experience for the given level
     */
    private static int getExpAtLevel(final int level) {
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        else return 9 * level - 158;
    }


    /**
     * Takes the desired amount of identifier stacks from the given player
     *
     * @param player     The player to take the stacks from
     * @param amount     The amount of stack to take
     * @param identifier The identifier to match
     */
    public static void takeAmount(final Player player, final int amount, final String identifier) {
//        int toTake = amount;
//
//        for (final ItemStack stack : player.getInventory().getContents()) {
//            if (stack == null || stack.getType() == Material.AIR) continue;
//            String type = getIdentifier(stack);
//            if (!type.equalsIgnoreCase(identifier)) continue;
//
//            if (stack.getAmount() <= toTake) {
//                toTake -= stack.getAmount();
//                stack.setAmount(0);
//            } else {
//                stack.setAmount(stack.getAmount() - toTake);
//                toTake -= toTake;
//            }
//
//            if (toTake == 0) break;
//        }
//
//        player.updateInventory();
    }

    public static String replaceUnderscoresAndCapitalize(String input) {
        // Check if the input is null or empty
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Convert the input to lowercase
        String newString = input.toLowerCase();

        // Split the input into words using underscores as separators
        String[] words = newString.split("_");

        // Create a StringBuilder to build the result
        StringBuilder result = new StringBuilder();

        // Iterate through each word, capitalize the first letter, and append to the result
        for (String word : words) {
            if (!word.isEmpty()) {
                char firstLetter = Character.toUpperCase(word.charAt(0));
                result.append(firstLetter).append(word.substring(1)).append(" ");
            }
        }

        // Remove the trailing space and return the result
        return result.toString().trim();
    }

    private MiscUtil() {
        throw new UnsupportedOperationException();
    }

}
