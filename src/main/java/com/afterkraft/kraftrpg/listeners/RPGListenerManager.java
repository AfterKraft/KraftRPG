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
package com.afterkraft.kraftrpg.listeners;

import java.util.ArrayList;
import java.util.List;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.api.listeners.ListenerManager;

/**
 * Standard implementatino of the ListenerManager
 */
public class RPGListenerManager implements ListenerManager {

    private KraftRPGPlugin plugin;

    private List<AbstractListener> listeners = new ArrayList<>();

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
