package com.afterkraft.kraftrpg.entity.effects;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.effects.Periodic;
import com.afterkraft.kraftrpg.api.spells.Spell;
import com.afterkraft.kraftrpg.api.spells.SpellArgument;

/**
 * @author gabizou
 */
public abstract class RPGPeriodicEffect extends RPGEffect implements Periodic {

    private final long period;
    protected long lastTickTime = 0;

    public RPGPeriodicEffect(Spell<? extends SpellArgument> spell, String name, long period) {
        super(spell, name);
        this.period = period;
    }

    public RPGPeriodicEffect(RPGPlugin plugin, String name, long period) {
        super(plugin, null, name);
        this.period = period;
    }

    @Override
    public long getLastTickTime() {
        return this.lastTickTime;
    }

    @Override
    public long getPeriod() {
        return this.period;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void tick(IEntity entity) {
        this.lastTickTime = System.currentTimeMillis();
        if (entity instanceof Champion) {
            this.tickChampion((Champion) entity);
        } else if (entity instanceof Monster) {
            this.tickMonster((Monster) entity);
        }
    }

    @Override
    public void tickMonster(Monster monster) { }

    @Override
    public void tickChampion(Champion player) { }
}
