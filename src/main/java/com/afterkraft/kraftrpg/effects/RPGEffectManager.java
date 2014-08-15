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
package com.afterkraft.kraftrpg.effects;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.logging.Level;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.effects.EffectManager;
import com.afterkraft.kraftrpg.api.effects.Expirable;
import com.afterkraft.kraftrpg.api.effects.Managed;
import com.afterkraft.kraftrpg.api.effects.Periodic;
import com.afterkraft.kraftrpg.api.effects.Timed;

public class RPGEffectManager implements EffectManager {

    private final static int EFFECT_INTERVAL = 2;
    private final Set<Managed> managedEffects = new HashSet<Managed>();
    private final Set<Managed> pendingRemovals = new HashSet<Managed>();
    private final Set<Managed> pendingAdditions = new HashSet<Managed>();
    private final RPGPlugin plugin;
    private int taskID = 0;

    public RPGEffectManager(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void manageEffect(Insentient being, Timed effect) {
        if ((effect instanceof Expirable) || (effect instanceof Periodic)) {
            this.pendingAdditions.add(new RPGManagedEffect(being, effect));
        }
    }

    @Override
    public void queueRemoval(Insentient being, Timed effect) {
        final RPGManagedEffect mEffect = new RPGManagedEffect(being, effect);
        if (this.managedEffects.contains(mEffect)) {
            this.pendingRemovals.add(mEffect);
        }
    }

    @Override
    public void initialize() {
        if (this.taskID != 0) {
            throw new IllegalStateException("RPGEffectManager is already initalized!");
        }
        this.taskID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new EffectUpdater(), 0, EFFECT_INTERVAL);
    }

    @Override
    public void shutdown() {
        this.plugin.getServer().getScheduler().cancelTask(this.taskID);
        this.taskID = 0;
    }

    class EffectUpdater implements Runnable {

        @Override
        public void run() {
            final Set<Managed> removals = new HashSet<Managed>(RPGEffectManager.this.pendingRemovals);
            RPGEffectManager.this.pendingRemovals.clear();
            for (final Managed managed : removals) {
                RPGEffectManager.this.managedEffects.remove(managed);
            }

            final Set<Managed> additions = new HashSet<Managed>(RPGEffectManager.this.pendingAdditions);
            RPGEffectManager.this.pendingAdditions.clear();
            for (final Managed managed : additions) {
                RPGEffectManager.this.managedEffects.add(managed);
            }

            for (final Managed managed : RPGEffectManager.this.managedEffects) {
                if (managed.getEffect() instanceof Expirable) {
                    if (((Expirable) managed.getEffect()).isExpired()) {
                        try {
                            managed.getSentientBeing().removeEffect(managed.getEffect());
                        } catch (final Exception e) {
                            RPGEffectManager.this.plugin.log(Level.SEVERE, "There was an error attempting to remove effect: " + managed.getEffect().getName());
                            e.printStackTrace();
                        }
                    }
                }
                if (managed.getEffect() instanceof Periodic) {
                    final Periodic periodic = (Periodic) managed.getEffect();
                    try {
                        if (periodic.isReady()) {
                            periodic.tick(managed.getSentientBeing());
                        }
                    } catch (final Exception e) {
                        RPGEffectManager.this.plugin.log(Level.SEVERE, "There was an error attempting to tick effect: " + managed.getEffect().getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
