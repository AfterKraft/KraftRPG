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
package com.afterkraft.kraftrpg.effects;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.afterkraft.kraftrpg.api.effects.IEffect;
import com.afterkraft.kraftrpg.api.effects.Managed;
import com.afterkraft.kraftrpg.api.effects.Timed;
import com.afterkraft.kraftrpg.api.entity.Insentient;

/**
 * Default implementatino of a Managed Effect.
 */
public class RPGManagedEffect implements Managed {

    private final Timed effect;
    private final Insentient entity;

    public RPGManagedEffect(Insentient entity, Timed effect) {
        this.effect = effect;
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
    public long getDelay(TimeUnit timeUnit) {
        return this.effect.getDelay(timeUnit);

    }

    @Override
    public int compareTo(Delayed o) {
        return this.effect.compareTo(o);
    }
}
