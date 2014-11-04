/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Gabriel Harris-Rouquette
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.afterkraft.kraftrpg.util;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.api.util.Properties;

/**
 * Default implementation of Properties
 */
public class RPGPluginProperties implements Properties {

    public static String storageType = "yml";
    public static boolean isMobDamageDistanceModified = false;
    public static boolean isMobHealthDistanceModified = false;
    public static boolean isMobExpDistanceModified = false;
    public static boolean isDamageVarying = false;
    public static double mobDamageDistanceModified = 0D;
    public static double mobHealthDistanceModified = 0D;
    public static double mobExpDistanceModified = 0D;
    public static double distanceTierModifier = 0D;
    public static int maxLevel = 50;
    public static int[] levels;
    public Map<EntityType, FixedPoint> creatureExperienceDrop =
            new EnumMap<>(EntityType.class);

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

    @Override
    public boolean isMobDamageDistanceModified() {
        return isMobDamageDistanceModified;
    }

    public boolean isMobHealthDistanceModified() {
        return isMobHealthDistanceModified;
    }

    public boolean isMobExpDistanceModified() {
        return isMobExpDistanceModified;
    }

    @Override
    public String getStorageType() {
        return storageType.toLowerCase();
    }

    @Override
    public boolean useVanishIfAvailable() {
        return true;
    }

    @Override
    public int getDefaultGlobalCooldown() {
        return 0;
    }

    @Override
    public boolean isVaryingDamageEnabled() {
        return false;
    }

    @Override
    public boolean isStarvingDamageEnabled() {
        return false;
    }

    @Override
    public int getCombatTime() {
        return 0;
    }

    @Override
    public int getDefaultMaxStamina() {
        return 0;
    }

    @Override
    public int getDefaultStaminaRegeneration() {
        return 0;
    }

    @Override
    public int getStaminaIncreaseForFood(Material foodMaterial) {
        return 0;
    }

    @Override
    public int getFoodHealPercent() {
        return 0;
    }

    @Override
    public int getFoodHealthPerTier() {
        return 0;
    }

    @Override
    public long getCombatPeriod() {
        return 0;
    }

    @Override
    public FixedPoint getMonsterExperience(LivingEntity entity, Location spawnPoint) {
        if (isMobExpDistanceModified) {
            FixedPoint exp = this.creatureExperienceDrop.get(entity.getType());
            Double value = Math.ceil(exp != null ? exp.doubleValue() : 0.0D);
            double percent = 1 + mobExpDistanceModified / distanceTierModifier;
            double modifier = Math.pow(percent,
                                       MathUtil.getModulatedDistance(
                                               entity.getWorld().getSpawnLocation(), spawnPoint)
                                               / distanceTierModifier);
            value = Math.ceil(value * modifier);
            return FixedPoint.valueOf(value);
        }
        return null;
    }

    @Override
    public double getExperienceLossMultiplier() {
        return 0;
    }

    @Override
    public double getExperienceLossMultiplierForPVP() {
        return 0;
    }

    @Override
    public FixedPoint getPlayerKillingExperience() {
        return new FixedPoint();
    }

    @Override
    public boolean hasEntityRewardType(EntityType type) {
        return true;
    }

    @Override
    public FixedPoint getEntityReward(EntityType type) {
        return new FixedPoint();
    }

    @Override
    public boolean allowSpawnCamping() {
        return false;
    }

    @Override
    public double getSpawnCampingMultiplier() {
        return 0;
    }
}
