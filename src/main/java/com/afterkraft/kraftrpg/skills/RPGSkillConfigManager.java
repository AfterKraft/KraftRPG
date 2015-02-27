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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.persistence.data.DataContainer;
import org.spongepowered.api.service.persistence.data.DataQuery;
import org.spongepowered.api.service.persistence.data.DataView;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ninja.leaping.configurate.ConfigurationNode;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.aspects.SkillAspect;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.SkillConfigManager;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.common.skills.common.PermissionSkill;
import com.afterkraft.kraftrpg.util.MathUtil;

/**
 *
 */
public class RPGSkillConfigManager implements SkillConfigManager {
    // Configurations

    private final Map<SkillCaster, Map<ISkill, ConfigurationNode>>
            customSettings;
    // TODO Review all methods and possible re-implement getters
    protected ConfigurationNode outsourcedSkillConfig;
    protected ConfigurationNode standardSkillConfig;
    protected ConfigurationNode defaultSkillConfig;
    private Map<String, ConfigurationNode> roleSkillConfigurations;
    private File skillConfigFile;
    private File outsourcedSkillConfigFile;
    private KraftRPGPlugin plugin;

    public RPGSkillConfigManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.customSettings = Maps.newHashMap();
        // TODO
    }

    @Override
    public void reload() {
        this.standardSkillConfig = null;
        this.outsourcedSkillConfig = null;
        this.initialize();
    }

    @Override
    public void saveSkillConfig() {

    }

    @Override
    public DataContainer getRoleSkillConfig(String name) {
        return null;
    }

    @Override
    public void addRoleSkillSettings(String roleName, String skillName,
            DataView section) {
        // TODO
    }

    @Override
    public void loadSkillDefaults(ISkill skill) {
        if (skill instanceof PermissionSkill) {
            return;
        }
        // TODO
    }

    @Override
    public void addTemporarySkillConfigurations(ISkill skill,
            SkillCaster caster,
            DataView section) {
        // TODO
    }

    @Override
    public void clearTemporarySkillConfigurations(SkillCaster caster) {
        checkNotNull(caster, "Cannot remove a null caster's custom "
                + "configurations!");
        this.customSettings.remove(caster);
    }

    @Override
    public void clearTemporarySkillConfigurations(SkillCaster caster,
            ISkill skill) {
        checkNotNull(caster, "Cannot clear configurations of a null caster!");
        checkNotNull(skill, "Cannot clear configurations of a null "
                + "skill!");

    }

    @Override
    public String getRawString(ISkill skill, SkillSetting setting) {
        return getRawString(skill, setting.node());
    }

    @Override
    public String getRawString(ISkill skill, DataQuery setting) {
        checkNotNull(skill, "Cannot check the config of a null skill!");
        checkNotNull(setting, "Cannot check the config with a null path!");
        if (outsourcedSkillConfig.getNode(skill.getName() + "." + setting)
                .isVirtual()) {
            throw new IllegalStateException(
                    "The requested skill setting, " + setting
                            + " was not defaulted by the skill: "
                            + skill.getName());
        }
        return outsourcedSkillConfig.getString(skill.getName() + "." + setting);
    }

    @Override
    public Boolean getRawBoolean(ISkill skill, SkillSetting setting) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean getRawBoolean(ISkill skill, DataQuery setting) {
        return Boolean.FALSE;
    }

    @Override
    public Set<DataQuery> getRawKeys(ISkill skill, DataQuery setting) {
        return Sets.newHashSet();
    }

    @Override
    public boolean isSettingConfigured(ISkill skill, SkillSetting setting) {
        checkNotNull(skill,
                "Cannot check the use configurations for a null skill!");
        checkNotNull(setting,
                "Cannot check the use configurations for a null setting!");
        return skill.getDefaultConfig().contains(setting.node()) ||
                !outsourcedSkillConfig.getNode(skill.getName()
                        + "." + setting.node())
                        .isVirtual();
    }

    @Override
    public boolean isSettingConfigured(ISkill skill, DataQuery setting) {
        checkNotNull(skill,
                "Cannot check the use configurations for a null skill!");
        checkNotNull(setting,
                "Cannot check the use configurations for a null setting!");
        return skill.getDefaultConfig().contains(setting) ||
                !outsourcedSkillConfig.getNode(skill.getName() + "." + setting)
                        .isVirtual();
    }

    @Override
    public Object getRawSetting(ISkill skill, DataQuery setting) {
        check(skill, setting);
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException(
                    "The skill: " + skill.getName()
                            + " has no configured defaults for: "
                            + setting);
        }
        return outsourcedSkillConfig.getNode(skill.getName() + "." + setting)
                .getValue();
    }

    @Override
    public Object getRawSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException(
                    "The skill: " + skill.getName()
                            + " has no configured defaults for: " + setting
                            .node());
        }
        return getRawSetting(skill, setting.node());
    }

    @Override
    public int getRawIntSetting(ISkill skill, DataQuery setting) {
        check(skill, setting);
        Object val = getRawSetting(skill, setting);
        if (val == null) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for skill: " + skill
                            .getName()
                            + " and setting: " + setting);
        } else {
            final Integer i = MathUtil.asInt(val);
            if (i == null) {
                throw new IllegalStateException(
                        "The configured setting is not an integer!");
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
    public double getRawDoubleSetting(ISkill skill, DataQuery setting) {
        check(skill, setting);
        Object val = getRawSetting(skill, setting);
        if (val == null) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for skill: " + skill
                            .getName()
                            + " and setting: " + setting);
        } else {
            final Double i = MathUtil.asDouble(val);
            if (i == null) {
                throw new IllegalStateException(
                        "The configured setting is not an integer!");
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
    public String getRawStringSetting(ISkill skill, DataQuery setting) {
        check(skill, setting);
        Object val = getRawSetting(skill, setting);
        if (val == null) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for skill: " + skill
                            .getName()
                            + " and setting: " + setting);
        }
        return val.toString();
    }

    @Override
    public String getRawStringSetting(ISkill skill, SkillSetting setting) {
        check(skill, setting);
        return getRawStringSetting(skill, setting.node());
    }

    @Override
    public Boolean getRawBooleanSetting(ISkill skill, DataQuery setting) {
        check(skill, setting);
        Object val = getRawSetting(skill, setting);
        if (val == null) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for skill: " + skill
                            .getName()
                            + " and setting: " + setting);
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
    public List<String> getRawStringListSetting(ISkill skill,
            DataQuery setting) {
        check(skill, setting);
        final Object val = getRawSetting(skill, setting);
        if (val == null || !(val instanceof List)) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for skill: " + skill
                            .getName()
                            + " and setting: " + setting);
        } else {
            return (List<String>) val;
        }
    }

    @Override
    public List<String> getRawStringListSetting(ISkill skill,
            SkillSetting setting) {
        check(skill, setting);
        return getRawStringListSetting(skill, setting.node());
    }

    @Override
    public ItemStack getRawItemStackSetting(ISkill skill, DataQuery setting) {
        check(skill, setting);
        final Object val = getRawSetting(skill, setting);
        if (!(val instanceof ItemStack)) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for skill: " + skill
                            .getName()
                            + " and setting: " + setting);
        }
        return RpgCommon.getGame().getRegistry().getItemBuilder()
                .fromItemStack((ItemStack) val).build();
    }

    @Override
    public ItemStack getRawItemStackSetting(ISkill skill,
            SkillSetting setting) {
        check(skill, setting);
        return getRawItemStackSetting(skill, setting.node());
    }

    @Override
    public Object getSetting(Role role, ISkill skill, SkillSetting setting) {
        check(role, skill, setting);
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException(
                    "The skill: " + skill.getName()
                            + " has no configured defaults for: " + setting
                            .node());
        }
        return getSetting(role, skill, setting.node());
    }

    @Override
    public Object getSetting(Role role, ISkill skill, DataQuery setting) {
        check(role, skill, setting);
        final ConfigurationNode config = roleSkillConfigurations
                .get(role.getName());
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException(
                    "The skill: " + skill.getName()
                            + " has no configured defaults for: "
                            + setting);
        }
        final String configurationSettingString = skill.getName()
                + "." + setting;

        if (!config.getNode(configurationSettingString).isVirtual()) {
            return config.getNode(configurationSettingString).getValue();
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
    public int getIntSetting(Role role, ISkill skill, DataQuery setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for: " + role
                            .getName() + " skill: "
                            + skill.getName() + " and setting: " + setting);
        } else {
            return MathUtil.toInt(val);
        }
    }

    @Override
    public double getDoubleSetting(Role role, ISkill skill,
            SkillSetting setting) {
        check(role, skill, setting);
        return getDoubleSetting(role, skill, setting.node());
    }

    @Override
    public double getDoubleSetting(Role role, ISkill skill, DataQuery setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for: " + role
                            .getName() + " skill: "
                            + skill.getName() + " and setting: " + setting);
        } else {
            return MathUtil.toDouble(val);
        }
    }

    @Override
    public String getStringSetting(Role role, ISkill skill,
            SkillSetting setting) {
        check(role, skill, setting);
        return getStringSetting(role, skill, setting.node());
    }

    @Override
    public String getStringSetting(Role role, ISkill skill, DataQuery setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for: " + role
                            .getName() + " skill: "
                            + skill.getName() + " and setting: " + setting);
        }
        return val instanceof String ? (String) val : val.toString();
    }

    @Override
    public Boolean getBooleanSetting(Role role, ISkill skill,
            SkillSetting setting) {
        check(role, skill, setting);
        return getBooleanSetting(role, skill, setting.node());
    }

    @Override
    public Boolean getBooleanSetting(Role role, ISkill skill,
            DataQuery setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for: " + role
                            .getName() + " skill: "
                            + skill.getName() + " and setting: " + setting);
        } else {
            return (Boolean) val;
        }
    }

    @Override
    public List<String> getStringListSetting(Role role, ISkill skill,
            SkillSetting setting) {
        check(role, skill, setting);
        return getStringListSetting(role, skill, setting.node());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getStringListSetting(Role role, ISkill skill,
            DataQuery setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (val == null || !(val instanceof List)) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for: " + role
                            .getName() + " skill: "
                            + skill.getName() + " and setting: " + setting);
        } else {
            return (List<String>) val;
        }
    }

    @Override
    public ItemStack getItemStackSetting(Role role, ISkill skill,
            SkillSetting setting) {
        check(role, skill, setting);
        return getItemStackSetting(role, skill, setting.node());
    }

    @Override
    public ItemStack getItemStackSetting(Role role, ISkill skill,
            DataQuery setting) {
        check(role, skill, setting);
        final Object val = getSetting(role, skill, setting);
        if (!(val instanceof ItemStack)) {
            throw new IllegalStateException(
                    "There was an issue getting the setting for: " + role
                            .getName() + " skill:"
                            + skill.getName() + " and setting: " + setting);
        }
        return RpgCommon.getGame().getRegistry().getItemBuilder()
                .fromItemStack((ItemStack) val).build();
    }

    @Override
    public int getLevel(SkillCaster caster, ISkill skill) {
        return getUsedIntSetting(caster, skill, SkillSetting.LEVEL);
    }

    @Override
    public Object getUsedSetting(SkillCaster caster, ISkill skill,
            SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedSetting(caster, skill, setting.node());
    }

    @Override
    public Object getUsedSetting(SkillCaster caster, ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        if (this.customSettings.containsKey(caster) && this.customSettings
                .get(caster)
                .containsKey(skill)) {
            return this.customSettings.get(caster).get(skill)
                    .getNode(setting).getValue();
        }
        if (!isSettingConfigured(skill, setting)) {
            throw new IllegalStateException(
                    "The skill: " + skill.getName()
                            + " has no configured defaults for: "
                            + setting);
        }
        if (caster.canPrimaryUseSkill(skill)) {
            return getSetting(caster.getPrimaryRole().get(), skill, setting);
        } else if (caster.canSecondaryUseSkill(skill)) {
            return getSetting(caster.getSecondaryRole().get(), skill, setting);
        } else if (caster.canAdditionalUseSkill(skill)) {
            for (Role role : caster.getAdditionalRoles()) {
                Optional<SkillAspect> optional = role.getAspect(SkillAspect
                        .class);
                if (optional.isPresent()
                        && optional.get()
                        .hasSkillAtLevel(skill, caster.getLevel(role).get())) {
                    return getSetting(role, skill, setting);
                }
            }
        }
        return outsourcedSkillConfig.getNode(skill.getName() + "." + setting)
                .getValue();
    }

    @Override
    public int getUsedIntSetting(SkillCaster caster, ISkill skill,
            SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedIntSetting(caster, skill, setting.node());
    }

    @Override
    public int getUsedIntSetting(SkillCaster caster, ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        return getUsedNumberSetting(caster, skill, setting).intValue();
    }

    @Override
    public double getUsedDoubleSetting(SkillCaster caster, ISkill skill,
            SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedDoubleSetting(caster, skill, setting.node());
    }

    @Override
    public double getUsedDoubleSetting(SkillCaster caster, ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        return getUsedNumberSetting(caster, skill, setting).doubleValue();
    }

    @Override
    public boolean getUsedBooleanSetting(SkillCaster caster, ISkill skill,
            SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedBooleanSetting(caster, skill, setting.node());
    }

    @Override
    public boolean getUsedBooleanSetting(SkillCaster caster, ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new IllegalStateException(
                "Undefined default for the following skill: " + skill
                        .getName());
    }

    @Override
    public String getUsedStringSetting(SkillCaster caster, ISkill skill,
            SkillSetting setting) {
        check(caster, skill, setting);
        return getUsedStringSetting(caster, skill, setting.node());
    }

    @Override
    public String getUsedStringSetting(SkillCaster caster, ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        return val instanceof String ? (String) val : val.toString();
    }

    @Override
    public List<?> getUsedListSetting(SkillCaster caster, ISkill skill,
            SkillSetting setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof List) {
            return (List<?>) val;
        }
        throw new IllegalStateException(
                "Illegal default for the following skill: " + skill.getName());
    }

    @Override
    public List<?> getUsedListSetting(SkillCaster caster, ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof List) {
            return (List<?>) val;
        }
        throw new IllegalStateException(
                "Illegal default for the following skill: " + skill.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUsedStringListSetting(SkillCaster caster,
            ISkill skill,
            SkillSetting setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof List) {
            return (List<String>) val;
        }
        throw new IllegalStateException(
                "Illegal default for the following skill: " + skill.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUsedStringListSetting(SkillCaster caster,
            ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof List) {
            return (List<String>) val;
        }
        throw new IllegalStateException(
                "Illegal default for the following skill: " + skill.getName());
    }

    @Override
    public ItemStack getUsedItemStackSetting(SkillCaster caster, ISkill skill,
            SkillSetting setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof ItemStack) {
            return RpgCommon.getGame().getRegistry().getItemBuilder()
                    .fromItemStack((ItemStack) val).build();
        }
        throw new IllegalStateException(
                "Illegal default for the following skill: " + skill.getName());
    }

    @Override
    public ItemStack getUsedItemStackSetting(SkillCaster caster, ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof ItemStack) {
            return RpgCommon.getGame().getRegistry().getItemBuilder()
                    .fromItemStack((ItemStack) val).build();
        }
        throw new IllegalStateException(
                "Illegal default for the following skill: " + skill.getName());
    }

    @Override
    public void initialize() {
        // TODO
    }

    @Override
    public void shutdown() {
        this.customSettings.clear();
        roleSkillConfigurations.clear();
    }

    private void check(SkillCaster caster, ISkill skill, SkillSetting setting) {
        checkNotNull(caster,
                "Cannot check the use configurations for a null caster!");
        checkNotNull(skill,
                "Cannot check the use configurations for a null skill!");
        checkNotNull(setting,
                "Cannot check the use configurations for a null setting!");
    }

    private void check(SkillCaster caster, ISkill skill, DataQuery setting) {
        checkNotNull(caster,
                "Cannot check the use configurations for a null caster!");
        checkNotNull(skill,
                "Cannot check the use configurations for a null skill!");
        checkNotNull(setting,
                "Cannot check the use configurations for a null setting!");
    }

    private Number getUsedNumberSetting(SkillCaster caster, ISkill skill,
            DataQuery setting) {
        check(caster, skill, setting);
        Object val = getUsedSetting(caster, skill, setting);
        if (val instanceof Number) {
            return (Number) val;
        } else {
            return MathUtil.asDouble(val);
        }
    }

    private void check(Role role, ISkill skill, SkillSetting setting) {
        checkNotNull(role, "Cannot get a setting for a null role!");
        checkNotNull(skill, "Cannot get a setting for a null skill!");
        checkNotNull(setting, "Cannot get a setting for a null path!");
    }

    private void check(Role role, ISkill skill, DataQuery setting) {
        checkNotNull(role, "Cannot get a setting for a null role!");
        checkNotNull(skill, "Cannot get a setting for a null skill!");
        checkNotNull(setting, "Cannot get a setting for a null path!");
    }

    private void check(ISkill skill, SkillSetting setting) {
        checkNotNull(skill, "Cannot get a setting for a null skill!");
        checkNotNull(setting, "Cannot get a setting for a null path!");
    }

    private void check(ISkill skill, DataQuery setting) {
        checkNotNull(skill, "Cannot get a setting for a null skill!");
        checkNotNull(setting, "Cannot get a setting for a null path!");
    }

    public void setClassDefaults() {
        // TODO
    }

}
