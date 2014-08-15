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
package com.afterkraft.kraftrpg.skills;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.Role.RoleType;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.SkillConfigManager;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.api.skills.common.PermissionSkill;
import com.afterkraft.kraftrpg.util.MathUtil;


public class RPGSkillConfigManager implements SkillConfigManager {
    // Configurations
    protected static Configuration outsourcedSkillConfig;
    protected static Configuration standardSkillConfig;
    protected static Configuration defaultSkillConfig = new MemoryConfiguration();

    private static Map<String, Configuration> roleSkillConfigurations = new HashMap<String, Configuration>();
    private static File skillConfigFile;
    private static File outsourcedSkillConfigFile;

    private final Map<SkillCaster, ConfigurationSection> customSettings = new HashMap<SkillCaster, ConfigurationSection>();
    private final RPGPlugin plugin;

    public RPGSkillConfigManager(RPGPlugin plugin) {
        final File dataFolder = plugin.getDataFolder();
        skillConfigFile = new File(dataFolder, "skills.yml");
        outsourcedSkillConfigFile = new File(dataFolder, "permission-skills.yml");
        this.plugin = plugin;
        plugin.getConfigurationManager().checkForConfig(outsourcedSkillConfigFile);
    }

    @Override
    public void reload() {
        standardSkillConfig = null;
        outsourcedSkillConfig = null;
        this.initialize();
    }

    @Override
    public void initialize() {
        // Setup the standard skill configuration
        standardSkillConfig = YamlConfiguration.loadConfiguration(skillConfigFile);
        standardSkillConfig.setDefaults(defaultSkillConfig);
        standardSkillConfig.options().copyDefaults(true);

        // Setup the outsourced skill configuration
        outsourcedSkillConfig = YamlConfiguration.loadConfiguration(outsourcedSkillConfigFile);
        outsourcedSkillConfig.setDefaults(standardSkillConfig);

        //MERGE!
        for (final String key : standardSkillConfig.getKeys(true)) {
            if (standardSkillConfig.isConfigurationSection(key)) {
                continue;
            }
            outsourcedSkillConfig.set(key, standardSkillConfig.get(key));
        }
    }

    @Override
    public void shutdown() {
        this.customSettings.clear();
        roleSkillConfigurations.clear();
    }

    @Override
    public void saveSkillConfig() {
        try {
            ((YamlConfiguration) standardSkillConfig).save(skillConfigFile);
        } catch (final IOException e) {
            this.plugin.log(Level.WARNING, "Unable to save default skills file!");
        }
    }

    @Override
    public Configuration getRoleSkillConfig(String name) {
        return roleSkillConfigurations.get(name);
    }

    @Override
    public void addRoleSkillSettings(String roleName, String skillName, ConfigurationSection section) {
        Validate.notNull(roleName, "Cannot add Role Skill configurations with a null Role name!");
        Validate.notNull(skillName, "Cannot add a Role Skill configuration with a null Skill name");
        Validate.notNull(section, "Cannot add a null configuration section!");
        Configuration config = roleSkillConfigurations.get(roleName);
        if (config == null) {
            config = new MemoryConfiguration();
            roleSkillConfigurations.put(roleName, config);
        }
        if (section == null) {
            return;
        }

        ConfigurationSection classSection = config.getConfigurationSection(skillName);
        if (classSection == null) {
            classSection = config.createSection(skillName);
        }

        for (final String key : section.getKeys(true)) {
            if (section.isConfigurationSection(key)) {
                classSection.createSection(key);
            }
        }

        for (final String key : section.getKeys(true)) {
            if (section.isConfigurationSection(key)) {
                continue;
            }

            classSection.set(key, section.get(key));
        }
    }

    @Override
    public void addTemporarySkillConfigurations(ISkill skill, SkillCaster caster, ConfigurationSection section) {
        Validate.notNull(skill, "Cannot assign a custom setting for a null skill!");
        Validate.notNull(caster, "Cannot assign a custom setting for a null caster!");
        Validate.notNull(section, "Cannot assign a null configuration setting for a caster's skill use!");
        for (String key : skill.getDefaultConfig().getKeys(true)) {
            if (!section.isSet(key)) {
                throw new IllegalArgumentException("The provided ConfigurationSection does not contain all necessary settings for the skill:" + skill.getName());
            }
        }
        this.customSettings.put(caster, section);
    }

    @Override
    public void loadSkillDefaults(ISkill skill) {
        if (skill instanceof PermissionSkill) {
            return;
        }
        final ConfigurationSection dSection = skill.getDefaultConfig();
        final ConfigurationSection newSection = defaultSkillConfig.createSection(skill.getName());
        //Loop through once and create all the keys
        for (final String key : dSection.getKeys(true)) {
            if (dSection.isConfigurationSection(key)) {
                newSection.createSection(key);
            }
        }
        for (final String key : dSection.getKeys(true)) {
            if (dSection.isConfigurationSection(key)) {
                //Skip section as they would overwrite data here
                continue;
            }
            final Object o = dSection.get(key);
            if (o instanceof List) {
                newSection.set(key, new ArrayList<Object>((List<?>) o));
            } else {
                newSection.set(key, o);
            }
        }
    }

    public void setClassDefaults() {
        for (final Configuration config : roleSkillConfigurations.values()) {
            config.setDefaults(outsourcedSkillConfig);
        }
    }

    @Override
    public String getRawString(ISkill skill, String setting) {
        Validate.notNull(skill, "Cannot check the config of a null skill!");
        Validate.notNull(setting, "Cannot check the config with a null path!");
        if (!outsourcedSkillConfig.isSet(skill.getName() + "." + setting)) {
            throw new IllegalStateException("The requested skill setting, " + setting + " was not defaulted by the skill: " + skill.getName());
        }
        return outsourcedSkillConfig.getString(skill.getName() + "." + setting);
    }

    @Override
    public String getRawString(ISkill skill, SkillSetting setting) {
        return getRawString(skill, setting.node());
    }

    @Override
    public Boolean getRawBoolean(ISkill skill, SkillSetting setting) {
        return null;
    }

    @Override
    public Boolean getRawBoolean(ISkill skill, String setting) {
        return null;
    }

    @Override
    public Set<String> getRawKeys(ISkill skill, String setting) {
        String path = skill.getName();
        if (setting != null) {
            path += "." + setting;
        }

        if (!outsourcedSkillConfig.isConfigurationSection(path)) {
            return new HashSet<String>();
        }

        return outsourcedSkillConfig.getConfigurationSection(path).getKeys(false);
    }

    @Override
    public void clearTemporarySkillConfigurations(SkillCaster caster) {
        Validate.notNull(caster, "Cannot remove a null caster's custom configurations!");
        this.customSettings.remove(caster);
    }

    @Override
    public boolean isSettingConfigured(ISkill skill, SkillSetting setting) {
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return skill.getDefaultConfig().contains(setting.node()) || outsourcedSkillConfig.contains(skill.getName() + "." + setting.node());
    }

    @Override
    public boolean isSettingConfigured(ISkill skill, String setting) {
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return skill.getDefaultConfig().contains(setting) || outsourcedSkillConfig.contains(skill.getName() + "." + setting);
    }

    @Override
    public Object getSetting(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException("The skill: " + skill.getName() + " has no configured defaults for: " + setting.node());
        }
        return getSetting(role, skill, setting.node());
    }

    @Override
    public Object getSetting(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot get a setting for a null role!");
        Validate.notNull(skill, "Cannot get a setting for a null skill!");
        Validate.notNull(setting, "Cannot get a setting for a null path!");
        final Configuration config = roleSkillConfigurations.get(role.getName());
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException("The skill: " + skill.getName() + " has no configured defaults for: " + setting);
        }
        return config.get(skill.getName() + "." + setting);
    }

    @Override
    public int getIntSetting(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getIntSetting(role, skill, setting.node());
    }


    @Override
    public int getIntSetting(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        } else {
            final Integer i = MathUtil.asInt(val);
            if (i == null) {
                throw new IllegalStateException("The configured setting is not an integer!");
            }
            return i;
        }
    }

    @Override
    public double getDoubleSetting(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getDoubleSetting(role, skill, setting.node());
    }

    @Override
    public double getDoubleSetting(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return MathUtil.toDouble(val);
        }
    }

    @Override
    public String getStringSetting(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getStringSetting(role, skill, setting.node());
    }

    @Override
    public String getStringSetting(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        }
        return val.toString();
    }

    @Override
    public Boolean getBooleanSetting(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getBooleanSetting(role, skill, setting.node());
    }

    @Override
    public Boolean getBooleanSetting(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return (Boolean) val;
        }
    }

    @Override
    public List<String> getStringListSetting(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getStringListSetting(role, skill, setting.node());
    }

    @Override
    public List<String> getStringListSetting(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        final Object val = getSetting(role, skill, setting);
        if (val == null || !(val instanceof List)) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return (List<String>) val;
        }
    }

    @Override
    public ItemStack getItemStackSetting(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        final Object val = getSetting(role, skill, setting);
        if (!(val instanceof ItemStack)) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill:" + skill.getName() + " and setting: " + setting);
        }
        return new ItemStack((ItemStack) val);
    }

    @Override
    public Set<String> getRootKeys(Role role, ISkill skill) {
        return null;
    }

    @Override
    public ItemStack getItemStackSetting(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getItemStackSetting(role, skill, setting.node());
    }

    @Override
    public Set<String> getSettingKeys(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        String path = skill.getName();
        if (setting != null) {
            path += "." + setting;
        }
        if (!roleSkillConfigurations.containsKey(path)) {
            throw new IllegalStateException("The skill: " + skill.getName() + " has no configured defaults for: " + setting);
        }
        final Configuration config = roleSkillConfigurations.get(role.getName());
        if ((config == null) || !config.isConfigurationSection(path)) {
            return new HashSet<String>();
        }

        return config.getConfigurationSection(path).getKeys(false);
    }

    @Override
    public Set<String> getSettingKeys(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot check the use configurations for a null role!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getSettingKeys(role, skill, setting.node());
    }

    @Override
    public Set<String> getUseSettingKeys(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        final Set<String> vals = new HashSet<String>();
        String path = skill.getName();
        if (setting != null) {
            path += "." + setting;
        }
        final ConfigurationSection section = outsourcedSkillConfig.getConfigurationSection(path);
        if (section != null) {
            vals.addAll(section.getKeys(false));
        }
        if (caster.canPrimaryUseSkill(skill)) {
            vals.addAll(getSettingKeys(caster.getPrimaryRole(), skill, setting));
        }
        if (caster.canSecondaryUseSkill(skill)) {
            vals.addAll(getSettingKeys(caster.getSecondaryRole(), skill, setting));
        }
        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    vals.addAll(getSettingKeys(role, skill, setting));
                }
            }
        }
        return vals;
    }

    @Override
    public Set<String> getUseSettingKeys(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getUseSettingKeys(caster, skill, setting.node());
    }

    @Override
    public List<String> getUseSettingKeys(SkillCaster caster, ISkill skill) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        final Set<String> keys = new HashSet<String>();
        final ConfigurationSection section = outsourcedSkillConfig.getConfigurationSection(skill.getName());
        if (section != null) {
            keys.addAll(section.getKeys(false));
        }

        if (caster.canPrimaryUseSkill(skill)) {
            keys.addAll(getRootKeys(caster.getPrimaryRole(), skill));
        }

        if (caster.canSecondaryUseSkill(skill)) {
            keys.addAll(getRootKeys(caster.getSecondaryRole(), skill));
        }

        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    keys.addAll(getRootKeys(role, skill));
                }
            }
        }
        return ImmutableList.copyOf(keys);
    }

    @Override
    public int getLevel(Sentient being, ISkill skill) {
        return 0;
    }

    @Override
    public int getLevel(Sentient being, ISkill skill, int def) {
        final String name = skill.getName();
        if (being == null) {
            return outsourcedSkillConfig.getInt(name + "." + SkillSetting.LEVEL.node(), def);
        }

        int val1 = getLevel(being, skill, RoleType.PRIMARY, def);
        int val2 = getLevel(being, skill, RoleType.SECONDARY, def);
        int val3 = getLevel(being, skill, RoleType.ADDITIONAL, def);
        int max = Math.max(Math.max(val1, val2), val3);
        if (max != -1) {
            return max;
        } else {
            return outsourcedSkillConfig.getInt(name + "." + SkillSetting.LEVEL.node(), def);
        }
    }

    @Override
    public Object getUsedSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getUsedSetting(caster, skill, setting.node());
    }

    @Override
    public Object getUsedSetting(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        if (this.customSettings.containsKey(caster)) {
            return this.customSettings.get(caster).get(setting);
        }
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException("The skill: " + skill.getName() + " has no configured defaults for: " + setting);
        }
        if (caster.canPrimaryUseSkill(skill)) {
            return getSetting(caster.getPrimaryRole(), skill, setting);
        } else if (caster.canSecondaryUseSkill(skill)) {
            return getSetting(caster.getSecondaryRole(), skill, setting);
        } else if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (role.hasSkillAtLevel(skill, caster.getLevel(role))) {
                    return getSetting(role, skill, setting);
                }
            }
        }
        return outsourcedSkillConfig.get(skill.getName() + "." + setting);
    }

    @Override
    public int getUsedIntSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getUsedIntSetting(caster, skill, setting.node());
    }

    @Override
    public int getUsedIntSetting(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        if (this.customSettings.containsKey(caster)) {
            return this.customSettings.get(caster).getInt(setting);
        }
        final Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof Number) {
            return MathUtil.toInt(val);
        }
        return 0;
    }

    @Override
    public double getUsedDoubleSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return getUsedDoubleSetting(caster, skill, setting.node());
    }

    @Override
    public double getUsedDoubleSetting(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        if (this.customSettings.containsKey(caster)) {
            return this.customSettings.get(caster).getDouble(setting);
        }
        final Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof Number) {
            return MathUtil.toDouble(val);
        }
        return 0;
    }

    @Override
    public boolean getUsedBooleanSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        Boolean val;

        if (caster.canPrimaryUseSkill(skill)) {
            val = getBooleanSetting(caster.getPrimaryRole(), skill, setting.node());
            if (val != null) {
                return val;
            } else {
                throw new IllegalStateException("Undefined default for the following skill: " + skill.getName());
            }
        }

        if (caster.canSecondaryUseSkill(skill)) {
            val = getBooleanSetting(caster.getSecondaryRole(), skill, setting.node());
            if (val != null) {
                return val;
            } else {
                throw new IllegalStateException("Undefined default for the following skill: " + skill.getName());
            }
        }
        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    val = getBooleanSetting(role, skill, setting.node());
                    if (val != null) {
                        return val;
                    } else {
                        throw new IllegalStateException("Undefined default for the following skill: " + skill.getName());
                    }
                }
            }
        }
        throw new IllegalStateException("Undefined default for the following skill: " + skill.getName());
    }

    @Override
    public boolean getUsedBooleanSetting(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");

        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new IllegalStateException("Undefined default for the following skill: " + skill.getName());
    }

    @Override
    public String getUsedStringSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return null;
    }

    @Override
    public String getUsedStringSetting(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return null;
    }

    @Override
    public List<?> getUsedListSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        return null;
    }

    @Override
    public List<?> getUsedListSetting(SkillCaster caster, ISkill skill, String setting) {
        return null;
    }

    @Override
    public List<String> getUsedStringListSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return null;
    }

    @Override
    public List<String> getUsedStringListSetting(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return null;
    }

    @Override
    public ItemStack getUsedItemStackSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return null;
    }

    @Override
    public ItemStack getUsedItemStackSetting(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
        return null;
    }


    public double getUseSetting(SkillCaster caster, ISkill skill, String setting, double def, boolean lower) {
        final String name = skill.getName();
        if (caster == null) {
            return outsourcedSkillConfig.getDouble(name + "." + setting, def);
        }

        double value = -1;
        if (caster.canPrimaryUseSkill(skill)) {
            double temp = getDoubleSetting(caster.getPrimaryRole(), skill, setting);
            if (temp != -1) {
                value = temp;
            }
        }
        if (caster.canSecondaryUseSkill(skill)) {
            double temp = getDoubleSetting(caster.getSecondaryRole(), skill, setting);
            if (temp != -1) {
                if (lower) {
                    value = temp < value ? temp : value;
                } else {
                    value = temp > value ? temp : value;
                }
            }
        }
        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    double temp = getDoubleSetting(role, skill, setting);
                    if (temp != -1) {
                        if (lower) {
                            value = temp < value ? temp : value;
                        } else {
                            value = temp > value ? temp : value;
                        }
                    }
                }
            }
        }
        if (value != -1) {
            return value;
        } else {
            return outsourcedSkillConfig.getDouble(name + "." + setting, def);
        }
    }

    public boolean getUseSetting(SkillCaster caster, ISkill skill, String setting, boolean def) {
        if (caster == null) {
            return outsourcedSkillConfig.getBoolean(skill.getName() + "." + setting);
        }
        Boolean val;

        if (caster.canPrimaryUseSkill(skill)) {
            val = getBooleanSetting(caster.getPrimaryRole(), skill, setting);
            if (val != null) {
                return val;
            }
        }

        if (caster.canSecondaryUseSkill(skill)) {
            val = getBooleanSetting(caster.getSecondaryRole(), skill, setting);
            if (val != null) {
                return val;
            }
        }
        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    val = getBooleanSetting(role, skill, setting);
                    if (val != null) {
                        return val;
                    }
                }
            }
        }

        return def;
    }

    public String getUseSetting(SkillCaster caster, ISkill skill, String setting, String def) {
        if (caster == null) {
            return outsourcedSkillConfig.getString(skill.getName() + "." + setting, def);
        } else if (caster.canPrimaryUseSkill(skill)) {
            return getStringSetting(caster.getPrimaryRole(), skill, setting);
        } else if (caster.canSecondaryUseSkill(skill)) {
            return getStringSetting(caster.getSecondaryRole(), skill, setting);
        } else if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    return getStringSetting(role, skill, setting);
                }
            }
            return outsourcedSkillConfig.getString(skill.getName() + "." + setting);
        } else {
            return outsourcedSkillConfig.getString(skill.getName() + "." + setting);
        }
    }

    public List<String> getUseSetting(SkillCaster caster, ISkill skill, String setting, List<String> def) {
        if (caster == null) {
            final List<String> list = outsourcedSkillConfig.getStringList(skill.getName() + "." + setting);
            return list != null ? list : def;
        }

        final List<String> vals = new ArrayList<String>();
        if (caster.canPrimaryUseSkill(skill)) {
            final List<String> list = getStringListSetting(caster.getPrimaryRole(), skill, setting);
            vals.addAll(list);
        }
        if (caster.canSecondaryUseSkill(skill)) {
            final List<String> list = getStringListSetting(caster.getSecondaryRole(), skill, setting);
            vals.addAll(list);
        }
        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    final List<String> list = getStringListSetting(role, skill, setting);
                    vals.addAll(list);
                }
            }
        }
        if (!vals.isEmpty()) {
            return vals;
        } else {
            final List<String> list = outsourcedSkillConfig.getStringList(skill.getName() + "." + setting);
            return (list != null) && !list.isEmpty() ? list : def;
        }
    }

    public ItemStack getUseSettingItem(SkillCaster caster, ISkill skill, String setting, ItemStack def) {
        if (caster == null) {
            final ItemStack item = outsourcedSkillConfig.getItemStack(skill.getName() + "." + setting);
            return item != null ? item : def;
        }
        ItemStack val;

        for (Role role : caster.getAllRoles()) {
            if (role.hasSkillAtLevel(skill, caster.getLevel(role))) {
                val = getItemStackSetting(role, skill, setting);
                if (val != null) {
                    return val;
                }
            }
        }

        return def;
    }

    private int getLevel(Sentient being, ISkill skill, Role.RoleType state, int def) {
        if (being == null) {
            return -1;
        }
        switch (state) {
            case PRIMARY:
                return being.getPrimaryRole() != null ? getIntSetting(being.getPrimaryRole(), skill, SkillSetting.LEVEL) : -1;
            case SECONDARY:
                return being.getSecondaryRole() != null ? getIntSetting(being.getSecondaryRole(), skill, SkillSetting.LEVEL) : -1;
            case ADDITIONAL:
                int val = -1;
                if (!being.getAdditionalRoles().isEmpty()) {
                    return val;
                }
                for (Role role : being.getAdditionalRoles()) {
                    int roleVal = getIntSetting(role, skill, SkillSetting.LEVEL);
                    val = val < roleVal ? roleVal : val;
                }
                return val;
            default:
                return -1;
        }
    }


}
