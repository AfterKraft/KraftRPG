package com.afterkraft.kraftrpg.spells;

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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.events.entity.ChampionLevelChangeEvent;
import com.afterkraft.kraftrpg.api.events.entity.ChampionRoleChangeEvent;
import com.afterkraft.kraftrpg.api.spells.Passive;
import com.afterkraft.kraftrpg.api.spells.PassiveSpell;
import com.afterkraft.kraftrpg.api.spells.Permissible;
import com.afterkraft.kraftrpg.api.spells.PermissionSpell;
import com.afterkraft.kraftrpg.api.spells.Spell;
import com.afterkraft.kraftrpg.api.spells.SpellArgument;
import com.afterkraft.kraftrpg.api.spells.SpellManager;

/**
 * @author gabizou
 */
public class RPGSpellManager extends URLClassLoader implements SpellManager {


    protected final Map<String, String> spellPermissions;
    protected final Map<String, Spell<? extends SpellArgument>> spellMap;
    protected final Map<String, File> spellFiles;
    protected final File spellDirectory;
    protected final KraftRPGPlugin plugin;
    private SpellManagerListener listener;

    public RPGSpellManager(KraftRPGPlugin plugin) {
        super(((URLClassLoader) plugin.getClass().getClassLoader()).getURLs(), plugin.getClass().getClassLoader());
        spellMap = new LinkedHashMap<String, Spell<? extends SpellArgument>>();
        spellFiles = new HashMap<String, File>();
        spellPermissions = new HashMap<String, String>();
        this.plugin = plugin;

        listener = new SpellManagerListener();

        spellDirectory = new File(plugin.getDataFolder(), "spells");
        spellDirectory.mkdir();

        for (final String spellFile : spellDirectory.list()) {
            if (spellFile.contains(".jar")) {
                final File file = new File(spellDirectory, spellFile);
                final String name = spellFile.toLowerCase().replace(".jar", "").replace("spell", "");
                if (spellFiles.containsKey(name)) {
                    plugin.log(Level.SEVERE, "Duplicate spell jar found! Please remove " + spellFile + " or " + spellFiles.get(name).getName());
                    continue;
                }
                spellFiles.put(name, file);
                try {
                    this.addURL(file.toURI().toURL());
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load all the spells.
     */
    public void initialize() {
        for (final Map.Entry<String, File> entry : spellFiles.entrySet()) {
            // if the Spell is already loaded, skip it
            if (isLoaded(entry.getKey())) {
                continue;
            }

            final Spell<? extends SpellArgument> spell = loadSpell(entry.getValue());
            if (spell != null) {
                addSpell(spell);
                plugin.debugLog(Level.INFO, "Spell " + spell.getName() + " Loaded");
            }
        }
        plugin.getServer().getPluginManager().registerEvents(new SpellManagerListener(), plugin);
    }

    public void shutdown() {

    }

    public void addSpell(Spell<? extends SpellArgument> spell) {
        spellMap.put(spell.getName().toLowerCase().replace("spell", ""), spell);
        if (spell instanceof Permissible || spell instanceof Passive) {
            this.listener.addSpell(spell);
        }
    }

    public Spell<? extends SpellArgument> getSpell(String name) {
        if (name == null) {
            return null;
        }
        name = name.toLowerCase();
        // Only attempt to load files that exist
        if (!isLoaded(name) && spellFiles.containsKey(name)) {
            loadSpell(name);
        }
        return spellMap.get(name);
    }

    public boolean loadOutsourcedSpell(String name) {
        if ((name == null) || (spellMap.get(name.toLowerCase()) != null)) {
            return true;
        }

        final PermissionSpell oSpell = new PermissionSpell(plugin, name);
        final ConfigurationSection config = RPGSpellConfigManager.outsourcedSpellConfig.getConfigurationSection(oSpell.getName());
        final Map<String, Boolean> perms = new HashMap<String, Boolean>();
        if (config != null) {
            final ConfigurationSection permConfig = config.getConfigurationSection("permissions");
            for (String key : permConfig.getKeys(true)) {
                perms.put(key, permConfig.getBoolean(key));
            }
            oSpell.setUsage(config.getString("usage", ""));
            oSpell.setDescription(config.getString("usage"));
        }
        if (perms.isEmpty()) {
            plugin.log(Level.SEVERE, "There are no permissions defined for " + oSpell.getName());
            return false;
        }
        oSpell.setPermissions(perms);
        spellMap.put(name.toLowerCase(), (PermissionSpell<? extends SpellArgument>) oSpell);
        return true;
    }

    public Collection<Spell<? extends SpellArgument>> getSpells() {
        return Collections.unmodifiableCollection(spellMap.values());
    }

    public boolean isLoaded(String name) {
        return spellMap.containsKey(name.toLowerCase());
    }

    protected Spell<? extends SpellArgument> loadSpell(File file) {
        try {
            final JarFile jarFile = new JarFile(file);
            final Enumeration<JarEntry> entries = jarFile.entries();

            String mainClass = null;
            while (entries.hasMoreElements()) {
                final JarEntry element = entries.nextElement();
                if (element.getName().equalsIgnoreCase("spell.info")) {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
                    mainClass = reader.readLine().substring(12);
                    break;
                }
            }

            if (mainClass != null) {
                final Class<?> clazz = Class.forName(mainClass, true, this);
                final Class<? extends Spell> spellClass = clazz.asSubclass(Spell.class);
                final Constructor<? extends Spell> ctor = spellClass.getConstructor(plugin.getClass());
                final Spell<? extends SpellArgument> spell = (Spell<? extends SpellArgument>) ctor.newInstance(plugin);
                plugin.getSpellConfigManager().loadSpellDefaults(spell);
                spell.initialize();
                jarFile.close();
                return spell;
            } else {
                jarFile.close();
                throw new IllegalArgumentException();
            }
        } catch (final NoClassDefFoundError e) {
            plugin.log(Level.WARNING, "Unable to load " + file.getName() + " spell was not written for KraftRPG!");
            plugin.debugThrow(this.getClass().toString(), "loadSpell", e);
            return null;
        } catch (final ClassNotFoundException e) {
            plugin.log(Level.WARNING, "Unable to load " + file.getName() + " spell was not written for KraftRPG!");
            plugin.debugThrow(this.getClass().toString(), "loadSpell", e);
            return null;
        } catch (final IllegalArgumentException e) {
            plugin.log(Level.SEVERE, "Could not detect the proper Spell class to load for: " + file.getName());
            return null;
        } catch (final Exception e) {
            plugin.log(Level.INFO, "The spell " + file.getName() + " failed to load for an unknown reason.");
            plugin.debugThrow(this.getClass().getName(), "loadSpell", e);
            return null;
        }
    }

    public void removeSpell(Spell<? extends SpellArgument> spell) {
        spellMap.remove(spell.getName().toLowerCase().replace("spell", ""));
    }

    private boolean loadSpell(String name) {
        // If the spell is already loaded, don't try to load it
        if (isLoaded(name)) {
            return true;
        }

        // Lets try loading the spell file
        final Spell<? extends SpellArgument> spell = loadSpell(spellFiles.get(name.toLowerCase()));
        if (spell == null) {
            return false;
        }

        addSpell(spell);
        return true;
    }

    public class SpellManagerListener implements Listener {

        private final Set<PassiveSpell> passiveSpells = new HashSet<PassiveSpell>();
        private final Set<PermissionSpell> permissionSpells = new HashSet<PermissionSpell>();

        protected void addSpell(Spell<? extends SpellArgument> spell) {
            if (spell instanceof PassiveSpell) {
                passiveSpells.add((PassiveSpell) spell);
            } else if (spell instanceof PermissionSpell) {
                permissionSpells.add((PermissionSpell) spell);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onClassChangeEvent(ChampionRoleChangeEvent event) {
            for (PermissionSpell spell : permissionSpells) {
                spell.tryLearning(event.getPlayer());
            }
            for (PassiveSpell spell : passiveSpells) {
                spell.tryApplyingToPlayer(event.getPlayer());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onLevelChangeEvent(ChampionLevelChangeEvent event) {
            for (PermissionSpell spell : permissionSpells) {
                spell.tryLearning(event.getPlayer());
            }
            for (PassiveSpell spell : passiveSpells) {
                spell.tryApplyingToPlayer(event.getPlayer());
            }
        }
    }
}
