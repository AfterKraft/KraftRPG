package com.afterkraft.kraftrpg.entity.effects;

import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.effects.Effect;
import com.afterkraft.kraftrpg.api.entity.effects.ManagedEffect;

/**
 * @author gabizou
 */
public class RPGManagedEffect implements ManagedEffect {

    private final Effect effect;
    private final IEntity entity;

    public RPGManagedEffect(IEntity entity, Effect effect) {
        this.effect = effect;
        this.entity = entity;
    }

    public Effect getEffect() {
        return this.effect;
    }

    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 7;
        result = (prime * result) + this.effect.hashCode();
        result = (prime * result) + this.entity.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        }
        final RPGManagedEffect other = (RPGManagedEffect) obj;
        if (!this.effect.equals(other.effect)) {
            return false;
        } else if (!this.entity.equals(other.entity)) {
            return false;
        }
        return true;
    }
}
