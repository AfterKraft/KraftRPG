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
package com.afterkraft.kraftrpg.effects;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.effects.EffectManager;
import com.afterkraft.kraftrpg.api.effects.Expirable;
import com.afterkraft.kraftrpg.api.effects.Managed;
import com.afterkraft.kraftrpg.api.effects.Periodic;
import com.afterkraft.kraftrpg.api.effects.Timed;
import com.afterkraft.kraftrpg.api.entity.Insentient;

/**
 * Default implementation of EffectManager
 */
public class RPGEffectManager implements EffectManager {

    private static final int EFFECT_INTERVAL = 2;
    private final Set<Managed> managedEffects = new HashSet<>();
    private final Set<Managed> pendingRemovals = new HashSet<>();
    private final Set<Managed> pendingAdditions = new HashSet<>();
    private final KraftRPGPlugin plugin;
    @Nullable
    private UUID taskID;

    public RPGEffectManager(KraftRPGPlugin plugin) {
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
        if (this.taskID != null) {
            throw new IllegalStateException(
                    "RPGEffectManager is already initalized!");
        }
        this.taskID = RpgCommon.getGame()
                .getSyncScheduler()
                .runRepeatingTask(this.plugin,
                        new EffectUpdater(),
                        EFFECT_INTERVAL).get().getUniqueId();
    }

    @Override
    public void shutdown() {
        if (this.taskID != null) {
            RpgCommon.getGame()
                    .getSyncScheduler()
                    .getTaskById(this.taskID).get().cancel();
            this.taskID = null;
        }
    }

    class EffectUpdater implements Runnable {

        @Override
        public void run() {
            final Set<Managed> removals =
                    new HashSet<>(RPGEffectManager.this.pendingRemovals);
            RPGEffectManager.this.pendingRemovals.clear();
            for (final Managed managed : removals) {
                RPGEffectManager.this.managedEffects.remove(managed);
            }

            final Set<Managed> additions =
                    new HashSet<>(RPGEffectManager.this.pendingAdditions);
            RPGEffectManager.this.pendingAdditions.clear();
            for (final Managed managed : additions) {
                RPGEffectManager.this.managedEffects.add(managed);
            }

            for (final Managed managed : RPGEffectManager.this.managedEffects) {
                if (managed.getEffect() instanceof Expirable) {
                    if (((Expirable) managed.getEffect()).isExpired()) {
                        try {
                            managed.getSentientBeing()
                                    .removeEffect(managed.getEffect());
                        } catch (final Exception e) {
                            RPGEffectManager.this.plugin.getLogger()
                                    .error("There "
                                            + "was an "
                                            + "error attempting to "
                                            + "remove effect: "
                                            + managed.getEffect().getName());
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
                        RPGEffectManager.this
                                .plugin.getLogger()
                                .error("There was an error attempting to "
                                        + "tick effect: "
                                        + managed.getEffect().getName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
