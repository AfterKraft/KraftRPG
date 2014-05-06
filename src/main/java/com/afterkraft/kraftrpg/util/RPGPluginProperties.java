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

import com.afterkraft.kraftrpg.api.util.Properties;


public class RPGPluginProperties implements Properties {

    public static String storageType;
    public static boolean isMobDamageDistanceModified;
    public static boolean isMobHealthDistanceModified;
    public static boolean isMobExpDistanceModified;
    public static double mobDamageDistanceModified;
    public static double mobHealthDistanceModified;
    public static double mobExpDistanceModified;
    public static double distanceTierModifier;

    public static int maxLevel;
    public static int[] levels;


    public double getMobDamageDistanceModified() {
        return mobDamageDistanceModified;
    }

    public double getMobHealthDistanceModified() {
        return mobHealthDistanceModified;
    }

    public double getMobExpDistanceModified() {
        return mobExpDistanceModified;
    }

    public double getDistanceTierModifier() {
        return distanceTierModifier;
    }

    public boolean isMobDamageDistanceModified() {
        return isMobDamageDistanceModified;
    }

    public boolean isMobHealthDistanceModified() {
        return isMobHealthDistanceModified;
    }

    public boolean isMobExpDistanceModified() {
        return isMobExpDistanceModified;
    }


    public String getStorageType() {
        return storageType;
    }

    @Override
    public int getDefaultGlobalCooldown() {
        return 0;
    }

    @Override
    public int getCombatTime() {
        return 0;
    }
}
