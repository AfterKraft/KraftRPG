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

import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.afterkraft.kraftrpg.api.ExternalProviderRegistration;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.CombatTracker;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.effects.EffectManager;
import com.afterkraft.kraftrpg.api.entity.party.PartyManager;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.listeners.ListenerManager;
import com.afterkraft.kraftrpg.api.roles.RoleManager;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.SkillBind;
import com.afterkraft.kraftrpg.api.skills.SkillConfigManager;
import com.afterkraft.kraftrpg.api.skills.SkillManager;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;
import com.afterkraft.kraftrpg.api.util.ConfigManager;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.api.util.Properties;
import com.afterkraft.kraftrpg.commands.RPGCommandManager;
import com.afterkraft.kraftrpg.commands.RPGParentCommand;
import com.afterkraft.kraftrpg.commands.RPGSkillCommand;
import com.afterkraft.kraftrpg.entity.RPGCombatTracker;
import com.afterkraft.kraftrpg.entity.RPGEntityManager;
import com.afterkraft.kraftrpg.effects.RPGEffectManager;
import com.afterkraft.kraftrpg.entity.party.RPGPartyManager;
import com.afterkraft.kraftrpg.listeners.RPGListenerManager;
import com.afterkraft.kraftrpg.roles.RPGRoleManager;
import com.afterkraft.kraftrpg.skills.RPGSkillConfigManager;
import com.afterkraft.kraftrpg.skills.RPGSkillManager;
import com.afterkraft.kraftrpg.storage.RPGStorageManager;
import com.afterkraft.kraftrpg.storage.YMLStorageBackend;
import com.afterkraft.kraftrpg.util.RPGConfigManager;
import com.afterkraft.kraftrpg.util.RPGDamageManager;
import com.afterkraft.kraftrpg.util.RPGPluginProperties;

public final class KraftRPGPlugin extends JavaPlugin implements RPGPlugin {

    public static final String ADMIN_INVENTORY_BYPASS_PERMISSION = "kraftrpg.admin.bypass.inventory";
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
    private PartyManager partyManager;
    private RPGEffectManager effectManager;
    private RPGListenerManager listenerManager;
    private RPGCommandManager commandManager;

    private Permission permisisons;
    private Economy economy;

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
    public void onDisable() {
        try {
            this.commandManager.shutdown();
            this.listenerManager.shutdown();
            this.entityManager.shutdown();
            this.combatTracker.shutdown();
            this.roleManager.shutdown();
            this.skillManager.shutdown();
            this.partyManager.shutdown();
            this.effectManager.shutdown();
            this.skillConfigManager.shutdown();
            this.damageManager.shutdown();
            this.storageManager.shutdown();
        } catch (Exception e) {
            log(Level.WARNING, "------------------------------------------------");
            log(Level.WARNING, "|--- Something did not shut down correctly! ---|");
            log(Level.WARNING, "|--- Please make sure to report the following -|");
            log(Level.WARNING, "|--- error to the KraftRPG devs! --------------|");
            e.printStackTrace();
            log(Level.WARNING, "|----------------------------------------------|");
            log(Level.WARNING, "|---------------- End of Error ----------------|");
            log(Level.WARNING, "------------------------------------------------");

        }
    }

    @Override
    public void onEnable() {
        instance = this;
        setupVault();
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
        this.skillManager = new RPGSkillManager(this);
        this.skillConfigManager = new RPGSkillConfigManager(this);
        this.effectManager = new RPGEffectManager(this);
        this.damageManager = new RPGDamageManager(this);
        this.roleManager = new RPGRoleManager(this);
        this.combatTracker = new RPGCombatTracker(this);
        this.entityManager = new RPGEntityManager(this);
        if (ExternalProviderRegistration.getPartyManager() != null) {
            this.partyManager = ExternalProviderRegistration.getPartyManager();
        } else {
            this.partyManager = new RPGPartyManager(this);
        }
        this.storageManager.initialize();
        this.skillConfigManager.initialize();
        this.skillManager.initialize();
        this.effectManager.initialize();
        this.damageManager.initialize();
        this.roleManager.initialize();
        this.combatTracker.initialize();
        this.entityManager.initialize();
        this.partyManager.initialize();
        this.listenerManager = new RPGListenerManager(this);
        CraftBukkitHandler.getInterface().loadExtraListeners();
        this.listenerManager.initialize();
        this.commandManager = new RPGCommandManager(this);
        registerCommandExecutors();
        this.commandManager.initialize();
    }

    private void setupVault() {
        if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            this.permisisons = rsp.getProvider();
            RegisteredServiceProvider<Economy> esp = this.getServer().getServicesManager().getRegistration(Economy.class);
            this.economy = esp != null ? esp.getProvider() : null;
        }
    }

    private void registerCommandExecutors() {
        this.commandManager.registerCommand("skill", new RPGSkillCommand(this));
        this.commandManager.registerCommand("rpg", new RPGParentCommand(this));
    }

    @Override
    public void cancelEnable() {
        cancel = true;
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public Permission getVaultPermissions() {
        return this.permisisons;
    }

    @Override
    public Economy getVaultEconomy() {
        return this.economy;
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
    public ConfigManager getConfigurationManager() {
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
