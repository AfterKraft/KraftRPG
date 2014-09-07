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

import org.apache.commons.lang.Validate;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.entity.Summon;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.util.FixedPoint;

public class RPGSummon extends RPGInsentient implements Summon {
    protected final SkillCaster owner;
    private int maxMana;
    private FixedPoint experience;
    private DamageWrapper wrapper;

    public RPGSummon(RPGPlugin plugin, SkillCaster owner, LivingEntity lEntity, String name) {
        super(plugin, lEntity, name);
        this.owner = owner;
    }

    @Override
    public int getMaxMana() {
        return this.maxMana;
    }

    @Override
    public void setMaxMana(int mana) {
        Validate.isTrue(mana > 0, "Cannot set mana to be negative or zero!");
        this.maxMana = mana;
    }

    @Override
    public double getMaxHealth() {
        return this.isEntityValid() ? this.getEntity().getMaxHealth() : 0D;
    }

    @Override
    public DamageWrapper getDamageWrapper() {
        return this.wrapper;
    }

    @Override
    public void setDamageWrapper(DamageWrapper wrapper) {
        this.wrapper = wrapper;

    }

    @Override
    public int getNoDamageTicks() {
        return this.isEntityValid() ? this.getEntity().getNoDamageTicks() : 0;
    }

    @Override
    public int getStamina() {
        return 20 * 4;
    }

    @Override
    public int getMaxStamina() {
        return 0;
    }

    @Override
    public void setStamina(int stamina) {

    }

    @Override
    public void modifyStamina(int staminaDiff) {
        // #Nope! We don't need to modify stamina for a summon
    }

    @Override
    public ItemStack[] getArmor() {
        if (!this.isEntityValid()) {
            return new ItemStack[4];
        } else {
            ItemStack[] armor = new ItemStack[4];
            for (int i = 0; i < getEntity().getEquipment().getArmorContents().length; i++) {
                armor[i] = new ItemStack(getEntity().getEquipment().getArmorContents()[i]);
            }
            return armor;
        }
    }

    @Override
    public void setArmor(ItemStack item, int armorSlot) throws IllegalArgumentException {
        Validate.isTrue(armorSlot < getEntity().getEquipment().getArmorContents().length, "Cannot set the armor slot greater than the current armor!");
        getEntity().getEquipment().getArmorContents()[armorSlot] = new ItemStack(item);
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
        this.experience = experience;
    }

    @Override
    public void sendMessage(String message, Object... args) {
        // do nothing
    }

    @Override
    public boolean isIgnoringSkill(ISkill skill) {
        return true; // We don't need to trigger any skill messages this way
    }

    @Override
    public long getTimeLeftAlive() {
        return 0;
    }

    @Override
    public SkillCaster getSummoner() {
        return this.owner;
    }

    @Override
    public void remove() {
        this.getEntity().remove();
    }

}
