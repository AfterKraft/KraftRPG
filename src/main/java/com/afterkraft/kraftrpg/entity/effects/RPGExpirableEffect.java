package com.afterkraft.kraftrpg.entity.effects;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.effects.Expirable;
import com.afterkraft.kraftrpg.api.spells.Spell;
import com.afterkraft.kraftrpg.api.spells.SpellArgument;

/**
 * @author gabizou
 */
public class RPGExpirableEffect extends RPGEffect implements Expirable {

    private final long duration;
    private long expireTime;

    public RPGExpirableEffect(Spell<? extends SpellArgument> skill, String name, long duration) {
        super(skill, name);
        this.duration = duration;
    }

    public RPGExpirableEffect(Spell<? extends SpellArgument> skill, RPGPlugin plugin, String name, long duration) {
        super(plugin, skill, name);
        this.duration = duration;
    }

    @Override
    public void applyToMonster(Monster monster) {
        super.applyToMonster(monster);
        this.expireTime = this.applyTime + this.duration;
    }

    @Override
    public void applyToPlayer(Champion champion) {
        super.applyToPlayer(champion);
        this.expireTime = this.applyTime + this.duration;
    }

    @Override
    public long getApplyTime() {
        return this.applyTime;
    }

    @Override
    public long getDuration() {
        return this.duration;
    }

    @Override
    public long getExpiry() {
        return this.expireTime;
    }

    @Override
    public long getRemainingTime() {
        return this.expireTime - System.currentTimeMillis();
    }

    @Override
    public boolean isExpired() {
        return !this.isPersistent() && System.currentTimeMillis() >= this.getExpiry();
    }

    @Override
    public void expire() {
        this.expireTime = System.currentTimeMillis();
    }

}
