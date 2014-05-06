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
package com.afterkraft.kraftrpg.listeners;

import java.util.ArrayList;
import java.util.List;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.Manager;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.listeners.bukkit.BukkitDamageListener;
import com.afterkraft.kraftrpg.listeners.bukkit.BukkitEntityListener;
import com.afterkraft.kraftrpg.listeners.bukkit.BukkitPlayerListener;
import com.afterkraft.kraftrpg.listeners.common.AbstractListener;
import com.afterkraft.kraftrpg.listeners.spigot.SpigotDamageListener;
import com.afterkraft.kraftrpg.listeners.spigot.SpigotPlayerListener;
import com.afterkraft.kraftrpg.listeners.tweakkit.TweakkitDamageListener;
import com.afterkraft.kraftrpg.listeners.tweakkit.TweakkitPlayerListener;

public class ListenerManager implements Manager {

    private KraftRPGPlugin plugin;

    private List<AbstractListener> listeners = new ArrayList<AbstractListener>();

    public ListenerManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        if (listeners.isEmpty()) {
            switch (CraftBukkitHandler.serverType) {
                case BUKKIT:
                    listeners = loadBukkitListeners();
                    break;
                case SPIGOT:
                    listeners = loadSpigotListeners();
                    break;
                case TWEAKKIT:
                    listeners = loadTweakkitListeners();
                    break;
                default:
                    break;
            }
            for (AbstractListener listener : listeners) {
                listener.initialize();
            }
        }
    }

    @Override
    public void shutdown() {
        if (!listeners.isEmpty()) {
            for (AbstractListener listener : listeners) {
                listener.shutdown();
            }
        }
    }

    private List<AbstractListener> loadBukkitListeners() {
        List<AbstractListener> list = new ArrayList<AbstractListener>();
        list.add(new BukkitPlayerListener(plugin));
        list.add(new BukkitDamageListener(plugin));
        list.add(new BukkitEntityListener(plugin));
        return list;
    }

    private List<AbstractListener> loadSpigotListeners() {
        List<AbstractListener> list = new ArrayList<AbstractListener>();
        list.add(new SpigotPlayerListener(plugin));
        list.add(new SpigotDamageListener(plugin));
        list.add(new BukkitEntityListener(plugin));
        return list;
    }

    private List<AbstractListener> loadTweakkitListeners() {
        List<AbstractListener> list = new ArrayList<AbstractListener>();
        list.add(new TweakkitPlayerListener(plugin));
        list.add(new TweakkitDamageListener(plugin));
        list.add(new BukkitEntityListener(plugin));
        return list;
    }
}
