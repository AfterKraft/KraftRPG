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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.mutable.MutableInt;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.effects.EffectType;
import com.afterkraft.kraftrpg.api.entity.effects.IEffect;
import com.afterkraft.kraftrpg.api.entity.effects.Timed;


public abstract class RPGInsentient extends RPGEntity implements Insentient {

    protected final Map<String, IEffect> effects = new HashMap<String, IEffect>();
    protected final ArrayDeque<RPGPotionEffect> potionEffectQueue = new ArrayDeque<RPGPotionEffect>();
    protected MutableInt mana = new MutableInt(0);
    protected Map<String, Double> healthMap = new ConcurrentHashMap<String, Double>();

    protected RPGInsentient(RPGPlugin plugin, LivingEntity lEntity, String name) {
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
    public LivingEntity getEntity() {
        return (LivingEntity) super.getEntity();
    }

    @Override
    public UUID getUniqueID() {
        return this.isEntityValid() ? this.getEntity().getUniqueId() : null;
    }

    @Override
    public Location getLocation() {
        return this.isEntityValid() ? this.getEntity().getLocation() : null;
    }

    @Override
    public World getWorld() {
        return this.isEntityValid() ? this.getLocation().getWorld() : null;
    }

    @Override
    public boolean isOnGround() {
        return this.isEntityValid() && this.getEntity().isOnGround();
    }

    @Override
    public void setHealth(double health) {
        if (this.isEntityValid()) {
            this.getEntity().setHealth(health);
        }
    }

    /**
     * Adds to the character's maximum health. Maps the amount of health to
     * add to a key. This operation will fail if the character already has a
     * health value with the specific key. This operation will add health to
     * the character's current health.
     * 
     * @param key to give
     * @param value amount
     * @return true if the operation was successful
     */
    @Override
    public boolean addMaxHealth(String key, double value) {
        if (!this.isEntityValid()) {
            return false;
        }
        // can't add maxHealth to dead players
        if (this.getEntity().getHealth() < 1) {
            return false;
        }
        if (healthMap.containsKey(key)) {
            return false;
        }
        healthMap.put(key, value);
        this.getEntity().setMaxHealth(this.getEntity().getMaxHealth() + value);
        this.getEntity().setHealth(value + this.getEntity().getHealth());
        return true;
    }

    /**
     * Thread-Safe Removes a maximum health addition on the character. This
     * will also remove current health from the character, down to a minimum
     * of 1.
     * 
     * @param key to remove
     * @return true if health was removed.
     */
    @Override
    public boolean removeMaxHealth(String key) {
        if (!this.isEntityValid()) {
            return false;
        }
        LivingEntity entity = this.getEntity();
        Double old = healthMap.remove(key);
        double currentHealth = entity.getHealth();
        if (old != null) {
            double newHealth = entity.getHealth() - old;
            if (currentHealth > 0 && newHealth < 1) {
                newHealth = 1;

            }
            if (this.getEntity().getHealth() != 0) {
                entity.setHealth(newHealth);
            }
            entity.setMaxHealth(entity.getMaxHealth() - old);
            return true;
        }
        return false;
    }

    @Override
    public double recalculateMaxHealth() {
        return 0;
    }

    @Override
    public void heal(double amount) {

    }

    /**
     * Thread-Safe removes all max-health bonuses from the character.
     */
    @Override
    public void clearHealthBonuses() {
        if (!this.isEntityValid()) {
            return;
        }
        double current = this.getEntity().getHealth();
        Iterator<Map.Entry<String, Double>> iter = healthMap.entrySet().iterator();
        int minus = 0;
        while (iter.hasNext()) {
            Double val = iter.next().getValue();
            iter.remove();
            current -= val;
            minus += val;
        }
        if (this.getEntity().getHealth() != 0) {
            this.getEntity().setHealth(current < 0 ? 0 : current);
        }
        this.getEntity().setMaxHealth(this.getEntity().getMaxHealth() - minus);
    }

    @Override
    public boolean isDead() {
        return this.getEntity() == null || this.getEntity().isDead();
    }

    @Override
    public void updateInventory() {

    }

    @Override
    public ItemStack getItemInHand() {
        return this.isEntityValid() ? this.getEntity().getEquipment().getItemInHand() : null;
    }

    @Override
    public Inventory getInventory() {
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
        return ImmutableSet.copyOf(this.effects.values());
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
    public void removePotionEffect(PotionEffectType type) {
        if (type == null) {
            return;
        }
        if (this.isEntityValid()) {
            potionEffectQueue.add(new RPGPotionEffect(new PotionEffect(type, 0, 0), false));
        }
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return this.getEntity() != null && this.getEntity().hasPotionEffect(type);
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

    @Override
    public void sendMessage(String message) {

    }
}
