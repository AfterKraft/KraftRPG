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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.Validate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.CircularDependencyException;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.roles.ExperienceType;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.RoleManager;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Passive;
import com.afterkraft.kraftrpg.api.skills.Permissible;
import com.afterkraft.kraftrpg.api.util.DirectedGraph;
import com.afterkraft.kraftrpg.entity.RPGEntityManager;


public class RPGRoleManager implements RoleManager {

    private static File rolesDirectory;
    private final KraftRPGPlugin plugin;
    private final Map<String, Role> roleMap;
    private Role defaultPrimaryRole;
    private Role defaultSecondaryRole;
    private DirectedGraph<Role> roleGraph = new DirectedGraph<Role>();

    public RPGRoleManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.roleMap = new HashMap<String, Role>();
        rolesDirectory = new File(plugin.getDataFolder() + File.separator + "roles");

    }

    @Override
    public Role getDefaultPrimaryRole() {
        return this.defaultPrimaryRole;
    }

    @Override
    public boolean setDefaultPrimaryRole(Role role) {
        Validate.notNull(role, "Cannot set the a default Primary null Role!");
        Validate.isTrue(role.getType() == Role.RoleType.PRIMARY, "Cannot have a non Primary RoleType as the default Primary Role!");
        this.defaultPrimaryRole = role;
        return true;
    }

    @Override
    public Role getDefaultSecondaryRole() {
        return this.defaultSecondaryRole;
    }

    @Override
    public void setDefaultSecondaryRole(Role role) {
        Validate.notNull(role, "Cannot set the a default secondary null Role!");
        Validate.isTrue(role.getType() == Role.RoleType.SECONDARY, "Cannot have a non Secondary RoleType as the default Secondary Role!");
        this.defaultSecondaryRole = role;
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
        if (!role.getChildren().isEmpty() || !role.getParents().isEmpty()) {
            for (Role child : role.getChildren()) {
                roleGraph.addEdge(child, role);
            }
            for (Role parent : role.getParents()) {
                roleGraph.addEdge(role, parent);
            }
        }
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
        return ImmutableMap.copyOf(roleMap);
    }

    @Override
    public Map<String, Role> getRolesByType(Role.RoleType type) {
        Validate.notNull(type, "Cannot get Roles by type of a null RoleType!");
        ImmutableMap.Builder<String, Role> builder = ImmutableMap.builder();
        for (Map.Entry<String, Role> entry : roleMap.entrySet()) {
            if (entry.getValue().getType() == type)
                builder.put(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    @Override
    public boolean addRoleDependency(Role parent, Role child) {
        Validate.notNull(parent, "Cannot add a null Role Parent dependency!");
        Validate.notNull(child, "Cannot add a null Role child dependency!");
        reconstructRoleGraph();
        roleGraph.addEdge(parent, child);
        Role newParent = Role.Builder.copyOf(parent).addChild(child).build();
        Role newChild = Role.Builder.copyOf(child).addParent(newParent).build();
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
        Validate.notNull(parent, "Cannot remove a null Role Parent dependency!");
        Validate.notNull(child, "Cannot remove a null Role child dependency!");
        reconstructRoleGraph();
        roleGraph.removeEdge(parent, child);
        Role newParent = Role.Builder.copyOf(parent).removeChild(child).build();
        Role newChild = Role.Builder.copyOf(child).removeParent(newParent).build();
        this.roleMap.remove(parent.getName());
        this.roleMap.remove(child.getName());
        this.roleMap.put(newParent.getName(), newParent);
        this.roleMap.put(newChild.getName(), newChild);
        return true;
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
        if (!rolesDirectory.exists()) {
            rolesDirectory.mkdirs();
            plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "admin.yml"));
            plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "weakling.yml"));
            plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "swordsman.yml"));
            plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "healer.yml"));
            plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "mage.yml"));
            plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "archer.yml"));
        }
        File[] roleFiles = rolesDirectory.listFiles();
        List<Role> wildCarded = new ArrayList<Role>();
        if (roleFiles == null) {
            plugin.debugLog(Level.SEVERE, "KraftRPG is unable to find the roles directory!");
        } else {
            List<Configuration> roleConfigurations = new ArrayList<Configuration>();
            for (final File roleFile : roleFiles) {
                if (roleFile.isFile() && roleFile.getName().endsWith(".yml")) {
                    YamlConfiguration roleYmlConfig = YamlConfiguration.loadConfiguration(roleFile);
                    Role role = loadRoleWithoutDependencies(roleYmlConfig);
                    if (role == null) {
                        plugin.debugLog(Level.WARNING, "Could not load the role: " + roleFile.getName() + "! Skipping!");
                        continue;
                    }
                    if (!addRole(role)) {
                        plugin.debugLog(Level.WARNING, "A Role: " + role.getName() + " could not be added to the RoleManager!");
                    }
                    if (hasNoDependencies(roleYmlConfig)) {

                        // By virtue of Roles, a default Role can not have any dependencies
                        if (roleYmlConfig.getBoolean("default-primary") && role.getType() == Role.RoleType.PRIMARY) {
                            if (defaultPrimaryRole == null) {
                                defaultPrimaryRole = role;
                            } else {
                                plugin.debugLog(Level.WARNING, "Cannot have multiple default Primary Roles!");
                            }
                        }
                        if (roleYmlConfig.getBoolean("default-secondary") && role.getType() == Role.RoleType.SECONDARY) {
                            if (defaultSecondaryRole == null) {
                                defaultSecondaryRole = role;
                            } else {
                                plugin.debugLog(Level.WARNING, "Cannot have multiple default Primary Roles!");
                            }
                        }
                    } else {
                        addRole(role);
                        roleConfigurations.add(roleYmlConfig);
                    }
                    if (roleYmlConfig.getBoolean("included-in-wildcard", false)) {
                        wildCarded.add(getRole(roleYmlConfig.getString("name")));
                    }
                }
            }
            if (!roleConfigurations.isEmpty()) {
                for (Configuration roleConfig : roleConfigurations) {
                    Role role = getRole(roleConfig.getString("name"));
                    if (roleConfig.getStringList("parents") != null && !roleConfig.getShortList("parents").isEmpty()) {
                        for (String parentName : roleConfig.getStringList("parents")) {
                            Role parent = getRole(parentName);
                            if (parent != null) {
                                addRoleDependency(parent, role);
                            }
                        }
                    }
                }
            }
            setupRolePermissions(wildCarded);
        }
    }

    private Role loadRoleWithoutDependencies(Configuration configuration) {
        Role.Builder roleBuilder = Role.builder(plugin);
        // Get the basics
        roleBuilder.setName(configuration.getString("name"));
        roleBuilder.setDescription(configuration.getString("description", ""));
        roleBuilder.setChoosable(configuration.getBoolean("choosable", true));
        roleBuilder.setManaName(configuration.getString("mana-name", "mana"));
        roleBuilder.setType(Role.RoleType.valueOf(configuration.getString("role-type")));
        roleBuilder.setHpAt0(configuration.getDouble("max-health-at-zero", 100.0D));
        roleBuilder.setHpPerLevel(configuration.getDouble("max-health-increase-per-level", 10.0D));
        roleBuilder.setMpAt0(configuration.getInt("max-mana-at-zero", 100));
        roleBuilder.setMpRegenAt0(configuration.getInt("mana-regen-at-zero", 1));
        roleBuilder.setMpPerLevel(configuration.getInt("max-mana-increase-per-level", 0));
        roleBuilder.setMpRegenPerLevel(configuration.getInt("mana-regen-increase-per-level", 0));
        roleBuilder.setAdvancementLevel(configuration.getInt("advancement-level", 20));

        // Set the maps/lists
        ConfigurationSection damageSection = configuration.getConfigurationSection("item-damages");
        if (damageSection != null) {
            Set<String> itemDamages = damageSection.getKeys(false);
            if (itemDamages != null && !itemDamages.isEmpty()) {
                for (String materialName : itemDamages) {
                    Material material = Material.matchMaterial(materialName);
                    if (material != null) {
                        roleBuilder.setItemDamage(material, damageSection.getDouble(materialName));
                    }
                }
            }
        }

        ConfigurationSection damagePerLevelSection = configuration.getConfigurationSection("item-damages-per-level");
        if (damagePerLevelSection != null) {
            Set<String> itemDamages = damagePerLevelSection.getKeys(false);
            if (itemDamages != null && !itemDamages.isEmpty()) {
                for (String materialName : itemDamages) {
                    Material material = Material.matchMaterial(materialName);
                    if (material != null) {
                        roleBuilder.setItemDamagePerLevel(material, damagePerLevelSection.getDouble(materialName));
                    }
                }
            }
        }

        ConfigurationSection damageVariesSection = configuration.getConfigurationSection("item-damages-vary");
        if (damageVariesSection != null) {
            Set<String> itemDamages = damageVariesSection.getKeys(false);
            if (itemDamages != null && !itemDamages.isEmpty()) {
                for (String materialName : itemDamages) {
                    Material material = Material.matchMaterial(materialName);
                    if (material != null) {
                        roleBuilder.setItemDamageVaries(material, damageVariesSection.getBoolean(materialName));
                    }
                }
            }
        }

        List<String> experienceTypes = configuration.getStringList("experience-types");
        if (experienceTypes != null) {
            for (String experienceName : experienceTypes) {
                ExperienceType experienceType = ExperienceType.valueOf(experienceName);
                if (experienceType != null) {
                    roleBuilder.addExperienceType(experienceType);
                }
            }
        }

        List<String> restrictedSkills = configuration.getStringList("restricted-skills");
        if (restrictedSkills != null && !restrictedSkills.isEmpty()) {
            for (String skillName : restrictedSkills) {
                roleBuilder.addRestirctedSkill(plugin.getSkillManager().getSkill(skillName));
            }
        }

        ConfigurationSection allowedSkills = configuration.getConfigurationSection("allowed-skills");
        if (allowedSkills != null) {
            boolean allowAllSkills = false;
            for (String skillName : allowedSkills.getKeys(false)) {
                if (skillName.equals("*") || skillName.equalsIgnoreCase("all")) {
                    allowAllSkills = true;
                    continue;
                }
                final ISkill skill = plugin.getSkillManager().getSkill(skillName);
                if (skill == null) {
                    plugin.debugLog(Level.WARNING, "A Skill:" + skillName + " configured for the Role: " + configuration.getString("name") + " does not exist!");
                    continue;
                }
                ConfigurationSection skillSettings = configuration.getConfigurationSection("allowed-skills." + skillName);
                if (skillSettings == null) {
                    skillSettings = configuration.createSection("skills." + skillName);
                }
                roleBuilder.addRoleSkill(skill, skillSettings);
                plugin.getSkillConfigManager().addClassSkillSettings(configuration.getString("name"), skillName, skillSettings);
            }

            if (allowAllSkills) {
                for (final ISkill skill : plugin.getSkillManager().getSkills()) {
                    if (skill instanceof Permissible) {
                        continue;
                    }
                    ConfigurationSection skillSettings = configuration.getConfigurationSection(skill.getName());
                    if (skillSettings == null) {
                        skillSettings = configuration.createSection(skill.getName());
                    }
                    roleBuilder.addRoleSkill(skill, skillSettings);
                }
            }
        }

        return roleBuilder.build();
    }

    private boolean hasNoDependencies(Configuration configuration) {
        return configuration.getList("parents") != null && configuration.getList("parents").isEmpty();
    }

    private void setupRolePermissions(List<Role> wildcarded) {
        // Set up all roles first
        for (Role role : getRoles().values()) {
            final Permission permission = new Permission("kraftrpg.roles." + role.getName().toLowerCase(), PermissionDefault.OP);
            Bukkit.getServer().getPluginManager().addPermission(permission);
        }
        // Now set up the wildcarded roles
        final Map<String, Boolean> wildcardRolePermissions = new HashMap<String, Boolean>();
        for (Role wildcardedRole : wildcarded) {
            wildcardRolePermissions.put("kraftrpg.roles." + wildcardedRole.getName().toLowerCase(), true);
        }
        final Permission wildcardClassPermission = new Permission("kraftrpg.roles.*", "Grants access to all roles.", PermissionDefault.OP, wildcardRolePermissions);
        plugin.getServer().getPluginManager().addPermission(wildcardClassPermission);

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

    @Override
    public void shutdown() {

    }
}
