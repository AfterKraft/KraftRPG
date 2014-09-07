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

import com.afterkraft.kraftrpg.api.CircularDependencyException;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.roles.ExperienceType;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.Role.RoleType;
import com.afterkraft.kraftrpg.api.roles.RoleManager;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Passive;
import com.afterkraft.kraftrpg.api.skills.common.Permissible;
import com.afterkraft.kraftrpg.api.util.DirectedGraph;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.entity.RPGEntityManager;


public class RPGRoleManager implements RoleManager {

    private static File rolesDirectory;
    private final RPGPlugin plugin;
    private final Map<String, Role> roleMap;
    private Role defaultPrimaryRole;
    private Role defaultSecondaryRole;
    private DirectedGraph<Role> roleGraph = new DirectedGraph<Role>();
    private Map<Role, FixedPoint[]> roleLevels = new HashMap<Role, FixedPoint[]>();

    public RPGRoleManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.roleMap = new HashMap<String, Role>();
        rolesDirectory = new File(plugin.getDataFolder() + File.separator + "roles");

    }

    @Override
    public FixedPoint getRoleLevelExperience(Role role, int level) {
        Validate.notNull(role, "Cannot calculate the experience for a null role!");
        Validate.isTrue(this.roleLevels.containsKey(role), "Cannot return the experience requirement for a role that isn't registered with the system!");
        Validate.isTrue(level <= role.getMaxLevel(), "Cannot return the experience requirement for a level above the max level for the role!");
        Validate.isTrue(level > 0, "Cannot get the experience requirement for a negative level!");
        return this.roleLevels.get(role)[level -1];
    }

    @Override
    public Role getDefaultPrimaryRole() {
        return this.defaultPrimaryRole;
    }

    @Override
    public boolean setDefaultPrimaryRole(Role role) {
        Validate.notNull(role, "Cannot set the a default Primary null Role!");
        Validate.isTrue(role.getType() == RoleType.PRIMARY, "Cannot have a non Primary RoleType as the default Primary Role!");
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
        Validate.isTrue(role.getType() == RoleType.SECONDARY, "Cannot have a non Secondary RoleType as the default Secondary Role!");
        this.defaultSecondaryRole = role;
    }

    @Override
    public Role getRole(String roleName) {
        return this.roleMap.get(roleName);
    }

    @Override
    public boolean addRole(Role role) {
        if (role == null || !this.roleMap.containsKey(role.getName())) {
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

        if (role == null || !this.roleMap.containsKey(role.getName())) {
            return true;
        }
        this.roleGraph.removeVertex(role);
        this.roleMap.remove(role.getName());
        return false;
    }

    @Override
    public Map<String, Role> getRoles() {
        return ImmutableMap.copyOf(this.roleMap);
    }

    @Override
    public Map<String, Role> getRolesByType(Role.RoleType type) {
        Validate.notNull(type, "Cannot get Roles by type of a null RoleType!");
        ImmutableMap.Builder<String, Role> builder = ImmutableMap.builder();
        for (Map.Entry<String, Role> entry : this.roleMap.entrySet()) {
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
        Validate.notNull(parent, "Cannot remove a null Role Parent dependency!");
        Validate.notNull(child, "Cannot remove a null Role child dependency!");
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

    private void reconstructRoleGraph() {
        this.roleGraph = new DirectedGraph<Role>();
        for (Map.Entry<String, Role> entry : this.roleMap.entrySet()) {
            Role role = entry.getValue();
            for (Role parent : role.getParents()) {
                try {
                    this.roleGraph.addEdge(parent, role);
                } catch (CircularDependencyException e) {
                    this.plugin.getLogger().severe("Could not add a Role dependency from parent: " + parent.getName() + " to child: " + role.getName());
                    e.printStackTrace();
                }
            }
            for (Role child : role.getChildren()) {
                try {
                    this.roleGraph.addEdge(role, child);
                } catch (CircularDependencyException e) {
                    this.plugin.getLogger().severe("Could not add a Role dependency from parent: " + role.getName() + " to child: " + child.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean areRoleDependenciesCyclic() {
        DirectedGraph<Role> tempGraph = new DirectedGraph<Role>();
        for (Map.Entry<String, Role> entry : this.roleMap.entrySet()) {
            Role role = entry.getValue();
            for (Role parent : role.getParents()) {
                try {
                    tempGraph.addEdge(parent, role);
                } catch (CircularDependencyException e) {
                    this.plugin.getLogger().severe("Could not add a Role dependency from parent: " + parent.getName() + " to child: " + role.getName());
                    e.printStackTrace();
                    return false;
                }
            }
            for (Role child : role.getChildren()) {
                try {
                    tempGraph.addEdge(role, child);
                } catch (CircularDependencyException e) {
                    this.plugin.getLogger().severe("Could not add a Role dependency from parent: " + role.getName() + " to child: " + child.getName());
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
            this.plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "admin.yml"));
            this.plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "weakling.yml"));
            this.plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "swordsman.yml"));
            this.plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "healer.yml"));
            this.plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "mage.yml"));
            this.plugin.getConfigurationManager().checkForConfig(new File(rolesDirectory, "archer.yml"));
        }
        File[] roleFiles = rolesDirectory.listFiles();
        List<Role> wildCarded = new ArrayList<Role>();
        if (roleFiles == null) {
            this.plugin.debugLog(Level.SEVERE, "KraftRPG is unable to find the roles directory!");
        } else {
            List<Configuration> roleConfigurations = new ArrayList<Configuration>();
            for (final File roleFile : roleFiles) {
                if (roleFile.isFile() && roleFile.getName().endsWith(".yml")) {
                    YamlConfiguration roleYmlConfig = YamlConfiguration.loadConfiguration(roleFile);
                    Role role = loadRoleWithoutDependencies(roleYmlConfig);
                    if (role == null) {
                        this.plugin.debugLog(Level.WARNING, "Could not load the role: " + roleFile.getName() + "! Skipping!");
                        continue;
                    }
                    if (!addRole(role)) {
                        this.plugin.debugLog(Level.WARNING, "A Role: " + role.getName() + " could not be added to the RoleManager!");
                    }
                    if (hasNoDependencies(roleYmlConfig)) {

                        // By virtue of Roles, a default Role can not have any dependencies
                        if (roleYmlConfig.getBoolean("default-primary") && role.getType() == RoleType.PRIMARY) {
                            if (this.defaultPrimaryRole == null) {
                                this.defaultPrimaryRole = role;
                            } else {
                                this.plugin.debugLog(Level.WARNING, "Cannot have multiple default Primary Roles!");
                            }
                        }
                        if (roleYmlConfig.getBoolean("default-secondary") && role.getType() == RoleType.SECONDARY) {
                            if (this.defaultSecondaryRole == null) {
                                this.defaultSecondaryRole = role;
                            } else {
                                this.plugin.debugLog(Level.WARNING, "Cannot have multiple default Primary Roles!");
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
                    if (roleConfig.getStringList("parents") != null && !roleConfig.getShortList("parents").isEmpty()) {
                        for (String parentName : roleConfig.getStringList("parents")) {
                            Role parent = getRole(parentName);
                            if (parent != null) {
                                addRoleDependency(parent, getRole(roleConfig.getString("name")));
                            }
                        }
                    }
                }
            }
            setupRolePermissions(wildCarded);
            setupRoleLevels();
        }
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

    private Role loadRoleWithoutDependencies(Configuration configuration) {
        Role.Builder roleBuilder = Role.builder(this.plugin);
        // Get the basics
        roleBuilder.setName(configuration.getString("name"));
        roleBuilder.setDescription(configuration.getString("description", ""));
        roleBuilder.setChoosable(configuration.getBoolean("choosable", true));
        roleBuilder.setManaName(configuration.getString("mana-name", "mana"));
        roleBuilder.setType(RoleType.valueOf(configuration.getString("role-type")));
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
                roleBuilder.addRestirctedSkill(this.plugin.getSkillManager().getSkill(skillName));
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
                final ISkill skill = this.plugin.getSkillManager().getSkill(skillName);
                if (skill == null) {
                    this.plugin.debugLog(Level.WARNING, "A Skill:" + skillName + " configured for the Role: " + configuration.getString("name") + " does not exist!");
                    continue;
                }
                ConfigurationSection skillSettings = configuration.getConfigurationSection("allowed-skills." + skillName);
                if (skillSettings == null) {
                    skillSettings = configuration.createSection("skills." + skillName);
                }
                roleBuilder.addRoleSkill(skill, skillSettings);
                this.plugin.getSkillConfigManager().addRoleSkillSettings(configuration.getString("name"), skillName, skillSettings);
            }

            if (allowAllSkills) {
                for (final ISkill skill : this.plugin.getSkillManager().getSkills()) {
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
        this.plugin.getServer().getPluginManager().addPermission(wildcardClassPermission);

    }

    public void swapRoles(Role oldRole, Role newRole) {
        boolean skillChanged = oldRole.getAllSkills().size() == newRole.getAllSkills().size();
        if (this.plugin.getEntityManager() instanceof RPGEntityManager) {
            RPGEntityManager entityManager = (RPGEntityManager) this.plugin.getEntityManager();
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
