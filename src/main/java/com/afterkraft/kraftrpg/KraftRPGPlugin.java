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
import com.afterkraft.kraftrpg.api.entity.RPGEntityManager;
import com.afterkraft.kraftrpg.api.entity.effects.RPGEffect;
import com.afterkraft.kraftrpg.api.spells.SpellManager;

/**
 * Author: gabizou
 */
public final class KraftRPGPlugin extends JavaPlugin implements RPGPlugin {

    @Override
    public Class<? extends RPGEffect> getEffectClass() {
        return null;
    }

    @Override
    public Class<? extends RPGEntityManager> getEntityManagerClass() {
        return null;
    }

    @Override
    public Class<? extends SpellManager> getSpellManagerClass() {
        return null;
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
