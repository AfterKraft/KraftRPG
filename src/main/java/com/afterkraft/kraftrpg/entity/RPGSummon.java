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
    public float getStamina() {
        return 20 * 4;
    }

    @Override
    public void modifyStamina(float staminaDiff) {
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

}
