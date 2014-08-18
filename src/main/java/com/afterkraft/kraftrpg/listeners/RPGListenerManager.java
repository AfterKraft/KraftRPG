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
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.api.listeners.ListenerManager;

public class RPGListenerManager implements ListenerManager {

    private KraftRPGPlugin plugin;

    private List<AbstractListener> listeners = new ArrayList<AbstractListener>();

    public RPGListenerManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        if (this.listeners.isEmpty()) {
            loadListeners();
        }
    }

    @Override
    public void shutdown() {
        if (!this.listeners.isEmpty()) {
            for (AbstractListener listener : this.listeners) {
                listener.shutdown();
            }
            this.listeners.clear();
        }
    }

    private void loadListeners() {
        addListener(new DamageListener(this.plugin));
        addListener(new EntityListener(this.plugin));
        addListener(new InventoryListener(this.plugin));
        addListener(new PlayerListener(this.plugin));
        addListener(new EffectsListener(this.plugin));
    }

    @Override
    public void addListener(AbstractListener listener) {
        if (listener != null && !this.listeners.contains(listener)) {
            this.listeners.add(listener);
            listener.initialize();
        }
    }
}
