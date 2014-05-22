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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.mutable.MutableInt;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.effects.EffectType;
import com.afterkraft.kraftrpg.api.entity.effects.IEffect;
import com.afterkraft.kraftrpg.api.entity.effects.Timed;


public abstract class RPGEntityInsentient extends RPGEntity implements Insentient {

    protected final Map<String, IEffect> effects = new HashMap<String, IEffect>();
    protected final ArrayDeque<RPGPotionEffect> potionEffectQueue = new ArrayDeque<RPGPotionEffect>();
    protected MutableInt mana = new MutableInt(0);

    protected RPGEntityInsentient(RPGPlugin plugin, LivingEntity lEntity, String name) {
        super(plugin, lEntity, name);
    }

    @Override
    public int getMana() {
        return this.mana.intValue();
    }

    @Override
    public void setMana(int mana) {
        this.mana.setValue(mana);
    }

    @Override
    public double getHealth() {
        return this.getEntity() != null ? this.getEntity().getHealth() : 0;
    }

    @Override
    public void setHealth(double health) {
        if (this.isEntityValid()) {
            this.getEntity().setHealth(health);
        }
    }

    @Override
    public Location getLocation() {
        if (this.isEntityValid()) {
            return this.getEntity().getLocation();
        }
        return null;
    }

    @Override
    public IEffect getEffect(String name) {
        if (name != null) {
            return this.effects.get(name.toLowerCase());
        }
        return null;
    }

    @Override
    public Set<IEffect> getEffects() {
        return new HashSet<IEffect>(this.effects.values());
    }

    @Override
    public void addEffect(IEffect effect) {
        if (effect == null) {
            return;
        }
        if (hasEffect(effect.getName())) {
            removeEffect(getEffect(effect.getName()));
        }

        effects.put(effect.getName().toLowerCase(), effect);
        effect.apply(this);

        if (effect instanceof Timed) {
            plugin.getEffectManager().manageEffect(this, (Timed) effect);
        }
    }

    @Override
    public void addPotionEffect(PotionEffect potion) {
        if (potion == null) {
            return;
        }
        if (this.isEntityValid()) {
            potionEffectQueue.add(new RPGPotionEffect(potion, true));
        }
    }

    @Override
    public boolean hasEffect(String name) {
        return name != null && this.effects.containsKey(name.toLowerCase());
    }

    @Override
    public boolean hasEffectType(EffectType type) {
        if (type == null) {
            return false;
        }
        for (final IEffect effect : this.effects.values()) {
            if (effect.isType(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeEffect(IEffect effect) {
        if (effect != null) {
            effect.remove(this);
            effects.remove(effect.getName().toLowerCase());

            if (effect instanceof Timed) {
                plugin.getEffectManager().queueRemoval(this, (Timed) effect);
            }
        }
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        if (type == null) {
            return;
        }
        if (this.isEntityValid()) {
            potionEffectQueue.add(new RPGPotionEffect(new PotionEffect(type, 0, 0), false));
        }
    }

    @Override
    public void manualRemoveEffect(IEffect effect) {
        if (effect != null) {
            effects.remove(effect.getName().toLowerCase());

            if (effect instanceof Timed) {
                plugin.getEffectManager().queueRemoval(this, (Timed) effect);
            }
        }
    }

    @Override
    public void clearEffects() {
        if (this.isEntityValid()) {
            for (final IEffect effect : this.getEffects()) {
                this.removeEffect(effect);
            }
        }
    }

    @Override
    public void manualClearEffects() {
        if (this.isEntityValid()) {
            for (final IEffect effect : this.getEffects()) {
                this.manualRemoveEffect(effect);
            }
        }
    }
}
