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
package com.afterkraft.kraftrpg.entity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.CombatTracker;
import com.afterkraft.kraftrpg.api.entity.EnterCombatReason;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;

/**
 * Default implementation of the Combat Tracker.
 */
public class RPGCombatTracker implements CombatTracker {

    private RPGPlugin plugin;
    private WeakHashMap<Insentient, WeakHashMap<Insentient, EnterCombatReason>>
            masterCombatMap;

    public RPGCombatTracker(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        this.masterCombatMap =
                new WeakHashMap<>();
    }

    @Override
    public void shutdown() {
        this.masterCombatMap.clear();
    }

    @Override
    public Map<Insentient, EnterCombatReason> getCombatants(Insentient target) {
        checkNotNull(target, "Cannot get the combatants for a null target!");
        ImmutableMap.Builder<Insentient, EnterCombatReason> builder =
                ImmutableMap.builder();
        for (Entry<Insentient, WeakHashMap<Insentient, EnterCombatReason>>
                entry :
                this.masterCombatMap.entrySet()) {
            if (entry.getValue().containsKey(target)) {
                builder.put(entry.getKey(), entry.getValue().get(target));
            }
        }
        return builder.build();
    }

    @Override
    public void enterCombatWith(Insentient target, Insentient attacker,
            EnterCombatReason reason) {
        checkNotNull(target, "Cannot enter combat with a null target!");
        checkNotNull(attacker, "Cannot enter combat with a null attacker!");
        checkNotNull(reason, "Cannot enter combat with a null reason!");
        WeakHashMap<Insentient, EnterCombatReason> map =
                this.masterCombatMap.get(attacker);
        if (map == null) {
            map = new WeakHashMap<>();
        }
        map.put(target, reason);
        this.masterCombatMap.put(attacker, map);
    }

    @Override
    public void leaveCombatWith(Insentient target, Insentient attacker,
            LeaveCombatReason reason) {
        checkNotNull(target, "Cannot enter combat with a null target!");
        checkNotNull(attacker, "Cannot enter combat with a null attacker!");
        checkNotNull(reason, "Cannot enter combat with a null reason!");
        WeakHashMap<Insentient, EnterCombatReason> map =
                this.masterCombatMap.get(attacker);
        if (map != null) {
            map.remove(target);
        }
        this.masterCombatMap.put(attacker, map);
    }

    @Override
    public void leaveCombat(Insentient target, LeaveCombatReason reason) {

    }

    @Override
    public boolean isInCombat(Insentient being) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInCombatWith(Insentient target,
            Insentient potentialAttacker) {
        // TODO Auto-generated method stub
        return false;
    }

}
