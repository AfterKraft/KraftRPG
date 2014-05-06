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
package com.afterkraft.kraftrpg.entity.roles;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.entity.roles.RoleManager;
import com.afterkraft.kraftrpg.api.entity.roles.RoleType;
import com.afterkraft.kraftrpg.api.skills.ISkill;


public class RPGRole implements Role {
    private transient final RPGPlugin plugin;
    private final String name;
    private transient final RoleType type;
    private transient final Map<ISkill, Set<ISkill>> skillDependencies = new HashMap<ISkill, Set<ISkill>>();
    private transient final Set<ISkill> skills = new LinkedHashSet<ISkill>();
    private transient final Map<Material, Double> itemDamages = new EnumMap<Material, Double>(Material.class);
    private transient final Map<Material, Double> itemDamagePerLevel = new EnumMap<Material, Double>(Material.class);

    public RPGRole(RPGPlugin plugin, String name, RoleType type) {
        this.plugin = plugin;
        this.name = name;
        this.type = type;
    }

    public final RoleType getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasSkill(ISkill skill) {
        return skill != null && skills.contains(skill);
    }

    public boolean hasSkill(String name) {
        ISkill query = plugin.getSkillManager().getSkill(name);
        return query != null && skills.contains(query);
    }

    @Override
    public boolean addSkill(ISkill skill) {
        return skill != null && this.skills.add(skill);
    }

    @Override
    public void removeSkill(ISkill skill) {
        if (skill == null) {
            return;
        }
        this.skills.remove(skill);
        // We need to check the dependencies
        if (this.skillDependencies.containsKey(skill)) {
            this.skillDependencies.remove(skill);
        }
        // We also need to check if another skill depended on the provided skill
        for (Map.Entry<ISkill, Set<ISkill>> entry : this.skillDependencies.entrySet()) {
            if (entry.getValue().contains(skill)) {
                entry.getValue().remove(skill);
            }
        }
        plugin.getRoleManager().queueRoleRefresh(this, RoleManager.RoleRefreshReason.SKILL_REMOVAL);

    }

    @Override
    public boolean hasPrerequisite(ISkill skill) {
        return skill != null && this.skillDependencies.containsKey(skill);
    }

    @Override
    public boolean hasPrerequisite(String name) {
        ISkill query = plugin.getSkillManager().getSkill(name);
        return query != null && this.skillDependencies.containsKey(query);
    }

    @Override
    public Set<ISkill> getSkillDependency(ISkill skill) {
        if (skill == null) {
            return null;
        }
        return this.skillDependencies.get(skill);
    }

    @Override
    public Set<ISkill> getSkillDependency(String name) {
        ISkill query = plugin.getSkillManager().getSkill(name);
        if (query == null) {
            return null;
        }
        return this.skillDependencies.get(query);
    }

    @Override
    public Map<ISkill, Set<ISkill>> getSkillDependencies() {
        return Collections.unmodifiableMap(this.skillDependencies);
    }

    @Override
    public boolean addSkillDependency(ISkill skill, ISkill dependency) {
        if (skill == null || dependency == null) {
            return false;
        }
        Set<ISkill> dependencies = this.skillDependencies.get(skill);
        if (dependencies == null) {
            dependencies = new HashSet<ISkill>();
        }

        return !dependencies.contains(dependency) && dependencies.add(dependency);
    }

    @Override
    public void removeSkillDependency(ISkill skill, ISkill dependency) {
        if (skill == null || dependency == null) {
            return;
        }
        Set<ISkill> dependencies = this.skillDependencies.get(skill);
        if (dependencies == null) {
            return;
        }
        if (dependencies.contains(dependency)) {
            dependencies.remove(dependency);
        }
    }

    @Override
    public void removeSkillDependency(ISkill skill) {
        if (skill == null) {
            return;
        }
        Set<ISkill> dependencies = this.skillDependencies.get(skill);
        if (dependencies == null) {
            return;
        }
        this.skillDependencies.remove(skill);
    }

    public double getItemDamage(Material type) {
        return this.itemDamages.get(type) != null ? this.itemDamages.get(type) : 0.0D;
    }

    public void setItemDamage(Material type, double damage) {
        this.itemDamages.put(type, damage);
    }

    public double getItemDamagePerLevel(Material type) {
        return this.itemDamagePerLevel.get(type) != null ? this.itemDamagePerLevel.get(type) : 0.0D;
    }

    public void setItemDamagePerLevel(Material type, double damage) {
        this.itemDamagePerLevel.put(type, damage);
    }

}
