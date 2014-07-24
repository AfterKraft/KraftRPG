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
package com.afterkraft.kraftrpg.entity;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.Validate;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.CombatTracker;
import com.afterkraft.kraftrpg.api.entity.EnterCombatReason;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.Sentient;

public class RPGCombatTracker implements CombatTracker {
    private RPGPlugin plugin;
    private WeakHashMap<Insentient, WeakHashMap<Insentient, EnterCombatReason>> masterCombatMap;

    public RPGCombatTracker(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        this.masterCombatMap = new WeakHashMap<Insentient, WeakHashMap<Insentient, EnterCombatReason>>();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Map<Insentient, EnterCombatReason> getCombatants(Insentient target) {
        Validate.notNull(target, "Cannot get the combatants for a null target!");
        ImmutableMap.Builder<Insentient, EnterCombatReason> builder = ImmutableMap.builder();
        for (Entry<Insentient, WeakHashMap<Insentient, EnterCombatReason>> entry : this.masterCombatMap.entrySet()) {
            if (entry.getValue().containsKey(target)) {
                builder.put(entry.getKey(), entry.getValue().get(target));
            }
        }
        return builder.build();
    }

    @Override
    public void enterCombatWith(Insentient target, Insentient attacker, EnterCombatReason reason) {
        Validate.notNull(target, "Cannot enter combat with a null target!");
        Validate.notNull(attacker, "Cannot enter combat with a null attacker!");
        Validate.notNull(reason, "Cannot enter combat with a null reason!");
        WeakHashMap<Insentient, EnterCombatReason> map = this.masterCombatMap.get(attacker);
        if (map == null) {
            map = new WeakHashMap<Insentient, EnterCombatReason>();
        }
        map.put(target, reason);
        this.masterCombatMap.put(attacker, map);
    }

    @Override
    public void leaveCombatWith(Insentient target, Insentient attacker, LeaveCombatReason reason) {
        Validate.notNull(target, "Cannot enter combat with a null target!");
        Validate.notNull(attacker, "Cannot enter combat with a null attacker!");
        Validate.notNull(reason, "Cannot enter combat with a null reason!");
        WeakHashMap<Insentient, EnterCombatReason> map = this.masterCombatMap.get(attacker);
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
    public boolean isInCombatWith(Insentient target, Insentient potentialAttacker) {
        // TODO Auto-generated method stub
        return false;
    }

}
