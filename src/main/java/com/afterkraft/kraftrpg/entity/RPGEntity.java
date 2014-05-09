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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.LivingEntity;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.IEntity;

public abstract class RPGEntity implements IEntity {

    protected final RPGPlugin plugin;
    protected final String name;
    protected final UUID uuid;
    protected WeakReference<LivingEntity> lEntity;
    protected Map<String, Double> healthMap = new ConcurrentHashMap<String, Double>();

    public RPGEntity(RPGPlugin plugin, LivingEntity lEntity, String name) {
        this.plugin = plugin;
        this.lEntity = new WeakReference<LivingEntity>(lEntity);
        this.name = name != null ? name : lEntity.getType().name();
        this.uuid = lEntity.getUniqueId();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public final boolean isValid() {
        return lEntity.get() != null;
    }

    @Override
    public final boolean isEntityValid() {
        return this.isValid() && this.getEntity().isValid();
    }

    /**
     * Returns the entity associated with this character if the entity has not
     * been garbage collected already (in which case, this RPGEntity will need
     * to be removed from the system.
     *
     * @return the associated LivingEntity for this RPGEntity or null if the
     * LivingEntity no longer exists
     */
    @Override
    public LivingEntity getEntity() {
        if (!isEntityValid()) {
            return null;
        }
        return lEntity.get();
    }

    @Override
    public final boolean setEntity(LivingEntity entity) {
        if (!this.uuid.equals(entity.getUniqueId())) {
            return false;
        }
        this.lEntity = new WeakReference<LivingEntity>(entity);
        return true;
    }

    /**
     * Adds to the character's maximum health.  Maps the amount of health to add
     * to a key. This operation will fail if the character already has a health
     * value with the specific key. This operation will add health to the
     * character's current health.
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
     * Thread-Safe Removes a maximum health addition on the character. This will
     * also remove current health from the character, down to a minimum of 1.
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
    public int hashCode() {
        return this.isEntityValid() ? this.uuid.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RPGEntity) && this.isEntityValid() &&
                ((RPGEntity) obj).isEntityValid() &&
                ((RPGEntity) obj).getEntity().getUniqueId().equals(this.getEntity().getUniqueId());
    }

}
