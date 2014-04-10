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
public abstract class RPGPeriodicExpirableEffect extends RPGExpirableEffect implements Periodic {

    private final long period;
    protected long lastTickTime = 0;

    public RPGPeriodicExpirableEffect(Spell<? extends SpellArgument> spell, String name, long period, long duration) {
        super(spell, name, duration);
        this.period = period;
    }

    public RPGPeriodicExpirableEffect(Spell<? extends SpellArgument> spell, RPGPlugin plugin, String name, long period, long duration) {
        super(spell, plugin, name, duration);
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
        return System.currentTimeMillis() >= (this.lastTickTime + this.period);
    }

    @Override
    public void tick(IEntity character) {
        this.lastTickTime = System.currentTimeMillis();
        if (character instanceof Champion) {
            this.tickChampion((Champion) character);
        } else if (character instanceof Monster) {
            this.tickMonster((Monster) character);
        }
    }
}
