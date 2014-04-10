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

import org.bukkit.plugin.java.JavaPlugin;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.entity.roles.RoleManager;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.spells.SpellConfigManager;
import com.afterkraft.kraftrpg.api.spells.SpellManager;
import com.afterkraft.kraftrpg.api.util.ConfigManager;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.entity.RPGEntityManager;
import com.afterkraft.kraftrpg.entity.roles.RPGRoleManager;
import com.afterkraft.kraftrpg.spells.RPGSpellConfigManager;
import com.afterkraft.kraftrpg.spells.RPGSpellManager;
import com.afterkraft.kraftrpg.storage.RPGStorageManager;
import com.afterkraft.kraftrpg.util.RPGConfigManager;
import com.afterkraft.kraftrpg.util.RPGDamangeManager;
import com.afterkraft.kraftrpg.util.RPGPluginProperties;

/**
 * @author gabizou
 */
public final class KraftRPGPlugin extends JavaPlugin implements RPGPlugin {

    private SpellManager spellManager;
    private SpellConfigManager spellConfigManager;
    private EntityManager entityManager;
    private RPGStorageManager storageManager;
    private RPGPluginProperties properties;
    private DamageManager damageManager;
    private ConfigManager configManager;
    private RoleManager roleManager;

    private static KraftRPGPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        CraftBukkitHandler.getInterface(); // Initialize CraftBukkitHandler
        this.properties = new RPGPluginProperties();
        this.configManager = new RPGConfigManager(this);
        this.storageManager = new RPGStorageManager(this);
        this.damageManager = new RPGDamangeManager(this);
        this.roleManager = new RPGRoleManager(this);
        this.entityManager = new RPGEntityManager(this);
        this.spellManager = new RPGSpellManager(this);
        this.spellConfigManager = new RPGSpellConfigManager(this);

    }

    public static KraftRPGPlugin getInstance() {
        return KraftRPGPlugin.instance;
    }

    @Override
    public SpellConfigManager getSpellConfigManager() {
        return this.spellConfigManager;
    }

    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public RPGStorageManager getStorageManager() {
        return this.storageManager;
    }

    public RPGPluginProperties getProperties() {
        return this.properties;
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
    public SpellManager getSpellManager() {
        return this.spellManager;
    }

    @Override
    public RoleManager getRoleManager() {
        return this.roleManager;
    }

    @Override
    public void log(Level level, String msg) {

    }

    @Override
    public void debugLog(Level level, String msg) {

    }

    @Override
    public void debugThrow(String sourceClass, String sourceMethod, Throwable thrown) {

    }
}
