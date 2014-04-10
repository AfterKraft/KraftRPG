package com.afterkraft.kraftrpg.util;

import org.bukkit.Location;

/**
 * @author gabizou
 */
public class MathUtil {

    public static Integer asInt(Object val) {
        if (val instanceof String) {
            try {
                return Integer.valueOf((String) val);
            } catch (final NumberFormatException e) {
                return null;
            }
        } else if (!(val instanceof Number)) {
            return null;
        } else {
            return ((Number) val).intValue();
        }
    }

    public static Double asDouble(Object val) {
        if (val instanceof String) {
            try {
                return Double.valueOf((String) val);
            } catch (final NumberFormatException e) {
                return null;
            }
        } else if (!(val instanceof Number)) {
            return null;
        } else {
            return ((Number) val).doubleValue();
        }
    }

    public static double getDistance(final Location from, final Location to) {
        return (from.toVector().distance(to.toVector())) - (from.toVector().distance(to.toVector()) % RPGPluginProperties.distanceTierModifier);
    }

    public static int getLevel(double exp) {
        for (int i = RPGPluginProperties.maxLevel - 1; i >= 0; i--) {
            if (exp >= RPGPluginProperties.levels[i]) {
                return i + 1;
            }
        }
        return -1;
    }
}
