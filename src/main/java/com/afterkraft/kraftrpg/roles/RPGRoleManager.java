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
package com.afterkraft.kraftrpg.roles;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.Validate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.CircularDependencyException;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.RoleManager;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Passive;
import com.afterkraft.kraftrpg.api.skills.Permissible;
import com.afterkraft.kraftrpg.api.util.DirectedGraph;
import com.afterkraft.kraftrpg.entity.RPGEntityManager;


public class RPGRoleManager implements RoleManager {

    private final KraftRPGPlugin plugin;
    private final Map<String, Role> roleMap;
    private Role defaultPrimaryRole;
    private Role defaultSecondaryRole;
    private DirectedGraph<Role> roleGraph = new DirectedGraph<Role>();

    public RPGRoleManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.roleMap = new HashMap<String, Role>();

    }

    @Override
    public Role getDefaultPrimaryRole() {
        return this.defaultPrimaryRole.asNewCopy();
    }

    @Override
    public boolean setDefaultPrimaryRole(Role role) {
        if (role == null || role.getType() != Role.RoleType.PRIMARY) {
            return false;
        }
        this.defaultPrimaryRole = role;
        return true;
    }

    @Override
    public Role getDefaultSecondaryRole() {
        return this.defaultSecondaryRole.asNewCopy();
    }

    @Override
    public void setDefaultSecondaryRole(Role role) {
        Validate.notNull(role, "Cannot set the a default secondary null Role!");
        Validate.isTrue(role.getType() == Role.RoleType.SECONDARY, "Cannot have a non Secondary RoleType as the default Secondary Role!");
        this.defaultSecondaryRole = role.asNewCopy();
        return;
    }

    @Override
    public Role getRole(String roleName) {
        return roleMap.get(roleName);
    }

    @Override
    public boolean addRole(Role role) {
        if (role == null || !this.roleMap.containsKey(role.getName())) {
            return false;
        }
        this.roleMap.put(role.getName(), role);
        roleGraph.addVertex(role);
        return true;
    }

    @Override
    public boolean removeRole(Role role) throws IllegalArgumentException {
        if (role == null || !this.roleMap.containsKey(role.getName())) {
            return true;
        }
        roleGraph.removeVertex(role);
        this.roleMap.remove(role.getName());
        return false;
    }

    @Override
    public Map<String, Role> getRoles() {
        ImmutableMap.Builder<String, Role> builder = ImmutableMap.builder();
        for (Map.Entry<String, Role> entry : roleMap.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().asNewCopy());
        }
        return builder.build();
    }

    @Override
    public Map<String, Role> getRolesByType(Role.RoleType type) {
        Validate.notNull(type, "Cannot get Roles by type of a null RoleType!");
        ImmutableMap.Builder<String, Role> builder = ImmutableMap.builder();
        for (Map.Entry<String, Role> entry : roleMap.entrySet()) {
            if (entry.getValue().getType() == type)
            builder.put(entry.getKey(), entry.getValue().asNewCopy());
        }
        return builder.build();
    }

    @Override
    public boolean addRoleDependency(Role parent, Role child) {
        Validate.notNull(parent, "Cannot add a null Role Parent dependency!");
        Validate.notNull(child, "Cannot add a null Role child dependency!");
        reconstructRoleGraph();
        roleGraph.addEdge(parent, child);
        Role newParent = Role.builder(plugin).copyOf(parent).addChild(child).build();
        Role newChild = Role.builder(plugin).copyOf(child).addParent(newParent).build();
        this.roleMap.remove(parent.getName());
        this.roleMap.remove(child.getName());
        this.roleMap.put(newParent.getName(), newParent);
        this.roleMap.put(newChild.getName(), newChild);
        swapRoles(parent, newParent);
        swapRoles(child, newChild);
        return true;
    }

    public void swapRoles(Role oldRole, Role newRole) {
        boolean skillChanged = oldRole.getAllSkills().size() == newRole.getAllSkills().size();
        if (plugin.getEntityManager() instanceof RPGEntityManager) {
            RPGEntityManager entityManager = (RPGEntityManager) plugin.getEntityManager();
            for (Sentient being : entityManager.getAllSentientBeings()) {
                boolean hasChanged = false;
                if (being.getPrimaryRole().equals(oldRole)) {
                    being.setPrimaryRole(newRole);
                    hasChanged = true;
                } else if (oldRole.equals(being.getSecondaryRole())) {
                    being.setSecondaryRole(newRole);
                    hasChanged = true;
                } else if (being.getAdditionalRoles().contains(oldRole)) {
                    being.removeAdditionalRole(oldRole);
                    being.addAdditionalRole(newRole);
                    hasChanged = true;
                }
                if (hasChanged) {
                    // We need to now try and unapply and re-apply all skills
                    if (skillChanged) {
                        for (ISkill skill : oldRole.getAllSkills()) {
                            if (skill instanceof Permissible) {
                                ((Permissible) skill).tryUnlearning(being);
                            }
                            if (skill instanceof Passive && being instanceof SkillCaster) {
                                ((Passive) skill).remove((SkillCaster) being);
                            }
                        }
                        for (ISkill skill : newRole.getAllSkills()) {
                            if (skill instanceof Permissible) {
                                ((Permissible) skill).tryLearning(being);
                            }
                            if (skill instanceof Passive && being instanceof SkillCaster) {
                                ((Passive) skill).apply((SkillCaster) being);
                            }
                        }
                    }
                    // Finally, clear all effects and update inventory
                    being.updateInventory();
                    being.manualClearEffects();
                    // Force update max health
                    being.recalculateMaxHealth();
                }
            }
        }
    }

    private void reconstructRoleGraph() {
        roleGraph = new DirectedGraph<Role>();
        for (Map.Entry<String, Role> entry : roleMap.entrySet()) {
            Role role = entry.getValue();
            for (Role parent : role.getParents()) {
                try {
                    roleGraph.addEdge(parent, role);
                } catch (CircularDependencyException e) {
                    plugin.getLogger().severe("Could not add a Role dependency from parent: " + parent.getName() + " to child: " + role.getName());
                    e.printStackTrace();
                }
            }
            for (Role child : role.getChildren()) {
                try {
                    roleGraph.addEdge(role, child);
                } catch (CircularDependencyException e) {
                    plugin.getLogger().severe("Could not add a Role dependency from parent: " + role.getName() + " to child: " + child.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean removeRoleDependency(Role parent, Role child) {
        Validate.notNull(parent, "Cannot remove a null Role Parent dependency!");
        Validate.notNull(child, "Cannot remove a null Role child dependency!");
        reconstructRoleGraph();
        roleGraph.removeEdge(parent, child);
        Role newParent = Role.builder(plugin).copyOf(parent).removeChild(child).build();
        Role newChild = Role.builder(plugin).copyOf(child).removeParent(newParent).build();
        this.roleMap.remove(parent.getName());
        this.roleMap.remove(child.getName());
        this.roleMap.put(newParent.getName(), newParent);
        this.roleMap.put(newChild.getName(), newChild);
        return true;
    }

    @Override
    public boolean areRoleDependenciesCyclic() {
        DirectedGraph<Role> tempGraph = new DirectedGraph<Role>();
        for (Map.Entry<String, Role> entry : roleMap.entrySet()) {
            Role role = entry.getValue();
            for (Role parent : role.getParents()) {
                try {
                    tempGraph.addEdge(parent, role);
                } catch (CircularDependencyException e) {
                    plugin.getLogger().severe("Could not add a Role dependency from parent: " + parent.getName() + " to child: " + role.getName());
                    e.printStackTrace();
                    return false;
                }
            }
            for (Role child : role.getChildren()) {
                try {
                    tempGraph.addEdge(role, child);
                } catch (CircularDependencyException e) {
                    plugin.getLogger().severe("Could not add a Role dependency from parent: " + role.getName() + " to child: " + child.getName());
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}
