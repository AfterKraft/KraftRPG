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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.potion.PotionEffect;
import org.spongepowered.api.potion.PotionEffectType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.effects.EffectType;
import com.afterkraft.kraftrpg.api.effects.IEffect;
import com.afterkraft.kraftrpg.api.effects.Timed;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.resource.Resource;

import javax.annotation.Nullable;

/**
 * Default implementation of an Insentient being. By default, this is a Living.
 * It can overridden by creating a new implementation of RPGInsentient for
 * non-LivingEntities.
 */
public abstract class RPGInsentient extends RPGEntity implements Insentient {

    protected final Map<String, IEffect> effects = new HashMap<>();
    protected final ArrayDeque<RPGPotionEffect> potionEffectQueue =
            new ArrayDeque<>();
    protected AtomicInteger mana = new AtomicInteger(0);
    protected Map<String, Double> healthMap = new ConcurrentHashMap<>();

    protected RPGInsentient(RPGPlugin plugin, Living livingEntity,
                            String name) {
        super(plugin, livingEntity, name);
    }

    @Override
    public Optional<? extends Living> getEntity() {
        return Optional.fromNullable(this.getUnsafeEntity());
    }

    @Override
    Living getUnsafeEntity() {
        return (Living) super.getUnsafeEntity();
    }

    @Override
    public <T extends Resource> Optional<T> getResource(Class<T> clazz) {
        return null;
    }

    @Override
    public int getMana() {
        return this.mana.intValue();
    }

    @Override
    public void setMana(int mana) {
        checkArgument(mana > 0, "Cannot set a negative mana value!");
        this.mana.set(mana);
    }

    @Override
    public double getHealth() {
        check();
        return this.getUnsafeEntity().getHealth();
    }

    @Override
    public void setHealth(double health) {
        check();
        checkArgument(health > 0, "Cannot set health to less than zero!");
        if (this.isEntityValid()) {
            this.getUnsafeEntity().setHealth(health);
        }
    }

    @Override
    public boolean addMaxHealth(String key, double value) {
        check();
        checkNotNull(key, "Cannot add a MaxHealth module with a null key!");
        checkArgument(!key.isEmpty(),
                "Cannot add a MaxHealth module with an empty key!");
        if (!this.isEntityValid()) {
            return false;
        }
        // can't add maxHealth to dead players
        if (this.getUnsafeEntity().getHealth() < 1) {
            return false;
        }
        if (this.healthMap.containsKey(key)) {
            return false;
        }
        this.healthMap.put(key, value);
        this.getUnsafeEntity()
                .setMaxHealth(this.getUnsafeEntity().getMaxHealth() + value);
        this.getUnsafeEntity()
                .setHealth(value + this.getUnsafeEntity().getHealth());
        return true;
    }

    @Override
    public boolean removeMaxHealth(String key) {
        check();
        checkNotNull(key, "Cannot remove a null key health module!");
        checkArgument(!key.isEmpty(),
                "Cannot remove an empty key health module!");
        if (!this.isEntityValid()) {
            return false;
        }
        Living entity = this.getUnsafeEntity();
        Double old = this.healthMap.remove(key);
        double currentHealth = entity.getHealth();
        if (old != null) {
            double newHealth = entity.getHealth() - old;
            if (currentHealth > 0 && newHealth < 1) {
                newHealth = 1;

            }
            if (this.getUnsafeEntity().getHealth() != 0) {
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
        double current = this.getUnsafeEntity().getHealth();
        Iterator<Map.Entry<String, Double>> iter =
                this.healthMap.entrySet().iterator();
        int minus = 0;
        while (iter.hasNext()) {
            Double val = iter.next().getValue();
            iter.remove();
            current -= val;
            minus += val;
        }
        if (this.getUnsafeEntity().getHealth() != 0) {
            this.getUnsafeEntity().setHealth(current < 0 ? 0 : current);
        }
        this.getUnsafeEntity()
                .setMaxHealth(this.getUnsafeEntity().getMaxHealth() - minus);
    }

    @Override
    public boolean isDead() {
        check();
        return this.getUnsafeEntity().getHealth() <= 0;
    }

    @Override
    public void updateInventory() {
        check();
    }

    @Override
    public Optional<ItemStack> getItemInHand() {
        check();
        return this.isEntityValid() && this
                .getUnsafeEntity() instanceof ArmorEquipable
                ? ((ArmorEquipable) this.getUnsafeEntity()).getItemInHand()
                : Optional.<ItemStack>absent();
    }

    @Override
    @Nullable
    public Inventory getInventory() {
        check();
        return this.isEntityValid() && this.getUnsafeEntity() instanceof
                Carrier
                ? ((Carrier) this.getUnsafeEntity()).getInventory()
                : null;
    }

    @Override
    public Optional<IEffect> getEffect(String name) {
        check();
        checkNotNull(name);
        return Optional.of(this.effects.get(name.toLowerCase()));

    }

    @Override
    public Set<IEffect> getEffects() {
        check();
        return ImmutableSet.copyOf(this.effects.values());
    }

    @Override
    public void addEffect(IEffect effect) {
        check();
        checkNotNull(effect, "Cannot add a null IEffect!");

        if (hasEffect(effect.getName())) {
            removeEffect(getEffect(effect.getName()).get());
        }

        /*
        EffectAddEvent event = new EffectAddEvent(this, effect);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        */

        this.effects.put(effect.getName().toLowerCase(), effect);
        effect.apply(this);

        if (effect instanceof Timed) {
            this.plugin.getEffectManager().manageEffect(this, (Timed) effect);
        }
    }

    @Override
    public void addPotionEffect(PotionEffect potion) {
        check();
        checkArgument(potion != null, "Cannot add a null PotionEffect!");
        if (this.isEntityValid()) {
            this.potionEffectQueue.add(
                    new RPGPotionEffect(RpgCommon.getGame()
                            .getRegistry()
                            .getPotionEffectBuilder()
                            .potionType(potion.getType())
                            .duration(potion.getDuration())
                            .ambience(potion.isAmbient())
                            .amplifier(
                                    potion.getAmplifier())
                            .particles(
                                    potion.getShowParticles())
                            .build(), true));
        }
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        check();
        checkArgument(type != null, "Cannot remove a null potion effect type!");
        if (this.isEntityValid()) {
            this.potionEffectQueue.add(
                    new RPGPotionEffect(
                            RpgCommon.getGame()
                                    .getRegistry()
                                    .getPotionEffectBuilder()
                                    .potionType(type)
                                    .build(), false));
        }
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        check();
        checkArgument(type != null,
                "Cannot check on a null potion effect type!");
        return this.getUnsafeEntity() != null && this.getUnsafeEntity()
                .hasPotionEffect
                        (type);
    }

    @Override
    public boolean hasEffect(String name) {
        check();
        checkArgument(name != null,
                "Cannot check if an effect with a null name!");
        checkArgument(!name.isEmpty(), "Cannot check an empty effect name!");
        return this.effects.containsKey(name.toLowerCase());
    }

    @Override
    public boolean hasEffectType(EffectType type) {
        check();
        checkArgument(type != null, "Cannot check on a null effect type!");
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
        checkArgument(effect != null, "Cannot remove a null effect!");

        // TODO
        /*
        EffectRemoveEvent event = new EffectRemoveEvent(this, effect);
        this.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        */

        effect.remove(this);
        this.effects.remove(effect.getName().toLowerCase());

        if (effect instanceof Timed) {
            this.plugin.getEffectManager().queueRemoval(this, (Timed) effect);
        }
    }

    @Override
    public void manualRemoveEffect(IEffect effect) {
        check();
        checkArgument(effect != null, "Cannot remove a null effect!");
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
            throw new IllegalStateException(
                    "The linked entity is no longer available!");
        }
    }
}
