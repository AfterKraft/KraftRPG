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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import com.google.common.base.Optional;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.resource.Resource;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.util.FixedPoint;

/**
 * Standard implementation of Monster. Note: This does not wrap non-living
 * entities.
 */
public class RPGMonster extends RPGInsentient implements Monster {

    private FixedPoint experience = new FixedPoint();
    private double baseDamage = 0;
    private double damage = 0;
    private int maxMana;
    private DamageWrapper wrapper;

    private Location spawnPoint;

    public RPGMonster(RPGPlugin plugin, Living entity) {
        this(plugin, entity, entity.getCustomName());
    }

    protected RPGMonster(RPGPlugin plugin, Living entity, String name) {
        super(plugin, entity, name);

    }

    @Override
    public Location getSpawnLocation() {
        return this.spawnPoint;
    }

    @Override
    public double getBaseDamage() {
        return this.baseDamage;
    }

    @Override
    public double getModifiedDamage() {
        return this.damage;
    }

    @Override
    public void setModifiedDamage(double damage) {
        checkArgument(damage > 0,
                "Cannot set the attacking damage to zero or less than zero!");
        this.damage = damage > 0 ? damage : 1;
    }

    @Override
    public Cause getSpawnReason() {
        return null;
    }

    @Override
    public int getMaxMana() {
        return this.maxMana;
    }

    @Override
    public void setMaxMana(int mana) {
        checkArgument(mana > 0, "Cannot set mana to be negative or zero!");
        this.maxMana = mana;
    }

    @Override
    public double getMaxHealth() {
        return this.isEntityValid() ? this.getUnsafeEntity().getMaxHealth()
                : 0D;
    }

    @Override
    public Optional<DamageWrapper> getDamageWrapper() {
        return Optional.of(this.wrapper);
    }

    @Override
    public void setDamageWrapper(DamageWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public int getNoDamageTicks() {
        return this.isEntityValid() ? this.getUnsafeEntity()
                .getInvulnerabilityTicks() : 0;
    }

    @Override
    public int getStamina() {
        return 20 * 4;
    }

    @Override
    public void setStamina(int stamina) {

    }

    @Override
    public int getMaxStamina() {
        return 0;
    }

    @Override
    public void modifyStamina(int staminaDiff) {
        // Nope! stamina doesn't exist for the Monster
    }

    @Override
    public ItemStack[] getArmor() {
        // TODO
        return new ItemStack[]{};
    }

    @Override
    public void setArmor(ItemStack item, int armorSlot) {

        // TODO
    }

    @Override
    public boolean canEquipItem(ItemStack itemStack) {
        return true;
    }

    @Override
    public FixedPoint getRewardExperience() {
        return this.experience;
    }

    @Override
    public void setRewardExperience(FixedPoint experience) {
        if (!isEntityValid()) {
            return;
        }
        this.experience = experience;
        // TODO
    }

    @Override
    public void sendMessage(String message, Object... args) {
        // do nothing
    }

    @Override
    public boolean isIgnoringSkill(ISkill skill) {
        return true;
    }

    @Override
    public void updateInventory() {
        // Nope! no updating inventories
    }

    @Override
    public Optional<ItemStack> getItemInHand() {
        return Optional.absent();
    }

    @Override
    public Inventory getInventory() {
        // TODO
        return null;
    }

    @Override
    public <T extends Resource> Optional<T> getResource(Class<T> clazz) {
        return null;
    }
}
