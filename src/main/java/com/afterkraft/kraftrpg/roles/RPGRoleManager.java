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
package com.afterkraft.kraftrpg.roles;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.CircularDependencyException;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.Role.RoleType;
import com.afterkraft.kraftrpg.api.roles.RoleManager;
import com.afterkraft.kraftrpg.api.roles.aspects.SkillAspect;
import com.afterkraft.kraftrpg.api.skills.Skill;
import com.afterkraft.kraftrpg.api.skills.Passive;
import com.afterkraft.kraftrpg.api.skills.common.Permissible;
import com.afterkraft.kraftrpg.api.util.DirectedGraph;
import com.afterkraft.kraftrpg.api.util.FixedPoint;

/**
 * Default implementation of RoleManager
 */
public class RPGRoleManager implements RoleManager {

    private static File rolesDirectory;
    private final KraftRPGPlugin plugin;
    private final Map<String, Role> roleMap;
    private Role defaultPrimaryRole;
    @Nullable
    private Role defaultSecondaryRole;
    private DirectedGraph<Role> roleGraph = new DirectedGraph<>();
    private Map<Role, FixedPoint[]> roleLevels;

    public RPGRoleManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.roleMap = Maps.newHashMap();
        this.roleLevels = Maps.newHashMap();
    }

    @Override
    public Role getDefaultPrimaryRole() {
        return this.defaultPrimaryRole;
    }

    @Override
    public boolean setDefaultPrimaryRole(Role role) {
        checkNotNull(role, "Cannot set the a default Primary null Role!");
        checkArgument(role.getType() == RoleType.PRIMARY,
                "Cannot have a non Primary RoleType as the default Primary "
                        + "Role!");
        this.defaultPrimaryRole = role;
        return true;
    }

    @Override
    public Optional<Role> getDefaultSecondaryRole() {
        return Optional.fromNullable(this.defaultSecondaryRole);
    }

    @Override
    public void setDefaultSecondaryRole(@Nullable Role role) {
        if (role != null) {
            checkArgument(role.getType() == RoleType.SECONDARY,
                    "Cannot have a non Secondary RoleType "
                            + "as the default Secondary Role!");
        }
        this.defaultSecondaryRole = role;
    }

    @Override
    public Optional<Role> getRole(String roleName) {
        return Optional.fromNullable(this.roleMap.get(roleName));
    }

    @Override
    public boolean addRole(Role role) {
        checkNotNull(role);
        if (!this.roleMap.containsKey(role.getName())) {
            return false;
        }
        this.roleMap.put(role.getName(), role);
        this.roleGraph.addVertex(role);
        if (!role.getChildren().isEmpty() || !role.getParents().isEmpty()) {
            for (Role child : role.getChildren()) {
                this.roleGraph.addEdge(child, role);
            }
            for (Role parent : role.getParents()) {
                this.roleGraph.addEdge(role, parent);
            }
        }
        return true;
    }

    @Override
    public boolean removeRole(Role role) {
        checkNotNull(role);
        if (!this.roleMap.containsKey(role.getName())) {
            return true;
        }
        this.roleGraph.removeVertex(role);
        this.roleMap.remove(role.getName());
        return false;
    }

    @Override
    public List<Role> getRoles() {
        return Lists.newArrayList();
    }

    @Override
    public List<Role> getRolesByType(Role.RoleType type) {
        return Lists.newArrayList();
    }

    @Override
    public FixedPoint getRoleLevelExperience(Role role, int level) {
        checkNotNull(role, "Cannot calculate the experience for a null role!");
        checkArgument(this.roleLevels.containsKey(role),
                "Cannot return the experience requirement for a role "
                        + "that isn't registered with the system!");
        checkArgument(level <= role.getMaxLevel(),
                "Cannot return the experience requirement for a level "
                        + "above the max level for the role!");
        checkArgument(level > 0,
                "Cannot get the experience requirement for a negative level!");
        return this.roleLevels.get(role)[level - 1];
    }

    @Override
    public boolean addRoleDependency(Role parent, Role child) {
        checkNotNull(parent, "Cannot add a null Role Parent dependency!");
        checkNotNull(child, "Cannot add a null Role child dependency!");
        reconstructRoleGraph();
        this.roleGraph.addEdge(parent, child);
        Role newParent = Role.copyOf(parent).addChild(child).build();
        Role newChild = Role.copyOf(child).addParent(newParent).build();
        this.roleMap.remove(parent.getName());
        this.roleMap.remove(child.getName());
        this.roleMap.put(newParent.getName(), newParent);
        this.roleMap.put(newChild.getName(), newChild);
        swapRoles(parent, newParent);
        swapRoles(child, newChild);
        return true;
    }

    @Override
    public boolean removeRoleDependency(Role parent, Role child) {
        checkNotNull(parent,
                "Cannot remove a null Role Parent dependency!");
        checkNotNull(child,
                "Cannot remove a null Role child dependency!");
        reconstructRoleGraph();
        this.roleGraph.removeEdge(parent, child);
        Role newParent = Role.copyOf(parent).removeChild(child).build();
        Role newChild = Role.copyOf(child).removeParent(newParent).build();
        this.roleMap.remove(parent.getName());
        this.roleMap.remove(child.getName());
        this.roleMap.put(newParent.getName(), newParent);
        this.roleMap.put(newChild.getName(), newChild);
        return true;
    }

    @Override
    public boolean areRoleDependenciesCyclic() {
        DirectedGraph<Role> tempGraph = new DirectedGraph<>();
        for (Map.Entry<String, Role> entry : this.roleMap.entrySet()) {
            Role role = entry.getValue();
            for (Role parent : role.getParents()) {
                try {
                    tempGraph.addEdge(parent, role);
                } catch (CircularDependencyException e) {
                    this.plugin.getLogger()
                            .error("Could not add a Role dependency from "
                                    + "parent: "
                                    + parent
                                    .getName() + " to child: " + role
                                    .getName());
                    e.printStackTrace();
                    return false;
                }
            }
            for (Role child : role.getChildren()) {
                try {
                    tempGraph.addEdge(role, child);
                } catch (CircularDependencyException e) {
                    this.plugin.getLogger()
                            .error("Could not add a Role dependency from "
                                    + "parent: "
                                    + role.getName()
                                    + " to child: " + child.getName());
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    private void reconstructRoleGraph() {
        this.roleGraph = new DirectedGraph<>();
        for (Map.Entry<String, Role> entry : this.roleMap.entrySet()) {
            Role role = entry.getValue();
            for (Role parent : role.getParents()) {
                try {
                    this.roleGraph.addEdge(parent, role);
                } catch (CircularDependencyException e) {
                    this.plugin.getLogger()
                            .error("Could not add a Role dependency from "
                                    + "parent: "
                                    + "" + parent
                                    .getName() + " to child: " + role
                                    .getName());
                    e.printStackTrace();
                }
            }
            for (Role child : role.getChildren()) {
                try {
                    this.roleGraph.addEdge(role, child);
                } catch (CircularDependencyException e) {
                    this.plugin.getLogger()
                            .error("Could not add a Role dependency from "
                                    + "parent: "
                                    + role.getName()
                                    + " to child: " + child.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    public void swapRoles(Role oldRole, Role newRole) {
        checkNotNull(oldRole);
        checkNotNull(newRole);
        Optional<SkillAspect> skillAspectOptional = oldRole
                .getAspect(SkillAspect.class);
        Optional<SkillAspect> newSkillAspectOptional = newRole
                .getAspect(SkillAspect.class);
        boolean skillChanged = false;
        if (skillAspectOptional.isPresent() && newSkillAspectOptional
                .isPresent()) {
            skillChanged = skillAspectOptional.get().getAllSkills().size() ==
                    newSkillAspectOptional.get().getAllSkills().size();
        }
        /*
        if (this.plugin.getEntityManager() instanceof RPGEntityManager) {
            RPGEntityManager entityManager =
                    (RPGEntityManager) this.plugin.getEntityManager();
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
                        for (Skill skill : skillAspectOptional.get()
                                .getAllSkills
                                        ()) {
                            if (skill instanceof Permissible) {
                                ((Permissible) skill).tryUnlearning(being);
                            }
                            if (skill instanceof Passive
                                    && being instanceof SkillCaster) {
                                ((Passive) skill).remove((SkillCaster) being);
                            }
                        }
                        for (Skill skill : newSkillAspectOptional.get()
                                .getAllSkills()) {
                            if (skill instanceof Permissible) {
                                ((Permissible) skill).tryLearning(being);
                            }
                            if (skill instanceof Passive
                                    && being instanceof SkillCaster) {
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
        */
    }

    @Override
    public void initialize() {
        RPGRoleManager.rolesDirectory = new File(this.plugin
                .getConfigurationManager()
                .getConfigDirectory() + File.separator + "roles");
        if (!RPGRoleManager.rolesDirectory.exists()) {
            RPGRoleManager.rolesDirectory.mkdirs();
            this.plugin.getConfigurationManager()
                    .checkForConfig(new File(RPGRoleManager.rolesDirectory, "admin.hocon"));
            this.plugin.getConfigurationManager()
                    .checkForConfig(new File(RPGRoleManager.rolesDirectory, "weakling.hocon"));
            this.plugin.getConfigurationManager()
                    .checkForConfig(
                            new File(RPGRoleManager.rolesDirectory, "swordsman.hocon"));
            this.plugin.getConfigurationManager()
                    .checkForConfig(new File(RPGRoleManager.rolesDirectory, "healer.hocon"));
            this.plugin.getConfigurationManager()
                    .checkForConfig(new File(RPGRoleManager.rolesDirectory, "mage.hocon"));
            this.plugin.getConfigurationManager()
                    .checkForConfig(new File(RPGRoleManager.rolesDirectory, "archer.hocon"));
        }
    }

    @Override
    public void shutdown() {

    }

    private void setupRoleLevels() {
        for (Role role : this.roleMap.values()) {
            final int maxLevel = role.getMaxLevel();
            FixedPoint[] levels = new FixedPoint[maxLevel + 1];
            final double timePerLevelModulo = 100;
            final double expPerTime = 10;
            for (int i = 0; i < (maxLevel + 1); i++) {
                double timePerLevel = Math.pow(i, timePerLevelModulo);
                double expPerMinute = Math.pow(i, expPerTime);
                levels[i] = FixedPoint.valueOf(timePerLevel * expPerMinute);
            }
            this.roleLevels.put(role, levels);
        }
    }
}
