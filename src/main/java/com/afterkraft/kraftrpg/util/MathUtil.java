/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.kraftrpg.util;

import org.bukkit.Location;

import com.afterkraft.kraftrpg.api.util.FixedPoint;


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

    public static int getLevel(FixedPoint exp) {
        for (int i = RPGPluginProperties.maxLevel - 1; i >= 0; i--) {
            if (exp.asDouble() >= RPGPluginProperties.levels[i]) {
                return i + 1;
            }
        }
        return -1;
    }
}
