package com.afterkraft.kraftrpg.storage;

import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.storage.RPGStorage;
import com.afterkraft.kraftrpg.api.storage.StorageManager;

/**
 * @author gabizou
 */
public class RPGStorageManager extends URLClassLoader implements StorageManager {

    private final Map<String, RPGStorage> possibleStorages;
    private final KraftRPGPlugin plugin;
    private final Map<String, File> storageFiles;
    private String configuredStorage;
    private RPGStorage defaultStorage;

    public RPGStorageManager(KraftRPGPlugin plugin) {
        super(((URLClassLoader) plugin.getClass().getClassLoader()).getURLs(), plugin.getClass().getClassLoader());
        this.possibleStorages = new HashMap<String, RPGStorage>();
        this.plugin = plugin;
        this.storageFiles = new HashMap<String, File>();
        final File storageDirectory = new File(plugin.getDataFolder(), "storage");
        this.configuredStorage = plugin.getProperties().getStorageType();

        if (configuredStorage == null) {
            this.configuredStorage = "sql";
        }
    }

    public RPGStorage getStorage() {
        return this.defaultStorage;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}
