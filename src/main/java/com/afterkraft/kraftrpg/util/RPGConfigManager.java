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

import org.spongepowered.api.service.config.ConfigService;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.util.ConfigManager;

/**
 * Standard implementation of ConfigManager specific for KraftRPG
 */
public class RPGConfigManager implements ConfigManager {

    // Files
    protected static File pluginDirectory;
    protected static File roleConfigFolder;
    protected static File expConfigFile;
    protected static File damageConfigFile;
    protected static File recipesConfigFile;
    protected static File storageConfigFile;
    protected final KraftRPGPlugin plugin;


    public RPGConfigManager(KraftRPGPlugin plugin, File mainConfig,
                            ConfigurationLoader<CommentedConfigurationNode>
                                    configurationLoader) {
        this.plugin = plugin;

        RPGConfigManager.pluginDirectory = getConfigurationDirectory();
        RPGConfigManager.expConfigFile =
                new File(pluginDirectory, "experience.yml");
        RPGConfigManager.damageConfigFile =
                new File(pluginDirectory, "damages.yml");
        RPGConfigManager.recipesConfigFile =
                new File(pluginDirectory, "recipes.yml");
        RPGConfigManager.storageConfigFile =
                new File(pluginDirectory, "storage.yml");
    }

    private static File getConfigurationDirectory() {
        return RpgCommon.getGame().getServiceManager()
                .provide(ConfigService.class)
                .get().getSharedConfig(RpgCommon.getPlugin()).getDirectory();
    }

    @Override
    public void checkForConfig(File config) {
        if (!config.exists()) {
            try {
                this.plugin.getLogger()
                        .warn("File " + config.getName() + " not "
                                + "found - "
                                + "generating defaults.");
                config.getParentFile().mkdir();
                config.createNewFile();
                final OutputStream output = new FileOutputStream(config, false);
                final InputStream input =
                        RPGConfigManager.class.getResourceAsStream(
                                "/defaults/" + config.getName());
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

    public File getConfigDirectory() {
        return RPGConfigManager.pluginDirectory;
    }
}
