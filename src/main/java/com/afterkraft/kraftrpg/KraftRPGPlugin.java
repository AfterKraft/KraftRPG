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
package com.afterkraft.kraftrpg;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.afterkraft.kraftrpg.api.ExternalProviderRegistration;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.effects.EffectManager;
import com.afterkraft.kraftrpg.api.entity.party.PartyManager;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.listeners.ListenerManager;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.SkillBind;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;
import com.afterkraft.kraftrpg.commands.RPGCommandManager;
import com.afterkraft.kraftrpg.commands.RPGParentCommand;
import com.afterkraft.kraftrpg.commands.RPGSkillCommand;
import com.afterkraft.kraftrpg.entity.RPGCombatTracker;
import com.afterkraft.kraftrpg.entity.RPGEntityManager;
import com.afterkraft.kraftrpg.entity.effects.RPGEffectManager;
import com.afterkraft.kraftrpg.entity.party.RPGPartyManager;
import com.afterkraft.kraftrpg.entity.roles.RPGRoleManager;
import com.afterkraft.kraftrpg.listeners.RPGListenerManager;
import com.afterkraft.kraftrpg.skills.RPGSkillConfigManager;
import com.afterkraft.kraftrpg.skills.RPGSkillManager;
import com.afterkraft.kraftrpg.storage.RPGStorageManager;
import com.afterkraft.kraftrpg.storage.YMLStorageBackend;
import com.afterkraft.kraftrpg.util.RPGConfigManager;
import com.afterkraft.kraftrpg.util.RPGDamageManager;
import com.afterkraft.kraftrpg.util.RPGPluginProperties;

public final class KraftRPGPlugin extends JavaPlugin implements RPGPlugin {

    private static KraftRPGPlugin instance;
    private static boolean cancel = false;

    private RPGSkillManager skillManager;
    private RPGSkillConfigManager skillConfigManager;
    private RPGCombatTracker combatTracker;
    private RPGEntityManager entityManager;
    private RPGStorageManager storageManager;
    private RPGPluginProperties properties;
    private RPGDamageManager damageManager;
    private RPGConfigManager configManager;
    private RPGRoleManager roleManager;
    private RPGPartyManager partyManager;
    private RPGEffectManager effectManager;
    private RPGListenerManager listenerManager;
    private RPGCommandManager commandManager;

    public static KraftRPGPlugin getInstance() {
        return KraftRPGPlugin.instance;
    }

    @Override
    public void onLoad() {
        ConfigurationSerialization.registerClass(SkillBind.class);

        // Register our defaults
        ExternalProviderRegistration.pluginLoaded(this);
        ExternalProviderRegistration.registerStorageBackend(new YMLStorageBackend(this), "yml", "yaml");
    }

    @Override
    public void onEnable() {
        instance = this;
        if (CraftBukkitHandler.getInterface() == null) {
            getLogger().severe("Could not initialize internal handlers - please check for updates!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ExternalProviderRegistration.finish();

        CraftBukkitHandler.getInterface().addNBTAttributes();

        this.properties = new RPGPluginProperties();
        this.configManager = new RPGConfigManager(this);
        this.storageManager = new RPGStorageManager(this);
        if (cancel) return;
        this.damageManager = new RPGDamageManager(this);
        this.roleManager = new RPGRoleManager(this);
        this.combatTracker = new RPGCombatTracker(this);
        this.entityManager = new RPGEntityManager(this);
        this.skillManager = new RPGSkillManager(this);
        this.skillConfigManager = new RPGSkillConfigManager(this);
        this.effectManager = new RPGEffectManager(this);
        this.partyManager = new RPGPartyManager(this);
        this.listenerManager = new RPGListenerManager(this);
        CraftBukkitHandler.getInterface().loadExtraListeners();
        this.commandManager = new RPGCommandManager(this);
        registerCommandExecutors();
    }

    private void registerCommandExecutors() {
        commandManager.registerCommand("skill", new RPGSkillCommand(this));
        commandManager.registerCommand("rpg", new RPGParentCommand(this));
    }

    @Override
    public void cancelEnable() {
        cancel = true;
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public RPGSkillConfigManager getSkillConfigManager() {
        return this.skillConfigManager;
    }

    @Override
    public RPGCombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    @Override
    public RPGEntityManager getEntityManager() {
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
    public RPGDamageManager getDamageManager() {
        return this.damageManager;
    }

    @Override
    public RPGSkillManager getSkillManager() {
        return this.skillManager;
    }

    @Override
    public RPGRoleManager getRoleManager() {
        return this.roleManager;
    }

    @Override
    public PartyManager getPartyManager() {
        return this.partyManager;
    }

    public RPGPluginProperties getProperties() {
        return this.properties;
    }

    @Override
    public ListenerManager getListenerManager() {
        return this.listenerManager;
    }

    @Override
    public void log(Level level, String msg) {

    }

    @Override
    public void logSkillThrowing(ISkill skill, String action, Throwable thrown, Object context) {
        Bukkit.broadcast(String.format("%sThe skill %s%s%s encountered an error while %s%s%s - %s%s.",
                ChatColor.RED, ChatColor.YELLOW, skill.getName(), ChatColor.RED,
                ChatColor.YELLOW, action, ChatColor.RED, ChatColor.BLUE, thrown.getClass()), Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
        thrown.printStackTrace();
        System.err.println(context);
    }

    @Override
    public void debugLog(Level level, String msg) {

    }

    @Override
    public void debugThrow(String sourceClass, String sourceMethod, Throwable thrown) {

    }
}
