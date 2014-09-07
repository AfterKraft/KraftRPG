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
package com.afterkraft.kraftrpg.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;

import com.afterkraft.kraftrpg.api.RPGPlugin;
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
    protected final RPGPlugin plugin;


    public RPGConfigManager(RPGPlugin plugin) {
        this.plugin = plugin;
        final File dataFolder = plugin.getDataFolder();
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
                final InputStream input = RPGConfigManager.class.getResourceAsStream("/defaults/" + config.getName());
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
