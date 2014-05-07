package com.afterkraft.kraftrpg.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import net.minecraft.util.org.apache.commons.io.FilenameUtils;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.skills.SkillBind;
import com.afterkraft.kraftrpg.api.storage.PlayerData;
import com.afterkraft.kraftrpg.api.storage.StorageBackend;
import com.afterkraft.kraftrpg.api.util.FixedPoint;

public class YMLStorageBackend implements StorageBackend {
    private RPGPlugin plugin;
    private File directory;

    public YMLStorageBackend(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws Throwable {
        directory = new File(plugin.getDataFolder(), "players");
        directory.mkdirs();
    }

    @Override
    public void shutdown() {
        // nothing
    }

    private File getFile(UUID uuid) {
        return new File(directory, uuid.toString() + ".yml");
    }

    // ASYNC
    @Override
    public boolean removePlayer(UUID uuid) {
        File file = getFile(uuid);
        return file.delete();
    }

    // ASYNC
    @Override
    public boolean savePlayer(UUID uuid, PlayerData data) {
        File file = getFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        List<String> list;
        Map<String, Object> map;

        // ignored on loading - solely for admin convenience when they want to edit files
        config.set("uuid", uuid.toString());
        config.set("name", data.lastKnownName);
        config.set("primary", data.primary.getName());
        config.set("profession", data.profession.getName());

        list = new ArrayList<String>();
        for (Role r : data.additionalRoles) {
            list.add(r.getName());
        }
        config.set("additional", list);

        map = new HashMap<String, Object>();
        for (Map.Entry<Role, FixedPoint> entry : data.exp.entrySet()) {
            map.put(entry.getKey().getName(), entry.getValue().rawValue());
        }
        config.set("exp", map);

        List<ConfigurationSerializable> list2 = new ArrayList<ConfigurationSerializable>(data.binds.values());
        config.set("binds", list2);

        map = new HashMap<String, Object>();
        map.putAll(data.cooldowns);
        config.set("cooldowns", map);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public PlayerData loadPlayer(UUID uuid, boolean shouldCreate) {
        File file = getFile(uuid);

        if (!file.exists()) {
            return shouldCreate ? new PlayerData() : null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerData data = new PlayerData();
        boolean ok = true;

        String temp = config.getString("primary");
        data.primary = plugin.getRoleManager().getRole(temp);
        if (data.primary == null) {
            data.primary = plugin.getRoleManager().getDefaultPrimaryRole();
            plugin.getLogger().warning("Could not find class " + temp + " referenced in data file " + file.getName());
            ok = false;
        }

        temp = config.getString("profession");
        data.profession = plugin.getRoleManager().getRole(temp);
        if (data.profession == null) {
            data.profession = plugin.getRoleManager().getDefaultSecondaryRole();
            plugin.getLogger().warning("Could not find class " + temp + " referenced in data file " + file.getName());
            ok = false;
        }

        List<String> list;
        list = config.getStringList("additional");

        for (String str : list) {
            Role r = plugin.getRoleManager().getRole(str);
            if (r == null) {
                plugin.getLogger().warning("Could not find class " + str + " referenced in data file " + file.getName());
                ok = false;
            } else {
                data.additionalRoles.add(r);
            }
        }

        ConfigurationSection section;
        section = config.getConfigurationSection("exp");
        for (String str : section.getKeys(false)) {
            Role r = plugin.getRoleManager().getRole(str);
            if (r == null) {
                plugin.getLogger().warning("Could not find class " + str + " referenced in data file " + file.getName());
                ok = false;
            } else {
                long l = section.getLong(str);
                data.exp.put(r, new FixedPoint(l, true));
            }
        }

        List<?> list2 = config.getList("binds");
        for (Object o : list2) {
            if (o instanceof SkillBind) {
                SkillBind bind = (SkillBind) o;
                data.binds.put(bind.getMaterial(), bind);
            } else {
                plugin.getLogger().warning("Item in binds that isn't a 'krpg-bind' in data file " + file.getName());
                ok = false;
            }
        }

        section = config.getConfigurationSection("cooldowns");
        for (String str : section.getKeys(false)) {
            data.cooldowns.put(str, config.getLong(str));
        }

        if (!ok) {
            plugin.getLogger().warning("Due to the potential for data loss, a backup will be made at `backup-" + file.getName() + "`");

            if (createBackupFile(uuid)) {
                data.lastKnownName = config.getString("name");
                // createBackupFile does a rename, so save the player now
                savePlayer(uuid, data);
            }
        }

        return data;
    }

    private boolean createBackupFile(UUID uuid) {
        File newFile = new File(directory, "backup-" + uuid.toString() + ".yml");
        if (newFile.exists()) {
            long last = newFile.lastModified();
            long duration = System.currentTimeMillis() - last;

            // 1 week
            if (duration > 7 * 24 * 60 * 60 * 1000) {
                plugin.getLogger().warning("Overwriting week-old previous backup file");
                newFile.delete();
            } else {
                return false;
            }
        }

        getFile(uuid).renameTo(newFile);
        return true;
    }

    // ASYNC
    @Override
    public List<UUID> getAllStoredUsers() {
        File[] files = directory.listFiles();
        List<UUID> list = new ArrayList<UUID>();

        for (File file : files) {
            try {
                list.add(UUID.fromString(FilenameUtils.removeExtension(file.getName())));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not parse uuid from filename '" + file.getName() + "'");
            }
        }

        return list;
    }
}
