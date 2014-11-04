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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.google.common.collect.ImmutableSet;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.ExternalProviderRegistration;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.events.roles.RoleChangeEvent;
import com.afterkraft.kraftrpg.api.events.roles.RoleLevelChangeEvent;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Passive;
import com.afterkraft.kraftrpg.api.skills.PassiveSkill;
import com.afterkraft.kraftrpg.api.skills.Skill;
import com.afterkraft.kraftrpg.api.skills.SkillManager;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.api.skills.SkillUseObject;
import com.afterkraft.kraftrpg.api.skills.Stalled;
import com.afterkraft.kraftrpg.api.skills.common.Permissible;
import com.afterkraft.kraftrpg.api.skills.common.PermissionSkill;

/**
 * Default implementation of SkillManager for KraftRPG.
 */
public class RPGSkillManager implements SkillManager {
    private static final Set<String> DEFAULT_ALLOWED_NODES;

    static {
        DEFAULT_ALLOWED_NODES = new HashSet<>();
        for (SkillSetting setting : SkillSetting.AUTOMATIC_SETTINGS) {
            DEFAULT_ALLOWED_NODES.add(setting.node());
            DEFAULT_ALLOWED_NODES.add(setting.scalingNode());
        }
        DEFAULT_ALLOWED_NODES.remove(null);

        // Add more auto-applied stuff here
        DEFAULT_ALLOWED_NODES.add("requirements");
    }

    private final Map<String, ISkill> skillMap;
    private final KraftRPGPlugin plugin;
    private SkillManagerListener listener;

    public RPGSkillManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.listener = new SkillManagerListener();
        this.skillMap = new HashMap<>();
    }

    /**
     * Load all the skills.
     */
    @Override
    public void initialize() {
        for (ISkill skill : ExternalProviderRegistration.getRegisteredSkills()) {
            addSkill(skill);
        }
        this.plugin.getServer().getPluginManager()
                .registerEvents(new SkillManagerListener(), this.plugin);
    }

    @Override
    public void addSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot add a null skill!");
        if (!checkSkillConfig(skill)) {
            return;
        }
        this.skillMap.put(Skill.getNormalizedName(skill.getName()), skill);
        if (skill instanceof Permissible || skill instanceof Passive) {
            this.listener.addSkill(skill);
        }
    }

    @Override
    public boolean hasSkill(String skillName) {
        Validate.notNull(skillName, "Cannot check a null skill name!");
        Validate.isTrue(!skillName.isEmpty(), "Cannot check an empty skill name!");
        return false;
    }

    @Override
    public ISkill getSkill(String name) {
        Validate.notNull(name, "Cannot get a null skill name!");
        name = name.toLowerCase();
        return this.skillMap.get(name);
    }

    @Override
    public boolean loadPermissionSkill(String name) {
        Validate.notNull(name, "Cannot load a null permission skill name!");
        if ((name == null) || (this.skillMap.get(name.toLowerCase()) != null)) {
            return true;
        }

        final PermissionSkill oSkill = new PermissionSkill(this.plugin, name);
        final ConfigurationSection config = RPGSkillConfigManager.outsourcedSkillConfig
                .getConfigurationSection(oSkill.getName());
        final Map<String, Boolean> perms = new HashMap<>();
        if (config != null) {
            final ConfigurationSection permConfig = config.getConfigurationSection("permissions");
            for (String key : permConfig.getKeys(true)) {
                perms.put(key, permConfig.getBoolean(key));
            }
            oSkill.setDescription(config.getString("usage"));

        }
        if (perms.isEmpty()) {
            this.plugin
                    .log(Level.SEVERE, "There are no permissions defined for " + oSkill.getName());
            return false;
        }
        oSkill.setPermissions(perms);
        this.skillMap.put(name.toLowerCase(), oSkill);
        return true;
    }

    @Override
    public Collection<ISkill> getSkills() {
        return Collections.unmodifiableCollection(this.skillMap.values());
    }

    @Override
    public boolean isLoaded(String name) {
        Validate.notNull(name, "Cannot check if a null skill name is loaded!");
        return this.skillMap.containsKey(name.toLowerCase());
    }

    @Override
    public void removeSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot remove a null skill!");
        this.skillMap.remove(skill.getName().toLowerCase().replace("skill", ""));
    }

    @Override
    public boolean isCasterDelayed(SkillCaster caster) {
        Validate.notNull(caster, "Cannot check if a null caster is delayed!");
        return false;
    }

    @Override
    public Stalled getDelayedSkill(SkillCaster caster) {
        Validate.notNull(caster, "Cannot get the stalled skill of a null caster!");
        return null;
    }

    @Override
    public void setCompletedSkill(SkillCaster caster) {
        Validate.notNull(caster, "Cannot set a null caster to complete a skill!");

    }

    @Override
    public void addSkillTarget(Entity o, SkillCaster caster, ISkill skill) {
        Validate.notNull(o, "Cannot add a null entity as a skill target!");
        Validate.notNull(caster, "Cannot add a null caster!");
        Validate.notNull(skill, "Cannot add a skill target to a null skill!");

    }

    @Override
    public SkillUseObject getSkillTargetInfo(Entity o) {
        Validate.notNull(o, "Cannot get the skill target info on a null entity!");
        return null;
    }

    @Override
    public boolean isSkillTarget(Entity o) {
        Validate.notNull(o, "Cannot check a null skill target!");
        return false;
    }

    @Override
    public void removeSkillTarget(Entity entity, SkillCaster caster, ISkill skill) {
        Validate.notNull(entity, "Cannot remove a null entity skill target!");
        Validate.notNull(caster, "Cannot remove a null caster skill target!");
        Validate.notNull(skill, "Cannot remove a null skill from a skill target!");

    }

    private boolean checkSkillConfig(ISkill skill) {
        if (skill.getUsedConfigNodes() == null || skill.getUsedConfigNodes().isEmpty()) {
            return true;
        }
        Set<SkillSetting> settings = ImmutableSet.copyOf(skill.getUsedConfigNodes());
        ConfigurationSection section = skill.getDefaultConfig();

        Set<String> allowedKeys = new HashSet<>(DEFAULT_ALLOWED_NODES);
        // Build the allowedKeys set
        for (SkillSetting setting : settings) {
            if (setting == SkillSetting.CUSTOM) {
                return true;
            } else if (setting == SkillSetting.CUSTOM_PER_CASTER) {
                continue;
            }
            allowedKeys.add(setting.node());
            allowedKeys.add(setting.scalingNode());
        }
        allowedKeys.remove(null);

        // Let's provide a nice message
        // return allowedKeys.containsAll(section.getKeys(false));
        for (String configKey : section.getKeys(false)) {
            if (!allowedKeys.contains(configKey)) {
                this.plugin.getLogger().severe("Error in skill " + skill.getName() + ":");
                this.plugin.getLogger().severe("  Extra default configuration value " + configKey
                                                       + " not declared in getUsedConfigNodes()");
                return false;
            }
        }
        return true;
    }

    @Override
    public void shutdown() {

    }

    private class SkillManagerListener implements Listener {

        private final Set<PassiveSkill> passiveSkills = new HashSet<>();
        private final Set<PermissionSkill> permissionSkills = new HashSet<>();

        protected void addSkill(ISkill skill) {
            if (skill instanceof PassiveSkill) {
                this.passiveSkills.add((PassiveSkill) skill);
            } else if (skill instanceof PermissionSkill) {
                this.permissionSkills.add((PermissionSkill) skill);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onClassChangeEvent(RoleChangeEvent event) {
            for (PermissionSkill skill : this.permissionSkills) {
                skill.tryLearning(event.getSentientBeing());
            }
            for (PassiveSkill skill : this.passiveSkills) {
                skill.apply((SkillCaster) event.getSentientBeing());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onLevelChangeEvent(RoleLevelChangeEvent event) {
            for (PermissionSkill skill : this.permissionSkills) {
                skill.tryLearning(event.getSentientBeing());
            }
            for (PassiveSkill skill : this.passiveSkills) {
                skill.apply((SkillCaster) event.getSentientBeing());
            }
        }
    }
}
