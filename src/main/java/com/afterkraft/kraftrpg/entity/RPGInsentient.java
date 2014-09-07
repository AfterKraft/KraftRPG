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
package com.afterkraft.kraftrpg.entity;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.mutable.MutableInt;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.effects.EffectType;
import com.afterkraft.kraftrpg.api.effects.IEffect;
import com.afterkraft.kraftrpg.api.effects.Timed;
import com.afterkraft.kraftrpg.api.events.effects.EffectAddEvent;
import com.afterkraft.kraftrpg.api.events.effects.EffectRemoveEvent;


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
        Validate.isTrue(mana > 0, "Cannot set a negative mana value!");
        this.mana.setValue(mana);
    }

    @Override
    public double getHealth() {
        check();
        return this.getEntity() != null ? this.getEntity().getHealth() : 0;
    }

    @Override
    public void setHealth(double health) {
        check();
        Validate.isTrue(health > 0, "Cannot set health to less than zero!");
        if (this.isEntityValid()) {
            this.getEntity().setHealth(health);
        }
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
    public boolean addMaxHealth(String key, double value) {
        check();
        Validate.notNull(key, "Cannot add a MaxHealth module with a null key!");
        Validate.isTrue(!key.isEmpty(), "Cannot add a MaxHealth module with an empty key!");
        if (!this.isEntityValid()) {
            return false;
        }
        // can't add maxHealth to dead players
        if (this.getEntity().getHealth() < 1) {
            return false;
        }
        if (this.healthMap.containsKey(key)) {
            return false;
        }
        this.healthMap.put(key, value);
        this.getEntity().setMaxHealth(this.getEntity().getMaxHealth() + value);
        this.getEntity().setHealth(value + this.getEntity().getHealth());
        return true;
    }

    @Override
    public boolean removeMaxHealth(String key) {
        check();
        Validate.notNull(key, "Cannot remove a null key health module!");
        Validate.isTrue(!key.isEmpty(), "Cannot remove an empty key health module!");
        if (!this.isEntityValid()) {
            return false;
        }
        LivingEntity entity = this.getEntity();
        Double old = this.healthMap.remove(key);
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
        check();
        // TODO
        return 0;
    }

    @Override
    public void heal(double amount) {
        check();
        // TODO

    }

    @Override
    public void clearHealthBonuses() {
        check();
        double current = this.getEntity().getHealth();
        Iterator<Map.Entry<String, Double>> iter = this.healthMap.entrySet().iterator();
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
        check();
        return this.getEntity() == null || this.getEntity().isDead();
    }

    @Override
    public void updateInventory() {
        check();
    }

    @Override
    public ItemStack getItemInHand() {
        check();
        return this.isEntityValid() ? this.getEntity().getEquipment().getItemInHand() : null;
    }

    @Override
    public Inventory getInventory() {
        check();
        return this.isEntityValid() ? (this.getEntity() instanceof InventoryHolder) ? ((InventoryHolder) this.getEntity()).getInventory() : null : null;
    }

    @Override
    public IEffect getEffect(String name) {
        check();
        if (name != null) {
            return this.effects.get(name.toLowerCase());
        }
        return null;
    }

    @Override
    public Set<IEffect> getEffects() {
        check();
        return ImmutableSet.copyOf(this.effects.values());
    }

    @Override
    public void addEffect(IEffect effect) {
        check();
        Validate.notNull(effect, "Cannot add a null IEffect!");

        if (hasEffect(effect.getName())) {
            removeEffect(getEffect(effect.getName()));
        }

        EffectAddEvent event = new EffectAddEvent(this, effect);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        this.effects.put(effect.getName().toLowerCase(), effect);
        effect.apply(this);

        if (effect instanceof Timed) {
            this.plugin.getEffectManager().manageEffect(this, (Timed) effect);
        }
    }

    @Override
    public void addPotionEffect(PotionEffect potion) {
        check();
        Validate.notNull(potion, "Cannot add a null PotionEffect!");
        if (this.isEntityValid()) {
            this.potionEffectQueue.add(
                    new RPGPotionEffect(
                            new PotionEffect(potion.getType(), potion.getDuration(), potion.getAmplifier(), potion.isAmbient()), true));
        }
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        check();
        Validate.notNull(type, "Cannot remove a null potion effect type!");
        if (this.isEntityValid()) {
            this.potionEffectQueue.add(
                    new RPGPotionEffect(
                            new PotionEffect(type, 0, 0), false));
        }
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        check();
        Validate.notNull(type, "Cannot check on a null potion effect type!");
        return this.getEntity() != null && this.getEntity().hasPotionEffect(type);
    }

    @Override
    public boolean hasEffect(String name) {
        check();
        Validate.notNull(name, "Cannot check if an effect with a null name!");
        Validate.isTrue(!name.isEmpty(), "Cannot check an empty effect name!");
        return this.effects.containsKey(name.toLowerCase());
    }

    @Override
    public boolean hasEffectType(EffectType type) {
        check();
        Validate.notNull(type, "Cannot check on a null effect type!");
        for (final IEffect effect : this.effects.values()) {
            if (effect.isType(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeEffect(IEffect effect) {
        check();
        Validate.notNull(effect, "Cannot remove a null effect!");

        // TODO
        EffectRemoveEvent event = new EffectRemoveEvent(this, effect);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        effect.remove(this);
        this.effects.remove(effect.getName().toLowerCase());

        if (effect instanceof Timed) {
            this.plugin.getEffectManager().queueRemoval(this, (Timed) effect);
        }
    }

    @Override
    public void manualRemoveEffect(IEffect effect) {
        check();
        Validate.notNull(effect, "Cannot remove a null effect!");
        this.effects.remove(effect.getName().toLowerCase());

        if (effect instanceof Timed) {
            this.plugin.getEffectManager().queueRemoval(this, (Timed) effect);
        }
    }

    @Override
    public void clearEffects() {
        check();
        if (this.isEntityValid()) {
            for (final IEffect effect : this.getEffects()) {
                this.removeEffect(effect);
            }
        }
    }

    @Override
    public void manualClearEffects() {
        check();
        if (this.isEntityValid()) {
            for (final IEffect effect : this.getEffects()) {
                this.manualRemoveEffect(effect);
            }
        }
    }

    @Override
    public void sendMessage(String message) {
        check();

    }

    private void check() {
        if (!isValid()) {
            throw new IllegalStateException("The linked entity is no longer available!");
        }
    }
}
