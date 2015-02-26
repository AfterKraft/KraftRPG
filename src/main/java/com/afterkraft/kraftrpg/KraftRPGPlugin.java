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
package com.afterkraft.kraftrpg;

import java.io.File;

import org.slf4j.Logger;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.state.ConstructionEvent;
import org.spongepowered.api.event.state.PostInitializationEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.util.event.Subscribe;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import com.afterkraft.kraftrpg.api.ExternalProviderRegistration;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.effects.EffectManager;
import com.afterkraft.kraftrpg.api.entity.CombatTracker;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.entity.party.PartyManager;
import com.afterkraft.kraftrpg.api.listeners.ListenerManager;
import com.afterkraft.kraftrpg.api.roles.RoleManager;
import com.afterkraft.kraftrpg.api.skills.SkillConfigManager;
import com.afterkraft.kraftrpg.api.skills.SkillManager;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.api.util.Properties;
import com.afterkraft.kraftrpg.commands.RPGSkillCommand;
import com.afterkraft.kraftrpg.effects.RPGEffectManager;
import com.afterkraft.kraftrpg.entity.RPGCombatTracker;
import com.afterkraft.kraftrpg.entity.RPGEntityManager;
import com.afterkraft.kraftrpg.entity.party.RPGPartyManager;
import com.afterkraft.kraftrpg.listeners.RPGListenerManager;
import com.afterkraft.kraftrpg.roles.RPGRoleManager;
import com.afterkraft.kraftrpg.skills.RPGSkillConfigManager;
import com.afterkraft.kraftrpg.skills.RPGSkillManager;
import com.afterkraft.kraftrpg.storage.RPGStorageManager;
import com.afterkraft.kraftrpg.util.RPGConfigManager;
import com.afterkraft.kraftrpg.util.RPGDamageManager;
import com.afterkraft.kraftrpg.util.RPGPluginProperties;

/**
 * Standard implementation of RPGPlugin for SpongeAPI
 */
@Singleton
@Plugin(id = "KraftRPG", name = "KraftRPG", version = "0.0.2-SNAPSHOT")
public final class KraftRPGPlugin implements RPGPlugin {

    public static final String ADMIN_INVENTORY_BYPASS_PERMISSION =
            "kraftrpg.admin.bypass.inventory";
    private static KraftRPGPlugin instance;
    private static boolean cancel = false;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File mainConfig;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    private RPGSkillManager skillManager;
    private RPGSkillConfigManager skillConfigManager;
    private RPGCombatTracker combatTracker;
    private RPGEntityManager entityManager;
    private RPGStorageManager storageManager;
    private RPGPluginProperties properties;
    private RPGDamageManager damageManager;
    private RPGConfigManager configManager;
    private RPGRoleManager roleManager;
    private PartyManager partyManager;
    private RPGEffectManager effectManager;
    private RPGListenerManager listenerManager;
    private boolean enabled = false;

    public static KraftRPGPlugin getInstance() {
        return KraftRPGPlugin.instance;
    }

    @Subscribe
    public void onConstruction(ConstructionEvent event) {
        RpgCommon.setPlugin(this);
        ExternalProviderRegistration.pluginLoaded(this);
        instance = this;

        Injector injector = Guice.createInjector(new RpgModule());
    }

    @Subscribe
    public void onPreInit(PreInitializationEvent event) {
        this.configManager = new RPGConfigManager(this, this.mainConfig,
                                                  this.configLoader);
        this.properties = new RPGPluginProperties();

    }

    @Subscribe
    public void onPostInit(PostInitializationEvent event) {

    }

    @Subscribe
    public void onPreStart(ServerAboutToStartEvent event) {
        RpgCommon.setCommonServer(event.getGame().getServer().get());
        RpgCommon.setGame(event.getGame());
        ExternalProviderRegistration.finish();
        this.storageManager = new RPGStorageManager(this);
        if (cancel) {
            return;
        }
        this.skillManager = new RPGSkillManager(this);
        this.skillConfigManager = new RPGSkillConfigManager(this);
        this.effectManager = new RPGEffectManager(this);
        this.damageManager = new RPGDamageManager(this);
        this.roleManager = new RPGRoleManager(this);
        this.combatTracker = new RPGCombatTracker(this);
        this.entityManager = new RPGEntityManager(this);
        // We need this registration check for external providers
        if (ExternalProviderRegistration.getPartyManager() != null) {
            this.partyManager = ExternalProviderRegistration.getPartyManager();
        } else {
            // If not, let's just use the default.
            this.partyManager = new RPGPartyManager(this);
        }
        // Initialize managers in order.
        this.storageManager.initialize();
        this.skillConfigManager.initialize();
        this.skillManager.initialize();
        this.effectManager.initialize();
        this.damageManager.initialize();
        this.roleManager.initialize();
        this.combatTracker.initialize();
        this.entityManager.initialize();
        this.partyManager.initialize();
        // Start up the RPGListener
        this.listenerManager = new RPGListenerManager(this);
        this.listenerManager.initialize();
        RpgCommon.finish();
        this.enabled = true;
    }

    @Subscribe
    public void onStarting(ServerStartingEvent event) {

    }

    @Subscribe
    public void onDisable(ServerStoppingEvent event) {
        try {
            this.listenerManager.shutdown();
            this.entityManager.shutdown();

        } catch (Exception e) {
            this.logger
                    .warn("------------------------------------------------");
            this.logger
                    .warn("|--- Something did not shut down correctly! ---|");
            this.logger
                    .warn("|--- Please make sure to report the following -|");
            this.logger
                    .warn("|--- error to the KraftRPG devs! --------------|");
            e.printStackTrace();
            this.logger
                    .warn("|----------------------------------------------|");
            this.logger
                    .warn("|---------------- End of Error ----------------|");
            this.logger
                    .warn("------------------------------------------------");
        }
    }

    private void registerCommandExecutors() {
        CommandService service = RpgCommon.getGame().getCommandDispatcher();
        service.register(this, new RPGSkillCommand(this), "skill", "skills",
                         "cast");

    }

    @Override
    public void cancelEnable() {
        cancel = true;
    }

    @Override
    public SkillConfigManager getSkillConfigManager() {
        return this.skillConfigManager;
    }

    @Override
    public CombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public EffectManager getEffectManager() {
        return this.effectManager;
    }

    @Override
    public StorageFrontend getStorage() {
        return this.storageManager.getStorage();
    }

    @Override
    public RPGConfigManager getConfigurationManager() {
        return this.configManager;
    }

    @Override
    public DamageManager getDamageManager() {
        return this.damageManager;
    }

    @Override
    public SkillManager getSkillManager() {
        return this.skillManager;
    }

    @Override
    public RoleManager getRoleManager() {
        return this.roleManager;
    }

    @Override
    public PartyManager getPartyManager() {
        return this.partyManager;
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public ListenerManager getListenerManager() {
        return this.listenerManager;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
