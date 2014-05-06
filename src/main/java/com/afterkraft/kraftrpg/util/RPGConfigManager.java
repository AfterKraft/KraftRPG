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
package com.afterkraft.kraftrpg.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.util.ConfigManager;


public class RPGConfigManager implements ConfigManager {

    // Files
    protected static File roleConfigFolder;
    protected static File expConfigFile;
    protected static File damageConfigFile;
    protected static File recipesConfigFile;
    protected static File storageConfigFile;
    //Configurations
    private static Configuration damageConfig;
    private static Configuration expConfig;
    private static Configuration recipeConfig;
    protected final KraftRPGPlugin plugin;


    public RPGConfigManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        final File dataFolder = plugin.getDataFolder();
        RPGConfigManager.roleConfigFolder = new File(dataFolder + File.separator + "roles");
        RPGConfigManager.expConfigFile = new File(dataFolder, "experience.yml");
        RPGConfigManager.damageConfigFile = new File(dataFolder, "damages.yml");
        RPGConfigManager.recipesConfigFile = new File(dataFolder, "recipes.yml");
        RPGConfigManager.storageConfigFile = new File(dataFolder, "storage.yml");
    }

    @Override
    public void checkForConfig(File config) {
        if (!config.exists()) {
            try {
                this.plugin.log(Level.WARNING, "File " + config.getName() + " not found - generating defaults.");
                config.getParentFile().mkdir();
                config.createNewFile();
                final OutputStream output = new FileOutputStream(config, false);
                final InputStream input = ConfigManager.class.getResourceAsStream("/defaults/" + config.getName());
                final byte[] buf = new byte[8192];
                while (true) {
                    final int length = input.read(buf);
                    if (length < 0) {
                        break;
                    }
                    output.write(buf, 0, length);
                }
                input.close();
                output.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
