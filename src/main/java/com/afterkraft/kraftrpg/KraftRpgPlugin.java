package com.afterkraft.kraftrpg;

import com.afterkraft.kraftrpg.api.RpgKeys;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "kraftrpg",
        name = "KraftRPG",
        version = "${version}",
        description = "Base plugin implementing the KraftRPG API to provide a robust skills and classes framework for SpongeAPI",
        authors = "gabizou"
)
public class KraftRpgPlugin {

    @Listener
    public void keyRegister(GameRegistryEvent.Register<Key<?>> event) {

        event.register(RpgKeys.RPG_EFFECTS);
        event.register(RpgKeys.PRIMARY_ROLE);
        event.register(RpgKeys.ADDITIONAL_ROLES);
        event.register(RpgKeys.SECONDARY_ROLE);
        event.register(RpgKeys.PARTY);
        event.register(RpgKeys.BASE_DAMAGE);
        event.register(RpgKeys.DAMAGE_MODIFIER);
        event.register(RpgKeys.MANA);
        event.register(RpgKeys.MAX_MANA);
        event.register(RpgKeys.SUMMON_DURATION);
        event.register(RpgKeys.REWARDING_EXPERIENCE);
    }


    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path configFile;


    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDirectory;

    private ConfigurationNode config;

    @Listener
    public void preInitialization(GamePreInitializationEvent event) {
        try {
            config = configManager.load();

            if (!configFile.toFile().exists()) {
                config.getNode("KraftRPG Config Placeholder").setValue(true);
                configManager.save(config);
            }


        } catch (IOException e) {
            logger.warn("KraftRPG could not successfully load the configuration!");
        }
    }

    @Listener
    public void gameLoaded(GameLoadCompleteEvent event) {
        

    }


    //Plugin Logger initialization
    private Logger logger;

    @Listener
    //Load up KraftRPG
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("KraftRPG was successfully loaded!");


    }

    public Path getConfigDirectory() {
        return configDirectory;
    }


    public Logger getLogger() {

        return logger;
    }

}