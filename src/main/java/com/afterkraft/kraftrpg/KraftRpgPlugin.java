/*
 * The MIT License (MIT)
 *
 * Copyright (c) Gabriel Harris-Rouquette
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
package com.afterkraft.kraftrpg;

import com.afterkraft.kraftrpg.api.RpgKeys;

import com.afterkraft.kraftrpg.api.role.Role;
import com.afterkraft.kraftrpg.common.data.manipulator.immutable.ImmutableRoleData;
import com.afterkraft.kraftrpg.common.data.manipulator.mutable.RoleData;
import com.afterkraft.kraftrpg.role.*;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;

import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "kraftrpg",
        name = "KraftRPG",
        version = "${version}",
        description = "Base plugin implementing the KraftRPG API to provide a robust skills and classes framework for SpongeAPI",
        authors = "gabizou"
)
public class KraftRpgPlugin  {


    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path configFile;


    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDirectory;

    @Inject private GameRegistry registry;
    @Inject private DataManager dataManager;
    @Inject private PluginContainer container;

    @Inject
    private Logger logger;

    private ConfigurationNode config;

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

    @Listener
    public void dataRegister(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
        DataRegistration.builder()
                .dataClass(RoleData.class)
                .dataImplementation(RoleDataImpl.class)
                .immutableClass(ImmutableRoleData.class)
                .immutableImplementation(ImmutableRoleDataImpl.class)
                .dataName("RoleData")
                .manipulatorId("roledata")
                .builder(new RoleDataBuilder())
                .buildAndRegister(this.container);
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        if (!event.getTargetEntity().get(RoleData.class).isPresent()) {
            event.getTargetEntity().offer(new RoleDataImpl(RoleRegistry.getInstance().getDefaultPrimaryRole(), RoleRegistry.getInstance().getDefaultSecondaryRole().orElse(null)));
        }
    }




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

        // This is where you need to implement some registries, you can look at
        // HappyTrails for an example.
        this.registry.registerModule(Role.class, RoleRegistry.getInstance());
        this.registry.registerBuilderSupplier(Role.Builder.class, RoleBuilderImpl::new);
        this.dataManager.registerBuilder(Role.class, new RoleBuilderImpl());

    }

}