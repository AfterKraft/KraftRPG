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
package com.afterkraft.kraftrpg.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;

public class EntityListener extends AbstractListener {
    public static final String SPAWNREASON_META_KEY = "KraftRPG: Spawn Reason";

    protected EntityListener(RPGPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        // TODO Handle removal of the entity being handled by KraftRPG
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityTarget(EntityTargetEvent event) {
        // TODO handle invisibility effects and such
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageEarly(EntityDamageEvent event) {
        Entity e = event.getEntity();
        if (e instanceof LivingEntity) {
            plugin.getEntityManager().getEntity(e);
        }

        if (event instanceof EntityDamageByEntityEvent) {
            Entity d = ((EntityDamageByEntityEvent) event).getDamager();
            if (d instanceof LivingEntity) {
                plugin.getEntityManager().getEntity(d);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        event.getEntity().setMetadata(SPAWNREASON_META_KEY, new FixedMetadataValue(plugin, event.getSpawnReason()));
        if (!plugin.getEntityManager().isEntityManaged(event.getEntity())) {
            // We just need to make the EntityManager aware of the newly spawned
            // entity in the event another plugin hasn't done so already
            plugin.getEntityManager().getEntity(event.getEntity());
        }
    }
}
