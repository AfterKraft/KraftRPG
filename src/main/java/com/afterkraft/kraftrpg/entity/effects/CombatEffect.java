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
package com.afterkraft.kraftrpg.entity.effects;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.EnterCombatReason;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.effects.Combat;
import com.afterkraft.kraftrpg.api.entity.effects.PeriodicEffect;
import com.afterkraft.kraftrpg.api.events.entity.LeaveCombatEvent;

public class CombatEffect extends PeriodicEffect implements Combat {

    private final Insentient being;
    private final WeakHashMap<LivingEntity, EnterCombatReason> combatMap = new WeakHashMap<LivingEntity, EnterCombatReason>();
    private WeakReference<LivingEntity> lastCombatEntity = null;

    public CombatEffect(Insentient being, RPGPlugin plugin) {
        super(plugin, "combat", plugin.getProperties().getCombatTime());
        this.being = being;
    }

    @Override
    public void tick(Insentient being) {
        if (!combatMap.isEmpty()) {
            combatMap.clear();
            Bukkit.getServer().getPluginManager().callEvent(new LeaveCombatEvent(being, combatMap, LeaveCombatReason.TIMED));
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }

    public boolean isInCombat() {
        if (combatMap.isEmpty()) {
            return false;
        } else if (isReady()) {
            combatMap.clear();
            return false;
        }
        return true;
    }

    @Override
    public boolean isReady() {
        return super.isReady();
    }

    @Override
    public boolean isInCombatWith(LivingEntity target) {
        return this.combatMap.containsKey(target);
    }

    @Override
    public void enterCombatWith(LivingEntity target, EnterCombatReason reason) {
        if (target != null && reason != null) {
            combatMap.put(target, reason);
            lastCombatEntity = new WeakReference<LivingEntity>(target);
            resetTimes();
        }
    }

    @Override
    public void leaveCombatWith(Insentient being, LivingEntity target, LeaveCombatReason reason) {
        if ((combatMap.remove(target) != null) && combatMap.isEmpty()) {
            lastCombatEntity = new WeakReference<LivingEntity>(target);
            being.leaveCombatWith(target, reason);
        }
    }

    @Override
    public void leaveCombatFromDeath(Insentient being) {

    }

    @Override
    public void leaveCombatFromLogout(Insentient being) {

    }

    @Override
    public void leaveCombatFromSuicide(Insentient being) {

    }

    @Override
    public long getTimeLeft() {
        return 0;
    }

    @Override
    public LivingEntity getLastCombatant() {
        return null;
    }

    @Override
    public WeakHashMap<LivingEntity, EnterCombatReason> getCombatants() {
        return null;
    }

    @Override
    public void clearCombatants() {

    }

    @Override
    public final void resetTimes() {
        this.applyTime = System.currentTimeMillis();
    }
}
