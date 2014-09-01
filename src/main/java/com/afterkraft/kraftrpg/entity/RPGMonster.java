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

import java.util.List;

import org.apache.commons.lang.Validate;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.listeners.EntityListener;


public class RPGMonster extends RPGInsentient implements Monster {

    private FixedPoint experience = new FixedPoint();
    private double baseDamage = 0;
    private double damage = 0;
    private SpawnReason spawnReason = null;
    private int maxMana;
    private DamageWrapper wrapper;

    private Location spawnPoint;

    public RPGMonster(RPGPlugin plugin, LivingEntity entity) {
        this(plugin, entity, entity.getCustomName());
    }

    protected RPGMonster(RPGPlugin plugin, LivingEntity entity, String name) {
        super(plugin, entity, name);
        this.spawnReason = CraftBukkitHandler.getInterface().getSpawnReason(entity, getMetaSpawnReason(entity));
        this.spawnPoint = CraftBukkitHandler.getInterface().getSpawnLocation(entity);
        this.baseDamage = plugin.getDamageManager().getEntityDamage(entity.getType());
        this.damage = plugin.getDamageManager().getModifiedEntityDamage(this, this.spawnPoint, this.baseDamage, this.spawnReason);
        this.damage = CraftBukkitHandler.getInterface().getEntityDamage(entity, this.damage);
        this.experience = plugin.getProperties().getMonsterExperience(entity, this.spawnPoint);
        this.experience = CraftBukkitHandler.getInterface().getMonsterExperience(entity, this.experience);
    }

    private static SpawnReason getMetaSpawnReason(LivingEntity entity) {
        List<MetadataValue> values = entity.getMetadata(EntityListener.SPAWNREASON_META_KEY);
        if (values.isEmpty()) return null;

        return (SpawnReason) values.get(0).value();
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
        Validate.isTrue(damage > 0, "Cannot set the attacking damage to zero or less than zero!");
        this.damage = damage > 0 ? damage : 1;
    }

    @Override
    public SpawnReason getSpawnReason() {
        return this.spawnReason;
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
        // Nope! stamina doesn't exist for the Monster
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
        if (!isEntityValid()) {
            return;
        }
        this.experience = experience;
        CraftBukkitHandler.getInterface().setMonsterExperience(getEntity(), experience);
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
    public ItemStack getItemInHand() {
        return this.isEntityValid() ? this.getEntity().getEquipment().getItemInHand() : null;
    }

    @Override
    public Inventory getInventory() {
        return this.isEntityValid() ? (this.getEntity() instanceof InventoryHolder) ? ((InventoryHolder) this.getEntity()).getInventory() : null : null;
    }
}
