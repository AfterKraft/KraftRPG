package com.afterkraft.kraftrpg.spells;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.entity.roles.RoleState;
import com.afterkraft.kraftrpg.api.spells.PermissionSpell;
import com.afterkraft.kraftrpg.api.spells.Spell;
import com.afterkraft.kraftrpg.api.spells.SpellArgument;
import com.afterkraft.kraftrpg.api.spells.SpellConfigManager;
import com.afterkraft.kraftrpg.api.spells.SpellSetting;
import com.afterkraft.kraftrpg.util.MathUtil;

/**
 * @author gabizou
 */
public class RPGSpellConfigManager implements SpellConfigManager {
    // Configurations
    protected static Configuration outsourcedSpellConfig;
    protected static Configuration standardSpellConfig;
    protected static Configuration defaultSpellConfig = new MemoryConfiguration();

    private static Map<String, Configuration> classSpellConfigs = new HashMap<String, Configuration>();
    private static File spellConfigFile;
    private static File outsourcedSpellConfigFile;

    private final KraftRPGPlugin plugin;

    public RPGSpellConfigManager(KraftRPGPlugin plugin) {
        final File dataFolder = plugin.getDataFolder();
        spellConfigFile = new File(dataFolder, "spells.yml");
        outsourcedSpellConfigFile = new File(dataFolder, "permission-spells.yml");
        this.plugin = plugin;
        plugin.getConfigurationManager().checkForConfig(outsourcedSpellConfigFile);
    }

    public void reload() {
        standardSpellConfig = null;
        outsourcedSpellConfig = null;
        this.initialize();
    }

    public void saveSpellConfig() {
        try {
            ((YamlConfiguration) standardSpellConfig).save(spellConfigFile);
        } catch (final IOException e) {
            this.plugin.log(Level.WARNING, "Unable to save default spells file!");
        }
    }

    public Configuration getClassConfig(String name) {
        return classSpellConfigs.get(name);
    }

    public void addClassSpellSettings(String className, String spellName, ConfigurationSection section) {
        Configuration config = classSpellConfigs.get(className);
        if (config == null) {
            config = new MemoryConfiguration();
            classSpellConfigs.put(className, config);
        }
        if (section == null) {
            return;
        }

        ConfigurationSection classSection = config.getConfigurationSection(spellName);
        if (classSection == null) {
            classSection = config.createSection(spellName);
        }

        for (final String key : section.getKeys(true)) {
            if (section.isConfigurationSection(key)) {
                classSection.createSection(key);
            }
        }

        for (final String key : section.getKeys(true)) {
            if (section.isConfigurationSection(key)) {
                continue;
            }

            classSection.set(key, section.get(key));
        }
    }

    public void loadSpellDefaults(Spell spell) {
        if (spell instanceof PermissionSpell) {
            return;
        }
        final ConfigurationSection dSection = spell.getDefaultConfig();
        final ConfigurationSection newSection = defaultSpellConfig.createSection(spell.getName());
        //Loop through once and create all the keys
        for (final String key : dSection.getKeys(true)) {
            if (dSection.isConfigurationSection(key)) {
                newSection.createSection(key);
            }
        }
        for (final String key : dSection.getKeys(true)) {
            if (dSection.isConfigurationSection(key)) {
                //Skip section as they would overwrite data here
                continue;
            }
            final Object o = dSection.get(key);
            if (o instanceof List) {
                newSection.set(key, new ArrayList<Object>((List<?>) o));
            } else {
                newSection.set(key, o);
            }
        }
    }

    // Because bukkit can't handle setting defaults before sections exist
    public void setClassDefaults() {
        for (final Configuration config : classSpellConfigs.values()) {
            config.setDefaults(outsourcedSpellConfig);
        }
    }

    //------------------------//
    // Data retrieval methods //
    //------------------------//

    public String getRaw(Spell spell, String setting, String def) {
        return outsourcedSpellConfig.getString(spell.getName() + "." + setting, def);
    }

    public String getRaw(Spell spell, SpellSetting setting, String def) {
        return getRaw(spell, setting.node(), def);
    }

    public Boolean getRaw(Spell spell, SpellSetting setting, boolean def) {
        return getRaw(spell, setting.node(), def);
    }

    public Boolean getRaw(Spell spell, String setting, boolean def) {
        return Boolean.valueOf(outsourcedSpellConfig.getString(spell.getName() + "." + setting));
    }

    public Set<String> getRawKeys(Spell spell, String setting) {
        String path = spell.getName();
        if (setting != null) {
            path += "." + setting;
        }

        if (!outsourcedSpellConfig.isConfigurationSection(path)) {
            return new HashSet<String>();
        }

        return outsourcedSpellConfig.getConfigurationSection(path).getKeys(false);
    }

    public Object getSetting(Role hc, Spell<? extends SpellArgument> spell, String setting) {
        final Configuration config = classSpellConfigs.get(hc.getName());
        if ((config == null) || !config.isConfigurationSection(spell.getName())) {
            return null;
        } else {
            return config.get(spell.getName() + "." + setting);
        }
    }

    public int getSetting(Role hc, Spell<? extends SpellArgument> spell, String setting, int def) {
        final Object val = getSetting(hc, spell, setting);
        if (val == null) {
            return def;
        } else {
            final Integer i = MathUtil.asInt(val);
            return i != null ? i : def;
        }
    }

    public double getSetting(Role hc, Spell<? extends SpellArgument> spell, String setting, double def) {
        final Object val = getSetting(hc, spell, setting);
        if (val == null) {
            return def;
        } else {
            final Double d = MathUtil.asDouble(val);
            return d != null ? d : def;
        }
    }

    public String getSetting(Role hc, Spell<? extends SpellArgument> spell, String setting, String def) {
        final Object val = getSetting(hc, spell, setting);
        if (val == null) {
            return def;
        } else {
            return val.toString();
        }
    }

    public Boolean getSetting(Role hc, Spell<? extends SpellArgument> spell, String setting, boolean def) {
        final Object val = getSetting(hc, spell, setting);
        if (val == null) {
            return null;
        } else if (val instanceof Boolean) {
            return (Boolean) val;
        } else if (val instanceof String) {
            return Boolean.valueOf((String) val);
        } else {
            return null;
        }
    }

    public List<String> getSetting(Role hc, Spell<? extends SpellArgument> spell, String setting, List<String> def) {
        final Configuration config = classSpellConfigs.get(hc.getName());
        if ((config == null) || !config.isConfigurationSection(spell.getName())) {
            return def;
        }

        final List<String> val = config.getStringList(spell.getName() + "." + setting);
        return (val != null) && !val.isEmpty() ? val : def;
    }

    public Set<String> getSettingKeys(Role hc, Spell<? extends SpellArgument> spell, String setting) {
        String path = spell.getName();
        if (setting != null) {
            path += "." + setting;
        }
        final Configuration config = classSpellConfigs.get(hc.getName());
        if ((config == null) || !config.isConfigurationSection(path)) {
            return new HashSet<String>();
        }

        return config.getConfigurationSection(path).getKeys(false);
    }

    public Set<String> getUseSettingKeys(Champion champion, Spell<? extends SpellArgument> spell, String setting) {
        final Set<String> vals = new HashSet<String>();
        String path = spell.getName();
        if (setting != null) {
            path += "." + setting;
        }
        final ConfigurationSection section = outsourcedSpellConfig.getConfigurationSection(path);
        if (section != null) {
            vals.addAll(section.getKeys(false));
        }
        if (champion.canPrimaryUseSpell(spell)) {
            vals.addAll(getSettingKeys(champion.getPrimaryRole(), spell, setting));
        }
        if (champion.canSecondaryUseSpell(spell)) {
            vals.addAll(getSettingKeys(champion.getSecondaryRole(), spell, setting));
        }
        return vals;
    }

    public List<String> getUseSettingKeys(Champion champion, Spell<? extends SpellArgument> spell) {
        final Set<String> keys = new HashSet<String>();
        final ConfigurationSection section = outsourcedSpellConfig.getConfigurationSection(spell.getName());
        if (section != null) {
            keys.addAll(section.getKeys(false));
        }

        if (champion.canPrimaryUseSpell(spell)) {
            keys.addAll(getSettingKeys(champion.getPrimaryRole(), spell, null));
        }

        if (champion.canSecondaryUseSpell(spell)) {
            keys.addAll(getSettingKeys(champion.getSecondaryRole(), spell, null));
        }
        return new ArrayList<String>(keys);
    }

    private int getLevel(Champion champion, Spell<? extends SpellArgument> spell, RoleState state, int def) {
        if (champion == null) {
            return -1;
        }
        switch (state) {
            case PRIMARY:
                return champion.getPrimaryRole() != null ? getSetting(champion.getPrimaryRole(), spell, SpellSetting.LEVEL.node(), def) : -1;
            case SECONDARY:
                return champion.getSecondaryRole() != null ? getSetting(champion.getSecondaryRole(), spell, SpellSetting.LEVEL.node(), def) : -1;
            case ADDITIONAL:
                int val = -1;
                if (!champion.getAdditionalRoles().isEmpty()) {
                    return val;
                }
                for (Role role : champion.getAdditionalRoles()) {
                    int roleVal = getSetting(role, spell, SpellSetting.LEVEL.node(), def);
                    val = val < roleVal ? roleVal : val;
                }
                return val;
            default:
                return -1;
        }
    }

    public int getLevel(Champion champion, Spell<? extends SpellArgument> spell, int def) {
        final String name = spell.getName();
        if (champion == null) {
            return outsourcedSpellConfig.getInt(name + "." + SpellSetting.LEVEL.node(), def);
        }

        int val1 = getLevel(champion, spell, RoleState.PRIMARY, def);
        int val2 = getLevel(champion, spell, RoleState.SECONDARY, def);
        int val3 = getLevel(champion, spell, RoleState.ADDITIONAL, def);
        int max = Math.max(Math.max(val1, val2), val3);
        if (max != -1) {
            return max;
        } else {
            return outsourcedSpellConfig.getInt(name + "." + SpellSetting.LEVEL.node(), def);
        }
    }

    public int getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, SpellSetting setting, int def, boolean lower) {
        if (setting == SpellSetting.LEVEL) {
            return getLevel(champion, spell, def);
        } else {
            return getUseSetting(champion, spell, setting.node(), def, lower);
        }
    }

    public String getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, SpellSetting setting, String def) {
        return getUseSetting(champion, spell, setting.node(), def);
    }

    public double getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, SpellSetting setting, double def, boolean lower) {
        return getUseSetting(champion, spell, setting.node(), def, lower);
    }

    public boolean getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, SpellSetting setting, boolean def) {
        return getUseSetting(champion, spell, setting.node(), def);
    }

    public int getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, String setting, int def, boolean lower) {
        if (setting.equalsIgnoreCase("level")) {
            throw new IllegalArgumentException("Do not use getSetting() for grabbing a champion level!");
        }

        final String name = spell.getName();
        if (champion == null) {
            return outsourcedSpellConfig.getInt(name + "." + setting, def);
        }

        int val1 = -1;
        int val2 = -1;
        if (champion.canPrimaryUseSpell(spell)) {
            val1 = getSetting(champion.getPrimaryRole(), spell, setting, def);
        }
        if (champion.canSecondaryUseSpell(spell)) {
            val2 = getSetting(champion.getSecondaryRole(), spell, setting, def);
        }

        if ((val1 != -1) && (val2 != -1)) {
            if (lower) {
                return val1 < val2 ? val1 : val2;
            } else {
                return val1 > val2 ? val1 : val2;
            }
        } else if (val1 != -1) {
            return val1;
        } else if (val2 != -1) {
            return val2;
        } else {
            return outsourcedSpellConfig.getInt(name + "." + setting, def);
        }
    }

    public double getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, String setting, double def, boolean lower) {
        final String name = spell.getName();
        if (champion == null) {
            return outsourcedSpellConfig.getDouble(name + "." + setting, def);
        }

        double val1 = -1;
        double val2 = -1;
        if (champion.canPrimaryUseSpell(spell)) {
            val1 = getSetting(champion.getPrimaryRole(), spell, setting, def);
        }
        if (champion.canSecondaryUseSpell(spell)) {
            val2 = getSetting(champion.getSecondaryRole(), spell, setting, def);
        }

        if ((val1 != -1) && (val2 != -1)) {
            if (lower) {
                return val1 < val2 ? val1 : val2;
            } else {
                return val1 > val2 ? val1 : val2;
            }
        } else if (val1 != -1) {
            return val1;
        } else if (val2 != -1) {
            return val2;
        } else {
            return outsourcedSpellConfig.getDouble(name + "." + setting, def);
        }
    }

    public boolean getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, String setting, boolean def) {
        if (champion == null) {
            return outsourcedSpellConfig.getBoolean(spell.getName() + "." + setting, def);
        }
        Boolean val;

        if (champion.canPrimaryUseSpell(spell)) {
            val = getSetting(champion.getPrimaryRole(), spell, setting, def);
            if (val != null) {
                return val;
            }
        }

        if (champion.canSecondaryUseSpell(spell)) {
            val = getSetting(champion.getSecondaryRole(), spell, setting, def);
            if (val != null) {
                return val;
            }
        }

        return def;
    }

    public String getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, String setting, String def) {
        if (champion == null) {
            return outsourcedSpellConfig.getString(spell.getName() + "." + setting, def);
        } else if (champion.canPrimaryUseSpell(spell)) {
            return getSetting(champion.getPrimaryRole(), spell, setting, def);
        } else if (champion.canSecondaryUseSpell(spell)) {
            return getSetting(champion.getSecondaryRole(), spell, setting, def);
        } else {
            return outsourcedSpellConfig.getString(spell.getName() + "." + setting, def);
        }
    }

    public List<String> getUseSetting(Champion champion, Spell<? extends SpellArgument> spell, String setting, List<String> def) {
        if (champion == null) {
            final List<String> list = outsourcedSpellConfig.getStringList(spell.getName() + "." + setting);
            return list != null ? list : def;
        }

        final List<String> vals = new ArrayList<String>();
        if (champion.canPrimaryUseSpell(spell)) {
            final List<String> list = getSetting(champion.getPrimaryRole(), spell, setting, new ArrayList<String>());
            vals.addAll(list);
        }
        if (champion.canSecondaryUseSpell(spell)) {
            final List<String> list = getSetting(champion.getSecondaryRole(), spell, setting, new ArrayList<String>());
            vals.addAll(list);
        }
        if (!vals.isEmpty()) {
            return vals;
        } else {
            final List<String> list = outsourcedSpellConfig.getStringList(spell.getName() + "." + setting);
            return (list != null) && !list.isEmpty() ? list : def;
        }
    }

    @Override
    public void initialize() {
        // Setup the standard spell configuration
        standardSpellConfig = YamlConfiguration.loadConfiguration(spellConfigFile);
        standardSpellConfig.setDefaults(defaultSpellConfig);
        standardSpellConfig.options().copyDefaults(true);

        // Setup the outsourced spell configuration
        outsourcedSpellConfig = YamlConfiguration.loadConfiguration(outsourcedSpellConfigFile);
        outsourcedSpellConfig.setDefaults(standardSpellConfig);

        //MERGE!
        for (final String key : standardSpellConfig.getKeys(true)) {
            if (standardSpellConfig.isConfigurationSection(key)) {
                continue;
            }
            outsourcedSpellConfig.set(key, standardSpellConfig.get(key));
        }
    }

    @Override
    public void shutdown() {

    }
}