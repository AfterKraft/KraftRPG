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
package com.afterkraft.kraftrpg.listeners.tweakkit;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.afterkraft.configuration.CustomDataCompound;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.entity.RPGMonster;
import com.afterkraft.kraftrpg.listeners.common.AbstractEntityListener;

public class TweakkitEntityListener extends AbstractEntityListener {

    private static final Vector centerOfMap = new Vector(0, 64, 0);

    public TweakkitEntityListener(KraftRPGPlugin plugin) {
        super(plugin);
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {

    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityTarget(EntityTargetEvent event) {

    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageEvent(EntityDamageEvent event) {

    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (!event.getEntity().getCustomData().isEmpty()) {
            CustomDataCompound compound = event.getEntity().getCustomData();
            if (compound.hasKey("kraftrpg")) {
                applyHealthChanges(event.getEntity());
                CustomDataCompound kraftdata = compound.getCompound("kraftrpg");
                Monster monster = new RPGMonster(plugin, event.getEntity(), CreatureSpawnEvent.SpawnReason.valueOf(kraftdata.getString("spawnReason")));
                monster.setExperience(new FixedPoint(kraftdata.getLong("experience"), true));
                monster.setModifiedDamage(kraftdata.getDouble("damage"));

            } else {
                applyHealthChanges(event.getEntity());
                compound.set("kraftrpg", new CustomDataCompound());
                CustomDataCompound kraftdata = compound.getCompound("kraftrpg");
                kraftdata.setString("spawnReason", event.getSpawnReason().name());
                kraftdata.setInt("spawnX", event.getLocation().getBlockX());
                kraftdata.setInt("spawnY", event.getLocation().getBlockY());
                kraftdata.setInt("spawnZ", event.getLocation().getBlockZ());
            }
        }
        event.getEntity().setMetadata("kraftrpg.spawnReason", new FixedMetadataValue(plugin, event.getSpawnReason().toString()));
    }

    private double applyHealthChanges(LivingEntity lEntity) {
        if (lEntity.getHealth() == lEntity.getMaxHealth()) {
            double defaultMaxHealth = Math.floor(plugin.getDamageManager().getDefaultEntityHealth(lEntity));
            if ((Math.floor(lEntity.getMaxHealth()) == defaultMaxHealth) || defaultMaxHealth == 0) {
                double max = plugin.getDamageManager().getModifiedEntityHealth(lEntity);
                if (plugin.getProperties().isMobHealthDistanceModified()) {
                    double percent = 1 + plugin.getProperties().getMobHealthDistanceModified() / 100.00d;
                    double modifier = Math.pow(percent, getEffectiveDistance(lEntity.getLocation().toVector()) / 100.00D) + 0.00D;
                    max = Math.ceil(max * modifier);
                }
                lEntity.setMaxHealth(max);
                lEntity.setHealth(max);
                return max;
            }
        }
        return 0;
    }

    private static double getEffectiveDistance(Vector entityPosition) {
        return (entityPosition.distance(centerOfMap)) - (entityPosition.distance(centerOfMap) % 100.00D);
    }

}
