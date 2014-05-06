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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.events.roles.RoleChangeEvent;
import com.afterkraft.kraftrpg.api.events.roles.RoleLevelChangeEvent;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Passive;
import com.afterkraft.kraftrpg.api.skills.PassiveSkill;
import com.afterkraft.kraftrpg.api.skills.Permissible;
import com.afterkraft.kraftrpg.api.skills.PermissionSkill;
import com.afterkraft.kraftrpg.api.skills.Skill;
import com.afterkraft.kraftrpg.api.skills.SkillArgument;
import com.afterkraft.kraftrpg.api.skills.SkillManager;
import com.afterkraft.kraftrpg.api.skills.SkillUseObject;
import com.afterkraft.kraftrpg.api.skills.Stalled;


public class RPGSkillManager extends URLClassLoader implements SkillManager {


    protected final Map<String, String> skillPermissions;
    protected final Map<String, ISkill> skillMap;
    protected final Map<String, File> skillFiles;
    protected final File skillDirectory;
    protected final KraftRPGPlugin plugin;
    private SkillManagerListener listener;

    public RPGSkillManager(KraftRPGPlugin plugin) {
        super(((URLClassLoader) plugin.getClass().getClassLoader()).getURLs(), plugin.getClass().getClassLoader());
        skillMap = new LinkedHashMap<String, ISkill>();
        skillFiles = new HashMap<String, File>();
        skillPermissions = new HashMap<String, String>();
        this.plugin = plugin;

        listener = new SkillManagerListener();

        skillDirectory = new File(plugin.getDataFolder(), "skills");
        skillDirectory.mkdir();
        loadSkillFiles();
    }

    private void loadSkillFiles() {
        for (final String skillFile : skillDirectory.list()) {
            if (skillFile.contains(".jar")) {
                final File file = new File(skillDirectory, skillFile);
                final String name = skillFile.toLowerCase().replace(".jar", "").replace("skill", "");
                if (skillFiles.containsKey(name)) {
                    plugin.log(Level.SEVERE, "Duplicate skill jar found! Please remove " + skillFile + " or " + skillFiles.get(name).getName());
                    continue;
                }
                skillFiles.put(name, file);
                try {
                    this.addURL(file.toURI().toURL());
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load all the skills.
     */
    public void initialize() {
        for (final Map.Entry<String, File> entry : skillFiles.entrySet()) {
            // if the Skill is already loaded, skip it
            if (isLoaded(entry.getKey())) {
                continue;
            }

            final Skill skill = loadSkill(entry.getValue());
            if (skill != null) {
                addSkill(skill);
                plugin.debugLog(Level.INFO, "Skill " + skill.getName() + " Loaded");
            }
        }
        plugin.getServer().getPluginManager().registerEvents(new SkillManagerListener(), plugin);
    }

    public void shutdown() {

    }

    protected Skill loadSkill(File file) {
        try {
            final JarFile jarFile = new JarFile(file);
            final Enumeration<JarEntry> entries = jarFile.entries();

            String mainClass = null;
            while (entries.hasMoreElements()) {
                final JarEntry element = entries.nextElement();
                if (element.getName().equalsIgnoreCase("skill.info")) {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
                    mainClass = reader.readLine().substring(12);
                    break;
                }
            }

            if (mainClass != null) {
                final Class<?> clazz = Class.forName(mainClass, true, this);
                final Class<? extends Skill> skillClass = clazz.asSubclass(Skill.class);
                final Constructor<? extends Skill> ctor = skillClass.getConstructor(plugin.getClass());
                final Skill skill = ctor.newInstance(plugin);
                plugin.getSkillConfigManager().loadSkillDefaults(skill);
                skill.initialize();
                jarFile.close();
                return skill;
            } else {
                jarFile.close();
                throw new IllegalArgumentException();
            }
        } catch (final NoClassDefFoundError e) {
            plugin.log(Level.WARNING, "Unable to load " + file.getName() + " skill was not written for KraftRPG!");
            plugin.debugThrow(this.getClass().toString(), "loadSkill", e);
            return null;
        } catch (final ClassNotFoundException e) {
            plugin.log(Level.WARNING, "Unable to load " + file.getName() + " skill was not written for KraftRPG!");
            plugin.debugThrow(this.getClass().toString(), "loadSkill", e);
            return null;
        } catch (final IllegalArgumentException e) {
            plugin.log(Level.SEVERE, "Could not detect the proper Skill class to load for: " + file.getName());
            return null;
        } catch (final Exception e) {
            plugin.log(Level.INFO, "The skill " + file.getName() + " failed to load for an unknown reason.");
            plugin.debugThrow(this.getClass().getName(), "loadSkill", e);
            return null;
        }
    }

    public void addSkill(ISkill skill) {
        skillMap.put(skill.getName().toLowerCase().replace("skill", ""), skill);
        if (skill instanceof Permissible || skill instanceof Passive) {
            this.listener.addSkill(skill);
        }
    }

    public ISkill getSkill(String name) {
        if (name == null) {
            return null;
        }
        name = name.toLowerCase();
        // Only attempt to load files that exist
        if (!isLoaded(name) && skillFiles.containsKey(name)) {
            loadSkill(name);
        }
        return skillMap.get(name);
    }

    private boolean loadSkill(String name) {
        // If the skill is already loaded, don't try to load it
        if (isLoaded(name)) {
            return true;
        }

        // Lets try loading the skill file
        final Skill skill = loadSkill(skillFiles.get(name.toLowerCase()));
        if (skill == null) {
            return false;
        }

        addSkill(skill);
        return true;
    }

    public boolean loadOutsourcedSkill(String name) {
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
    public Stalled<? extends SkillArgument> getDelayedSkill(SkillCaster caster) {
        return null;
    }

    @Override
    public void setCompletedSkill(SkillCaster caster) {

    }

    @Override
    public void addSkillTarget(Entity o, SkillCaster caster, ISkill skill) {

    }

    @Override
    public SkillUseObject<? extends SkillArgument> getSkillTargetInfo(Entity o) {
        return null;
    }

    @Override
    public boolean isSkillTarget(Entity o) {
        return false;
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
                skill.tryApplying(event.getSentientBeing());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onLevelChangeEvent(RoleLevelChangeEvent event) {
            for (PermissionSkill skill : permissionSkills) {
                skill.tryLearning(event.getSentientBeing());
            }
            for (PassiveSkill skill : passiveSkills) {
                skill.tryApplying(event.getSentientBeing());
            }
        }
    }
}
