package com.afterkraft.kraftrpg.util;

/**
 * @author gabizou
 */
public class RPGPluginProperties {

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
}
