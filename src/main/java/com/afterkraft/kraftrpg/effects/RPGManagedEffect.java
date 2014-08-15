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
package com.afterkraft.kraftrpg.effects;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.effects.IEffect;
import com.afterkraft.kraftrpg.api.effects.Managed;
import com.afterkraft.kraftrpg.api.effects.Timed;


public class RPGManagedEffect implements Managed {

    private final Timed effect;
    private final Insentient entity;

    public RPGManagedEffect(Insentient entity, Timed IEffect) {
        this.effect = IEffect;
        this.entity = entity;
    }

    @Override
    public IEffect getEffect() {
        return (IEffect) this.effect;
    }

    @Override
    public Insentient getSentientBeing() {
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

    @Override
    public long getDelay(TimeUnit unit) {
        return this.effect.getDelay(unit);

    }

    @Override
    public int compareTo(Delayed o) {
        return this.effect.compareTo(o);
    }
}
