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
import com.afterkraft.kraftrpg.api.skills.PermissionSkill;
import com.afterkraft.kraftrpg.api.skills.SkillConfigManager;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.api.util.Utilities;
import com.afterkraft.kraftrpg.util.MathUtil;


public class RPGSkillConfigManager implements SkillConfigManager {
    // Configurations
    protected static Configuration outsourcedSkillConfig;
    protected static Configuration standardSkillConfig;
    protected static Configuration defaultSkillConfig = new MemoryConfiguration();

    private static Map<String, Configuration> roleSkillConfigurations = new HashMap<String, Configuration>();
    private static File skillConfigFile;
    private static File outsourcedSkillConfigFile;

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
    public Configuration getClassConfig(String name) {
        return roleSkillConfigurations.get(name);
    }

    @Override
    public void addClassSkillSettings(String roleName, String skillName, ConfigurationSection section) {
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

    @Override
    public void setClassDefaults() {
        for (final Configuration config : roleSkillConfigurations.values()) {
            config.setDefaults(outsourcedSkillConfig);
        }
    }

    @Override
    public String getRaw(ISkill skill, String setting, String def) {
        return outsourcedSkillConfig.getString(skill.getName() + "." + setting, def);
    }

    @Override
    public String getRaw(ISkill skill, SkillSetting setting, String def) {
        return getRaw(skill, setting.node(), def);
    }

    @Override
    public Boolean getRaw(ISkill skill, SkillSetting setting, boolean def) {
        return getRaw(skill, setting.node(), def);
    }

    @Override
    public Boolean getRaw(ISkill skill, String setting, boolean def) {
        return Boolean.valueOf(outsourcedSkillConfig.getString(skill.getName() + "." + setting));
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
    public Object getSetting(Role role, ISkill skill, String setting) {
        final Configuration config = roleSkillConfigurations.get(role.getName());
        if ((config == null) || !config.isConfigurationSection(skill.getName())) {
            return null;
        } else {
            return config.get(skill.getName() + "." + setting);
        }
    }

    @Override
    public int getSetting(Role role, ISkill skill, String setting, int def) {
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            return def;
        } else {
            final Integer i = MathUtil.asInt(val);
            return i != null ? i : def;
        }
    }

    @Override
    public double getSetting(Role role, ISkill skill, String setting, double def) {
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            return def;
        } else {
            final Double d = MathUtil.asDouble(val);
            return d != null ? d : def;
        }
    }

    @Override
    public String getSetting(Role role, ISkill skill, String setting, String def) {
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            return def;
        } else {
            return val.toString();
        }
    }

    @Override
    public Boolean getSetting(Role role, ISkill skill, String setting, boolean def) {
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            return null;
        } else if (val instanceof Boolean) {
            return (Boolean) val;
        } else if (val instanceof String) {
            return Boolean.valueOf((String) val);
        } else {
            return null;
        }
    }

    @Override
    public List<String> getSetting(Role role, ISkill skill, String setting, List<String> def) {
        final Configuration config = roleSkillConfigurations.get(role.getName());
        if ((config == null) || !config.isConfigurationSection(skill.getName())) {
            return def;
        }

        final List<String> val = config.getStringList(skill.getName() + "." + setting);
        return (val != null) && !val.isEmpty() ? val : def;
    }

    @Override
    public ItemStack getSettingItem(Role role, ISkill skill, String setting, ItemStack def) {
        final Object val = getSetting(role, skill, setting);
        ItemStack item = Utilities.loadItem(val);

        return (item == null) ? def : item;
    }

    @Override
    public Set<String> getSettingKeys(Role role, ISkill skill, String setting) {
        String path = skill.getName();
        if (setting != null) {
            path += "." + setting;
        }
        final Configuration config = roleSkillConfigurations.get(role.getName());
        if ((config == null) || !config.isConfigurationSection(path)) {
            return new HashSet<String>();
        }

        return config.getConfigurationSection(path).getKeys(false);
    }

    @Override
    public Set<String> getUseSettingKeys(SkillCaster caster, ISkill skill, String setting) {
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
    public List<String> getUseSettingKeys(SkillCaster caster, ISkill skill) {
        final Set<String> keys = new HashSet<String>();
        final ConfigurationSection section = outsourcedSkillConfig.getConfigurationSection(skill.getName());
        if (section != null) {
            keys.addAll(section.getKeys(false));
        }

        if (caster.canPrimaryUseSkill(skill)) {
            keys.addAll(getSettingKeys(caster.getPrimaryRole(), skill, null));
        }

        if (caster.canSecondaryUseSkill(skill)) {
            keys.addAll(getSettingKeys(caster.getSecondaryRole(), skill, null));
        }

        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    keys.addAll(getSettingKeys(role, skill, null));
                }
            }
        }
        return new ArrayList<String>(keys);
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
    public int getUseSetting(SkillCaster caster, ISkill skill, SkillSetting setting, int def, boolean lower) {
        if (setting == SkillSetting.LEVEL) {
            return getLevel(caster, skill, def);
        } else if (setting.isLevelScaled()) {
            int base = getUseSetting(caster, skill, setting.node(), def, lower);
            double scale = getUseSetting(caster, skill, setting.scalingNode(), -1.0, lower);
            if (scale != -1) {
                int level = caster.getHighestSkillLevel(skill);
                return (int) (base + scale * level);
            } else {
                return base;
            }
        } else {
            return getUseSetting(caster, skill, setting.node(), def, lower);
        }
    }

    @Override
    public String getUseSetting(SkillCaster caster, ISkill skill, SkillSetting setting, String def) {
        return getUseSetting(caster, skill, setting.node(), def);
    }

    @Override
    public double getUseSetting(SkillCaster caster, ISkill skill, SkillSetting setting, double def, boolean lower) {
        if (setting.isLevelScaled()) {
            double base = getUseSetting(caster, skill, setting.node(), def, lower);
            double scale = getUseSetting(caster, skill, setting.scalingNode(), -1.0, lower);
            if (scale != -1) {
                int level = caster.getHighestSkillLevel(skill);
                return base + scale * level;
            } else {
                return base;
            }
        } else {
            return getUseSetting(caster, skill, setting.node(), def, lower);
        }
    }

    @Override
    public boolean getUseSetting(SkillCaster caster, ISkill skill, SkillSetting setting, boolean def) {
        return getUseSetting(caster, skill, setting.node(), def);
    }

    @Override
    public ItemStack getUseSettingItem(SkillCaster caster, ISkill skill, SkillSetting setting, ItemStack def) {
        assert setting == SkillSetting.REAGENT;

        return getUseSettingItem(caster, skill, setting.node(), def);
    }

    @Override
    public int getUseSetting(SkillCaster caster, ISkill skill, String setting, int def, boolean lower) {
        if (setting.equalsIgnoreCase("level")) {
            throw new IllegalArgumentException("Do not use getSetting() for grabbing a SkillCaster level!");
        }

        final String name = skill.getName();
        if (caster == null) {
            return outsourcedSkillConfig.getInt(name + "." + setting, def);
        }

        int value = -1;
        if (caster.canPrimaryUseSkill(skill)) {
            int temp = getSetting(caster.getPrimaryRole(), skill, setting, def);
            if (temp != -1) {
                value = temp;
            }
        }
        if (caster.canSecondaryUseSkill(skill)) {
            int temp = getSetting(caster.getSecondaryRole(), skill, setting, def);
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
                    int temp = getSetting(role, skill, setting, def);
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
            return outsourcedSkillConfig.getInt(name + "." + setting, def);
        }
    }

    @Override
    public double getUseSetting(SkillCaster caster, ISkill skill, String setting, double def, boolean lower) {
        final String name = skill.getName();
        if (caster == null) {
            return outsourcedSkillConfig.getDouble(name + "." + setting, def);
        }

        double value = -1;
        if (caster.canPrimaryUseSkill(skill)) {
            double temp = getSetting(caster.getPrimaryRole(), skill, setting, def);
            if (temp != -1) {
                value = temp;
            }
        }
        if (caster.canSecondaryUseSkill(skill)) {
            double temp = getSetting(caster.getSecondaryRole(), skill, setting, def);
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
                    double temp = getSetting(role, skill, setting, def);
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

    @Override
    public boolean getUseSetting(SkillCaster caster, ISkill skill, String setting, boolean def) {
        if (caster == null) {
            return outsourcedSkillConfig.getBoolean(skill.getName() + "." + setting, def);
        }
        Boolean val;

        if (caster.canPrimaryUseSkill(skill)) {
            val = getSetting(caster.getPrimaryRole(), skill, setting, def);
            if (val != null) {
                return val;
            }
        }

        if (caster.canSecondaryUseSkill(skill)) {
            val = getSetting(caster.getSecondaryRole(), skill, setting, def);
            if (val != null) {
                return val;
            }
        }
        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    val = getSetting(role, skill, setting, def);
                    if (val != null) {
                        return val;
                    }
                }
            }
        }

        return def;
    }

    @Override
    public String getUseSetting(SkillCaster caster, ISkill skill, String setting, String def) {
        if (caster == null) {
            return outsourcedSkillConfig.getString(skill.getName() + "." + setting, def);
        } else if (caster.canPrimaryUseSkill(skill)) {
            return getSetting(caster.getPrimaryRole(), skill, setting, def);
        } else if (caster.canSecondaryUseSkill(skill)) {
            return getSetting(caster.getSecondaryRole(), skill, setting, def);
        } else if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    return getSetting(role, skill, setting, def);
                }
            }
            return outsourcedSkillConfig.getString(skill.getName() + "." + setting, def);
        } else {
            return outsourcedSkillConfig.getString(skill.getName() + "." + setting, def);
        }
    }

    @Override
    public List<String> getUseSetting(SkillCaster caster, ISkill skill, String setting, List<String> def) {
        if (caster == null) {
            final List<String> list = outsourcedSkillConfig.getStringList(skill.getName() + "." + setting);
            return list != null ? list : def;
        }

        final List<String> vals = new ArrayList<String>();
        if (caster.canPrimaryUseSkill(skill)) {
            final List<String> list = getSetting(caster.getPrimaryRole(), skill, setting, new ArrayList<String>());
            vals.addAll(list);
        }
        if (caster.canSecondaryUseSkill(skill)) {
            final List<String> list = getSetting(caster.getSecondaryRole(), skill, setting, new ArrayList<String>());
            vals.addAll(list);
        }
        if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                if (caster.canSpecificAdditionalUseSkill(role, skill)) {
                    final List<String> list = getSetting(role, skill, setting, new ArrayList<String>());
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

    @Override
    public ItemStack getUseSettingItem(SkillCaster caster, ISkill skill, String setting, ItemStack def) {
        if (caster == null) {
            final ItemStack item = outsourcedSkillConfig.getItemStack(skill.getName() + "." + setting);
            return item != null ? item : def;
        }
        ItemStack val;

        for (Role role : caster.getAllRoles()) {
            if (role.hasSkillAtLevel(skill, caster.getLevel(role))) {
                val = getSettingItem(role, skill, setting, def);
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
                return being.getPrimaryRole() != null ? getSetting(being.getPrimaryRole(), skill, SkillSetting.LEVEL.node(), def) : -1;
            case SECONDARY:
                return being.getSecondaryRole() != null ? getSetting(being.getSecondaryRole(), skill, SkillSetting.LEVEL.node(), def) : -1;
            case ADDITIONAL:
                int val = -1;
                if (!being.getAdditionalRoles().isEmpty()) {
                    return val;
                }
                for (Role role : being.getAdditionalRoles()) {
                    int roleVal = getSetting(role, skill, SkillSetting.LEVEL.node(), def);
                    val = val < roleVal ? roleVal : val;
                }
                return val;
            default:
                return -1;
        }
    }


}
