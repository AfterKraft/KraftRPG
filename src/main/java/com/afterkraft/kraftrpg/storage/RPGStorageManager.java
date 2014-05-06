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
package com.afterkraft.kraftrpg.storage;

import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.storage.RPGStorage;
import com.afterkraft.kraftrpg.api.storage.StorageManager;


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

    @Override
    public boolean setStorage(RPGStorage storage) {
        if (storage != null) {
            this.defaultStorage = storage;
            return true;
        }
        return false;
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
