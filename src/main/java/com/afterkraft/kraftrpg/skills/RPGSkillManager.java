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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.ExternalProviderRegistration;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.events.roles.RoleChangeEvent;
import com.afterkraft.kraftrpg.api.events.roles.RoleLevelChangeEvent;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Passive;
import com.afterkraft.kraftrpg.api.skills.PassiveSkill;
import com.afterkraft.kraftrpg.api.skills.Permissible;
import com.afterkraft.kraftrpg.api.skills.PermissionSkill;
import com.afterkraft.kraftrpg.api.skills.Skill;
import com.afterkraft.kraftrpg.api.skills.SkillManager;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.api.skills.SkillUseObject;
import com.afterkraft.kraftrpg.api.skills.Stalled;


public class RPGSkillManager implements SkillManager {
    private static final Set<String> defaultAllowedNodes;
    static {
        defaultAllowedNodes = new HashSet<String>();
        for (SkillSetting setting : SkillSetting.AUTOMATIC_SETTINGS) {
            defaultAllowedNodes.add(setting.node());
            defaultAllowedNodes.add(setting.scalingNode());
        }
        defaultAllowedNodes.remove(null);

        // Add more auto-applied stuff here
        defaultAllowedNodes.add("requirements");
    }
    private final Map<String, ISkill> skillMap;
    private final KraftRPGPlugin plugin;
    private SkillManagerListener listener;

    public RPGSkillManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        listener = new SkillManagerListener();
        skillMap = new HashMap<String, ISkill>();

        for (ISkill skill : ExternalProviderRegistration.getRegisteredSkills()) {
            addSkill(skill);
        }
    }

    public void addSkill(ISkill skill) {
        if (!checkSkillConfig(skill)) {
            return;
        }
        this.skillMap.put(Skill.getNormalizedName(skill.getName()), skill);
        if (skill instanceof Permissible || skill instanceof Passive) {
            this.listener.addSkill(skill);
        }
    }

    @Override
    public boolean hasSkill(String skillName) throws IllegalArgumentException {
        return false;
    }

    private boolean checkSkillConfig(ISkill skill) {
        if (skill.getUsedConfigNodes() == null) {
            return true;
        }
        EnumSet<SkillSetting> settings = EnumSet.copyOf(skill.getUsedConfigNodes());
        ConfigurationSection section = skill.getDefaultConfig();

        Set<String> allowedKeys = new HashSet<String>(defaultAllowedNodes);
        // Build the allowedKeys set
        for (SkillSetting setting : settings) {
            if (setting == SkillSetting.CUSTOM) {
                return true;
            } else if (setting == SkillSetting.CUSTOM_PER_CHAMPION) {
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
                plugin.getLogger().severe("Error in skill " + skill.getName() + ":");
                plugin.getLogger().severe("  Extra default configuration value " + configKey + " not declared in getUsedConfigNodes()");
                return false;
            }
        }
        return true;
    }

    public ISkill getSkill(String name) {
        if (name == null) {
            return null;
        }
        name = name.toLowerCase();
        return skillMap.get(name);
    }

    public boolean loadPermissionSkill(String name) {
        if ((name == null) || (skillMap.get(name.toLowerCase()) != null)) {
            return true;
        }

        final PermissionSkill oSkill = new PermissionSkill(plugin, name);
        final ConfigurationSection config = RPGSkillConfigManager.outsourcedSkillConfig.getConfigurationSection(oSkill.getName());
        final Map<String, Boolean> perms = new HashMap<String, Boolean>();
        if (config != null) {
            final ConfigurationSection permConfig = config.getConfigurationSection("permissions");
            for (String key : permConfig.getKeys(true)) {
                perms.put(key, permConfig.getBoolean(key));
            }
            oSkill.setDescription(config.getString("usage"));

        }
        if (perms.isEmpty()) {
            plugin.log(Level.SEVERE, "There are no permissions defined for " + oSkill.getName());
            return false;
        }
        oSkill.setPermissions(perms);
        skillMap.put(name.toLowerCase(), oSkill);
        return true;
    }

    public Collection<ISkill> getSkills() {
        return Collections.unmodifiableCollection(skillMap.values());
    }

    public boolean isLoaded(String name) {
        return skillMap.containsKey(name.toLowerCase());
    }

    public void removeSkill(ISkill skill) {
        skillMap.remove(skill.getName().toLowerCase().replace("skill", ""));
    }

    @Override
    public boolean isCasterDelayed(SkillCaster caster) {
        return false;
    }

    @Override
    public Stalled getDelayedSkill(SkillCaster caster) {
        return null;
    }

    @Override
    public void setCompletedSkill(SkillCaster caster) {

    }

    @Override
    public void addSkillTarget(Entity o, SkillCaster caster, ISkill skill) {

    }

    @Override
    public SkillUseObject getSkillTargetInfo(Entity o) {
        return null;
    }

    @Override
    public boolean isSkillTarget(Entity o) {
        return false;
    }

    @Override
    public void removeSkillTarget(Entity entity, SkillCaster caster, ISkill skill) {

    }

    /**
     * Load all the skills.
     */
    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(new SkillManagerListener(), plugin);
    }

    public void shutdown() {

    }

    protected class SkillManagerListener implements Listener {

        private final Set<PassiveSkill> passiveSkills = new HashSet<PassiveSkill>();
        private final Set<PermissionSkill> permissionSkills = new HashSet<PermissionSkill>();

        protected void addSkill(ISkill skill) {
            if (skill instanceof PassiveSkill) {
                passiveSkills.add((PassiveSkill) skill);
            } else if (skill instanceof PermissionSkill) {
                permissionSkills.add((PermissionSkill) skill);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onClassChangeEvent(RoleChangeEvent event) {
            for (PermissionSkill skill : permissionSkills) {
                skill.tryLearning(event.getSentientBeing());
            }
            for (PassiveSkill skill : passiveSkills) {
                skill.apply((SkillCaster) event.getSentientBeing());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onLevelChangeEvent(RoleLevelChangeEvent event) {
            for (PermissionSkill skill : permissionSkills) {
                skill.tryLearning(event.getSentientBeing());
            }
            for (PassiveSkill skill : passiveSkills) {
                skill.apply((SkillCaster) event.getSentientBeing());
            }
        }
    }
}
