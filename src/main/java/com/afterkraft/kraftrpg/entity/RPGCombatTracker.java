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

import java.util.WeakHashMap;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.CombatTracker;
import com.afterkraft.kraftrpg.api.entity.EnterCombatReason;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.Sentient;

public class RPGCombatTracker implements CombatTracker {
    private RPGPlugin plugin;
    private WeakHashMap<Sentient, WeakHashMap<Sentient, EnterCombatReason>> masterCombatMap;

    public RPGCombatTracker(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        masterCombatMap = new WeakHashMap<Sentient, WeakHashMap<Sentient, EnterCombatReason>>();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void enterCombat(Sentient target, EnterCombatReason reason) {
        // TODO Auto-generated method stub

    }

    @Override
    public void enterCombatWith(Sentient target, Sentient attacker, EnterCombatReason reason) {
        // TODO Auto-generated method stub

    }

    @Override
    public void leaveCombat(Sentient target, LeaveCombatReason reason) {
        // TODO Auto-generated method stub

    }

    @Override
    public void leaveCombatWith(Sentient target, Sentient attacker, LeaveCombatReason reason) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInCombat(Sentient sentient) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInCombatWith(Sentient target, Sentient potentialAttacker) {
        // TODO Auto-generated method stub
        return false;
    }

}
