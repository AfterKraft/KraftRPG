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
