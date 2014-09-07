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

    // TODO Review all methods and possible re-implement getters
    protected static Configuration outsourcedSkillConfig;
    protected static Configuration standardSkillConfig;
    protected static Configuration defaultSkillConfig = new MemoryConfiguration();

    private static Map<String, Configuration> roleSkillConfigurations = new HashMap<String, Configuration>();
    private static File skillConfigFile;
    private static File outsourcedSkillConfigFile;

    private final Map<SkillCaster, Map<ISkill, ConfigurationSection>> customSettings = new HashMap<SkillCaster, Map<ISkill, ConfigurationSection>>();
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
        Map<ISkill, ConfigurationSection> skillMap = this.customSettings.get(caster);
        if (skillMap == null) {
            skillMap = new HashMap<ISkill, ConfigurationSection>();
        }
        skillMap.put(skill, section);
        this.customSettings.put(caster, skillMap);
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
    public void clearTemporarySkillConfigurations(SkillCaster caster, ISkill skill) {
        Validate.notNull(caster, "Cannot clear configurations of a null caster!");
        Validate.notNull(skill, "Cannot clear configurations of a null skill!");
        Map<ISkill, ConfigurationSection> skillMap = this.customSettings.get(caster);
        if (skillMap != null) {
            skillMap.remove(skill);
            this.customSettings.put(caster, skillMap);
        }
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
    public Object getRawSetting(ISkill skill, String setting) {
        check(skill, setting);
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException("The skill: " + skill.getName() + " has no configured defaults for: " + setting);
        }
        return outsourcedSkillConfig.get(skill.getName() + "." + setting);
    }

    @Override
    public Object getRawSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException("The skill: " + skill.getName() + " has no configured defaults for: " + setting.node());
        }
        return getRawSetting(skill, setting.node());
    }

    @Override
    public int getRawIntSetting(ISkill skill, String setting) {
        check(skill, setting);
        Object val = getRawSetting(skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName() + " and setting: " + setting);
        } else {
            final Integer i = MathUtil.asInt(val);
            if (i == null) {
                throw new IllegalStateException("The configured setting is not an integer!");
            }
            return i;
        }
    }

    @Override
    public int getRawIntSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        return getRawIntSetting(skill, setting.node());
    }

    @Override
    public double getRawDoubleSetting(ISkill skill, String setting) {
        check(skill, setting);
        Object val = getRawSetting(skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName() + " and setting: " + setting);
        } else {
            final Double i = MathUtil.asDouble(val);
            if (i == null) {
                throw new IllegalStateException("The configured setting is not an integer!");
            }
            return i;
        }
    }

    @Override
    public double getRawDoubleSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        return getRawDoubleSetting(skill, setting.node());
    }

    @Override
    public String getRawStringSetting(ISkill skill, String setting) {
        check(skill, setting);
        Object val = getRawSetting(skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName() + " and setting: " + setting);
        }
        return val.toString();
    }

    @Override
    public String getRawStringSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        return getRawStringSetting(skill, setting.node());
    }

    @Override
    public Boolean getRawBooleanSetting(ISkill skill, String setting) {
        check(skill, setting);
        Object val = getRawSetting(skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return (Boolean) val;
        }
    }

    @Override
    public Boolean getRawBooleanSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        return getRawBooleanSetting(skill, setting.node());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRawStringListSetting(ISkill skill, String setting) {
        check(skill, setting);
        final Object val = getRawSetting(skill, setting);
        if (val == null || !(val instanceof List)) {
            throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return (List<String>) val;
        }
    }

    @Override
    public List<String> getRawStringListSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        return getRawStringListSetting(skill, setting.node());
    }

    @Override
    public ItemStack getRawItemStackSetting(ISkill skill, String setting) {
        check(skill, setting);
        final Object val = getRawSetting(skill, setting);
        if (!(val instanceof ItemStack)) {
            throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName() + " and setting: " + setting);
        }
        return new ItemStack((ItemStack) val);
    }

    @Override
    public ItemStack getRawItemStackSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        return getRawItemStackSetting(skill, setting.node());
    }

    @Override
    public Object getSetting(Role role, ISkill skill, SkillSetting setting) {
        check(role, skill, setting);
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException("The skill: " + skill.getName() + " has no configured defaults for: " + setting.node());
        }
        return getSetting(role, skill, setting.node());
    }

    @Override
    public Object getSetting(Role role, ISkill skill, String setting) {
        check(role, skill, setting);
        final Configuration config = roleSkillConfigurations.get(role.getName());
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException("The skill: " + skill.getName() + " has no configured defaults for: " + setting);
        }
        final String configurationSettingString = skill.getName() + "." + setting;

        if (config.isSet(configurationSettingString)) {
            return config.get(configurationSettingString);
        } else {
            return getRawSetting(skill, setting);
        }
    }

    @Override
    public int getIntSetting(Role role, ISkill skill, SkillSetting setting) {
        check(role, skill, setting);
        return getIntSetting(role, skill, setting.node());
    }


    @Override
    public int getIntSetting(Role role, ISkill skill, String setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return MathUtil.toInt(val);
        }
    }

    @Override
    public double getDoubleSetting(Role role, ISkill skill, SkillSetting setting) {
        check(role, skill, setting);
        return getDoubleSetting(role, skill, setting.node());
    }

    @Override
    public double getDoubleSetting(Role role, ISkill skill, String setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return MathUtil.toDouble(val);
        }
    }

    @Override
    public String getStringSetting(Role role, ISkill skill, SkillSetting setting) {
        check(role, skill, setting);
        return getStringSetting(role, skill, setting.node());
    }

    @Override
    public String getStringSetting(Role role, ISkill skill, String setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        }
        return val instanceof String ? (String) val : val.toString();
    }

    @Override
    public Boolean getBooleanSetting(Role role, ISkill skill, SkillSetting setting) {
        check(role, skill, setting);
        return getBooleanSetting(role, skill, setting.node());
    }

    @Override
    public Boolean getBooleanSetting(Role role, ISkill skill, String setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return (Boolean) val;
        }
    }

    @Override
    public List<String> getStringListSetting(Role role, ISkill skill, SkillSetting setting) {
        check(role, skill, setting);
        return getStringListSetting(role, skill, setting.node());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getStringListSetting(Role role, ISkill skill, String setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null || !(val instanceof List)) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill: " + skill.getName() + " and setting: " + setting);
        } else {
            return (List<String>) val;
        }
    }

    @Override
    public ItemStack getItemStackSetting(Role role, ISkill skill, String setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (!(val instanceof ItemStack)) {
            throw new IllegalStateException("There was an issue getting the setting for: " + role.getName() + " skill:" + skill.getName() + " and setting: " + setting);
        }
        return new ItemStack((ItemStack) val);
    }

    @Override
    public ItemStack getItemStackSetting(Role role, ISkill skill, SkillSetting setting) {
        check(role, skill, setting);
        return getItemStackSetting(role, skill, setting.node());
    }

    @Override
    public int getLevel(SkillCaster caster, ISkill skill) {
        return getUsedIntSetting(caster, skill, SkillSetting.LEVEL);
    }

    @Override
    public Object getUsedSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedSetting(caster, skill, setting.node());
    }

    @Override
    public Object getUsedSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        if (this.customSettings.containsKey(caster) && this.customSettings.get(caster).containsKey(skill)) {
            return this.customSettings.get(caster).get(skill).get(setting);
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

    private Number getUsedNumberSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof Number) {
            return (Number) val;
        } else {
            return MathUtil.asDouble(val);
        }
    }

    @Override
    public int getUsedIntSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedIntSetting(caster, skill, setting.node());
    }

    @Override
    public int getUsedIntSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        return getUsedNumberSetting(caster, skill, setting).intValue();
    }

    @Override
    public double getUsedDoubleSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedDoubleSetting(caster, skill, setting.node());
    }

    @Override
    public double getUsedDoubleSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        return getUsedNumberSetting(caster, skill, setting).doubleValue();
    }

    @Override
    public boolean getUsedBooleanSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedBooleanSetting(caster, skill, setting.node());
    }

    @Override
    public boolean getUsedBooleanSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new IllegalStateException("Undefined default for the following skill: " + skill.getName());
    }

    @Override
    public String getUsedStringSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedStringSetting(caster, skill, setting.node());
    }

    @Override
    public String getUsedStringSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        return val instanceof String ? (String) val : val.toString();
    }

    @Override
    public List<?> getUsedListSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof List) {
            return (List<?>) val;
        }
        throw new IllegalStateException("Illegal default for the following skill: " + skill.getName());
    }

    @Override
    public List<?> getUsedListSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof List) {
            return (List<?>) val;
        }
        throw new IllegalStateException("Illegal default for the following skill: " + skill.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUsedStringListSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof List) {
            return (List<String>) val;
        }
        throw new IllegalStateException("Illegal default for the following skill: " + skill.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUsedStringListSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof List) {
            return (List<String>) val;
        }
        throw new IllegalStateException("Illegal default for the following skill: " + skill.getName());
    }

    @Override
    public ItemStack getUsedItemStackSetting(SkillCaster caster, ISkill skill, SkillSetting setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof ItemStack)
            return new ItemStack((ItemStack) val);
        throw new IllegalStateException("Illegal default for the following skill: " + skill.getName());
    }

    @Override
    public ItemStack getUsedItemStackSetting(SkillCaster caster, ISkill skill, String setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof ItemStack)
            return new ItemStack((ItemStack) val);
        throw new IllegalStateException("Illegal default for the following skill: " + skill.getName());
    }

    private void check(SkillCaster caster, ISkill skill, String setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
    }

    private void check(SkillCaster caster, ISkill skill, SkillSetting setting) {
        Validate.notNull(caster, "Cannot check the use configurations for a null caster!");
        Validate.notNull(skill, "Cannot check the use configurations for a null skill!");
        Validate.notNull(setting, "Cannot check the use configurations for a null setting!");
    }

    private void check(Role role, ISkill skill, String setting) {
        Validate.notNull(role, "Cannot get a setting for a null role!");
        Validate.notNull(skill, "Cannot get a setting for a null skill!");
        Validate.notNull(setting, "Cannot get a setting for a null path!");
    }

    private void check(Role role, ISkill skill, SkillSetting setting) {
        Validate.notNull(role, "Cannot get a setting for a null role!");
        Validate.notNull(skill, "Cannot get a setting for a null skill!");
        Validate.notNull(setting, "Cannot get a setting for a null path!");
    }

    private void check(ISkill skill, String setting) {
        Validate.notNull(skill, "Cannot get a setting for a null skill!");
        Validate.notNull(setting, "Cannot get a setting for a null path!");
    }

    private void check(ISkill skill, SkillSetting setting) {
        Validate.notNull(skill, "Cannot get a setting for a null skill!");
        Validate.notNull(setting, "Cannot get a setting for a null path!");
    }

}
