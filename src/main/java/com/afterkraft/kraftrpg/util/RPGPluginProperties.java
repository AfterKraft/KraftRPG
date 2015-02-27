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

import java.io.File;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.world.Location;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.api.util.Properties;

/**
 * Default implementation of Properties
 */
public class RPGPluginProperties implements Properties {

    public static final String STORAGE = "storage";

    public static final String MOBS_ALLOW_SPAWN_CAMPING = "spawn-camping";

    public static final String MOBS_SPAWN_CAMPING_MULTIPLIER =
            "spawn-camping-multiplier";

    public static final String MOBS_DAMAGE_DISTANCE = "spawn-distance-damage";

    public static final String MOBS_DAMAGE_DISTANCE_MODIFIER =
            "spawn-distance-damage-modifier";

    public static final String MOBS_HEALTH_DISTANCE = "spawn-distance-health";

    public static final String MOBS_HEALTH_DISTANCE_MODIFIER =
            "spawn-distance-health-modifier";

    public static final String MOBS_EXP_DISTANCE = "spawn-distance-experience";

    public static final String MOBS_EXP_DISTANCE_MODIFIER =
            "spawn-distance-experience-modifier";
    public static final String MOBS_DISTANCE = "spawn-distance-mod";
    public static final String GLOBAL_COOLDOWN = "global-cooldown";
    public static final String ALLOW_SPAWN_CAMPING = "allow-spawn-camping";
    public static final String SPAWN_CAMPING_MODIFIER
            = "spawn-camping-modifier";
    private static RPGPluginProperties instance;
    public Map<EntityType, FixedPoint> creatureExperienceDrop =
            Maps.newHashMap();
    private CommentedConfigurationNode root = SimpleCommentedConfigurationNode
            .root();

    /**
     * Get the instance of this properties
     *
     * @return The instance of the plugin properties
     */
    public static RPGPluginProperties getInstance() {
        return instance;
    }

    /**
     * Initializes the properties file
     */
    public void initialize() {
        instance = this;
        KraftRPGPlugin plugin = (KraftRPGPlugin) RpgCommon.getPlugin();
        checkNotNull(plugin);
        RPGConfigManager manager = plugin.getConfigurationManager();
        checkNotNull(manager);
        File directory = manager.getConfigDirectory();
        File mainConfig = new File(directory + File.separator + "main.hocon");
        try {
            if (!mainConfig.getParentFile().exists()) {
                mainConfig.getParentFile().mkdirs();
            }
            HoconConfigurationLoader loader;
            if (!mainConfig.exists()) {
                mainConfig.createNewFile();

                loader = HoconConfigurationLoader.builder()
                        .setFile(mainConfig).build();
                ConfigBase configBase = new GlobalConfig();
                ObjectMapper<ConfigBase>.BoundInstance configMapper
                        = ObjectMapper.forObject(configBase);
                configMapper.serialize(this.root.getNode("main"));
                loader.save(this.root);
                this.root = loader.load();
                KraftRPGPlugin.getInstance().getLogger()
                        .info(this.root.getString());
                KraftRPGPlugin.getInstance().getLogger().info(this.root
                        .getNode("main").getNode("skills").getString());
            } else {
                loader = HoconConfigurationLoader.builder()
                        .setFile(mainConfig).build();
                this.root = loader.load();
                KraftRPGPlugin.getInstance().getLogger().info(
                        this.root.getString());
                KraftRPGPlugin.getInstance().getLogger().info(this.root
                        .getNode("main").getNode("skills").getString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public double getMobDamageDistanceModified() {
        return this.root.getNode("main").getNode("entity")
                .getNode(MOBS_DAMAGE_DISTANCE_MODIFIER).getDouble();
    }

    public double getMobHealthDistanceModified() {
        return this.root.getNode("main").getNode("entity")
                .getNode(MOBS_HEALTH_DISTANCE_MODIFIER)
                .getDouble();
    }

    public double getMobExpDistanceModified() {
        return this.root.getNode("main").getNode("entity")
                .getNode(MOBS_EXP_DISTANCE_MODIFIER).getDouble();
    }

    public double getDistanceTierModifier() {
        return this.root.getNode("main").getNode("entity")
                .getNode(MOBS_DISTANCE).getDouble();
    }

    public boolean isMobHealthDistanceModified() {
        return this.root.getNode("main").getNode("entity")
                .getNode(MOBS_DAMAGE_DISTANCE).getBoolean();
    }

    public boolean isMobExpDistanceModified() {
        return this.root.getNode("main").getNode("entity")
                .getNode(MOBS_EXP_DISTANCE).getBoolean();
    }

    @Override
    public int getDefaultGlobalCooldown() {
        return this.root.getNode("main").getNode("skills")
                .getNode(GLOBAL_COOLDOWN).getInt();
    }

    @Override
    public boolean isVaryingDamageEnabled() {
        return false; // TODO
    }

    @Override
    public boolean isStarvingDamageEnabled() {
        return false; // TODO
    }

    @Override
    public int getCombatTime() {
        return 0; // TODO
    }

    @Override
    public int getDefaultMaxStamina() {
        return 0; // TODO
    }

    @Override
    public int getDefaultStaminaRegeneration() {
        return 0; // TODO
    }

    @Override
    public int getStaminaIncreaseForFood(ItemType foodMaterial) {
        return 0; // TODO
    }

    @Override
    public int getFoodHealPercent() {
        return 0; // TODO
    }

    @Override
    public int getFoodHealthPerTier() {
        return 0; // TODO
    }

    @Override
    public long getCombatPeriod() {
        return 0; // TODO
    }

    @Override
    public FixedPoint getMonsterExperience(Living entity, Location spawnPoint) {
        if (this.isMobDamageDistanceModified()) {
            FixedPoint exp = this.creatureExperienceDrop.get(entity.getType());
            Double value = Math.ceil(exp != null ? exp.doubleValue() : 0.0D);
            double percent = 1 + this.getMobDamageDistanceModified() / this
                    .getDistanceTierModifier();
            double modifier = Math.pow(percent,
                    MathUtil.getModulatedDistance(
                            entity.getLocation(),
                            spawnPoint)
                            / this.getDistanceTierModifier());
            value = Math.ceil(value * modifier);
            return FixedPoint.valueOf(value);
        }
        return FixedPoint.valueOf(0);
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
    public Optional<FixedPoint> getEntityReward(EntityType type) {
        return Optional.absent();
    }

    @Override
    public boolean allowSpawnCamping() {
        return this.root.getNode("main").getNode("combat")
                .getNode(ALLOW_SPAWN_CAMPING).getBoolean();
    }

    @Override
    public double getSpawnCampingMultiplier() {
        return this.root.getNode("main").getNode("combat")
                .getNode(SPAWN_CAMPING_MODIFIER).getInt();
    }

    @Override
    public boolean isMobDamageDistanceModified() {
        return this.root.getNode("main").getNode("entity")
                .getNode(MOBS_DAMAGE_DISTANCE).getBoolean();
    }

    @Override
    public String getStorageType() {
        return this.root.getNode("main").getNode("storage").getNode(STORAGE)
                .getString().toLowerCase();
    }

    @Override
    public boolean useVanishIfAvailable() {
        return true;
    }

    private class GlobalConfig extends ConfigBase {

    }

    private class ConfigBase {

        @Setting
        public EntityCategory entity = new EntityCategory();
        @Setting
        public StorageCategory storage = new StorageCategory();
        @Setting
        public CombatCategory combat = new CombatCategory();
        @Setting
        public SkillCategory skills = new SkillCategory();


        @ConfigSerializable
        private class EntityCategory extends Category {

            @Setting(value = MOBS_DAMAGE_DISTANCE,
                     comment = "Whether mob damage based on distance from "
                             + "spawn is modified")
            public boolean isMobDamageDistanceModified = false;
            @Setting(value = MOBS_HEALTH_DISTANCE,
                     comment = "Whether mob health is modified based on the "
                             + "distance from spawn")
            public boolean isMobHealthDistanceModified = false;
            @Setting(value = MOBS_EXP_DISTANCE,
                     comment = "Whether mob experience is multiplied by a "
                             + "factor of distance from spawn")
            public boolean isMobExpDistanceModified = false;
            @Setting(value = MOBS_DAMAGE_DISTANCE_MODIFIER,
                     comment = "How much mob damage is multiplied by based on"
                             + " distance")
            public double mobDamageDistanceModified = 0D;
            @Setting(value = MOBS_HEALTH_DISTANCE_MODIFIER,
                     comment = "How much mob health is multiplied by based on"
                             + " distance")
            public double mobHealthDistanceModified = 0D;
            @Setting(value = MOBS_EXP_DISTANCE_MODIFIER,
                     comment = "How much mob experience is multiplied by "
                             + "based on distance")
            public double mobExpDistanceModified = 0D;

            @Setting(value = MOBS_DISTANCE,
                     comment = "The distance between mob modifier jumps")
            public double mobdistanceTier = 100.0D;

        }

        @ConfigSerializable
        private class CombatCategory extends Category {

            @Setting(value = ALLOW_SPAWN_CAMPING,
                     comment = "Whether spawn camping spawners is allowed or "
                             + "not.")
            public boolean spawnCamping = false;
            @Setting(value = SPAWN_CAMPING_MODIFIER,
                     comment = "How much experience is reduced by for spawn "
                             + "camped mobs")
            public double campingMultiplier = 0.1D;
        }

        @ConfigSerializable
        private class SkillCategory extends Category {

            @Setting(value = GLOBAL_COOLDOWN,
                     comment = "The global cooldown between casting skills. "
                             + "Measured in milliseconds.")
            public int globalCooldown = 10;
        }

        @ConfigSerializable
        private class StorageCategory extends Category {

            @Setting(value = STORAGE,
                     comment = "The storage manage for player data")
            public String storage = "hocon";
        }

        @ConfigSerializable
        private class Category {

        }
    }
}
