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

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.listeners.EntityListener;


public class RPGMonster extends RPGInsentient implements Monster {

    private FixedPoint experience = new FixedPoint();
    private double baseDamage = 0;
    private double damage = 0;
    private SpawnReason spawnReason = null;

    private Location spawnPoint;

    public RPGMonster(RPGPlugin plugin, LivingEntity entity) {
        this(plugin, entity, entity.getCustomName());
    }

    protected RPGMonster(RPGPlugin plugin, LivingEntity entity, String name) {
        super(plugin, entity, name);
        this.spawnReason = CraftBukkitHandler.getInterface().getSpawnReason(entity, getMetaSpawnReason(entity));
        this.spawnPoint = CraftBukkitHandler.getInterface().getSpawnLocation(entity);
        this.baseDamage = plugin.getDamageManager().getEntityDamage(entity.getType());
        this.damage = plugin.getDamageManager().getModifiedEntityDamage(this, spawnPoint, baseDamage, spawnReason);
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
        return spawnPoint;
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
        this.damage = damage > 0 ? damage : 1;
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
    public SpawnReason getSpawnReason() {
        return this.spawnReason;
    }

    @Override
    public DamageWrapper getDamageWrapper() {
        return null;
    }

    @Override
    public void setDamageWrapper(DamageWrapper wrapper) {

    }

    @Override
    public int getNoDamageTicks() {
        return 0;
    }

    @Override
    public float getStamina() {
        return 20 * 4;
    }

    @Override
    public void modifyStamina(float staminaDiff) {
        // Nope! stamina doesn't exist for the Monster
    }

    @Override
    public ItemStack[] getArmor() {
        return this.isEntityValid() ? this.getEntity().getEquipment().getArmorContents() : null;
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
        return null;
    }
}
