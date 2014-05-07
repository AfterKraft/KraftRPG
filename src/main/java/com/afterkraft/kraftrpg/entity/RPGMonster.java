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

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.EnterCombatReason;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.util.FixedPoint;


public class RPGMonster extends RPGEntityInsentient implements Monster {

    private FixedPoint experience = new FixedPoint(0);
    private double baseDamage = 0;
    private double damage = 0;
    private SpawnReason spawnReason = null;

    private Location spawnPoint;

    public RPGMonster(RPGPlugin plugin, LivingEntity entity) {
        this(plugin, entity, entity.getCustomName(), SpawnReason.valueOf(entity.getMetadata("kraftrpg.spawnReason").get(1).asString()));
    }

    protected RPGMonster(RPGPlugin plugin, LivingEntity entity, String name, SpawnReason reason) {
        super(plugin, entity, name);
        this.spawnReason = CraftBukkitHandler.getInterface().getSpawnReason(entity);
        this.spawnPoint = CraftBukkitHandler.getInterface().getSpawnLocation(entity);
        this.baseDamage = plugin.getDamageManager().getEntityDamage(entity.getType());
        this.damage = plugin.getDamageManager().getModifiedEntityDamage(this, spawnPoint, baseDamage, spawnReason);
        this.damage = CraftBukkitHandler.getInterface().getEntityDamage(entity, this.damage);
        this.experience = plugin.getProperties().getMonsterExperience(entity, this.spawnPoint);
        this.experience = CraftBukkitHandler.getInterface().getMonsterExperience(entity, this.experience);
    }

    public RPGMonster(RPGPlugin plugin, LivingEntity entity, SpawnReason reason) {
        this(plugin, entity, entity.getCustomName(), reason);
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
    public FixedPoint getExperience() {
        return this.experience;
    }

    @Override
    public void setExperience(FixedPoint experience) {
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
    public void enterCombatWith(LivingEntity target, EnterCombatReason reason) {

    }

    @Override
    public void leaveCombatWith(LivingEntity target, LeaveCombatReason reason) {

    }
}
