package com.afterkraft.kraftrpg.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.util.ConfigManager;

/**
 * @author gabizou
 */
public class RPGConfigManager implements ConfigManager {

    protected final KraftRPGPlugin plugin;
    // Files
    protected static File classConfigFolder;
    protected static File expConfigFile;
    protected static File damageConfigFile;
    protected static File recipesConfigFile;
    protected static File storageConfigFile;

    //Configurations
    private static Configuration damageConfig;
    private static Configuration expConfig;
    private static Configuration recipeConfig;


    public RPGConfigManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        final File dataFolder = plugin.getDataFolder();
        RPGConfigManager.classConfigFolder = new File(dataFolder + File.separator + "classes");
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
