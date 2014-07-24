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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.storage.PlayerData;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;

public class RPGEntityManager implements EntityManager {

    private final RPGPlugin plugin;
    private final Map<UUID, Champion> champions;
    private final Map<UUID, Monster> monsters;
    private final Map<UUID, IEntity> entities;
    private int entityTaskID;
    private int potionTaskID;

    private StorageFrontend storage;

    public RPGEntityManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.champions = new HashMap<UUID, Champion>();
        this.monsters = new ConcurrentHashMap<UUID, Monster>();
        this.entities = new ConcurrentHashMap<UUID, IEntity>();
        this.storage = this.plugin.getStorage();
    }

    public final Set<Sentient> getAllSentientBeings() {
        Set<Sentient> sentients = new HashSet<Sentient>();
        for (Champion champion : this.champions.values()) {
            sentients.add(champion);
        }
        for (IEntity entity : this.entities.values()) {
            if (entity instanceof Sentient) {
                sentients.add((Sentient) entity);
            }
        }
        for (Monster monster : this.monsters.values()) {
            if (monster instanceof Sentient) {
                sentients.add((Sentient) monster);
            }
        }
        return sentients;
    }

    @Override
    public final IEntity getEntity(Entity entity) {
        Validate.notNull(entity, "Cannot get an IEntity of a null Entity!");
        if (entity instanceof Player) {
            return this.getChampion((Player) entity);
        } else if (entity instanceof LivingEntity && this.monsters.containsKey(entity.getUniqueId())) {
            return getMonster((LivingEntity) entity);
        } else {
            return this.getIEntity(entity);
        }
    }

    @Override
    public Champion getChampion(Player player) {
        Validate.notNull(player, "Cannot get a Champion of a null Player!");

        Champion champion = this.champions.get(player.getUniqueId());
        if (champion != null) {
            if (!champion.isEntityValid() || (champion.getPlayer().getEntityId() != player.getEntityId())) {
                this.plugin.log(Level.WARNING, "Duplicate Champion object found! Please make sure Champions are properly removed!");
                champion.clearEffects();
                champion.setPlayer(player);
            }
            this.champions.put(player.getUniqueId(), champion);
        } else {
            champion = createChampion(player, new PlayerData());
            this.champions.put(player.getUniqueId(), champion);
            this.storage.saveChampion(champion);
        }
        return champion;
    }

    @Override
    public Monster getMonster(LivingEntity entity) {
        Validate.notNull(entity, "Cannot get a Monster with a null LivingEntity!");
        final UUID id = entity.getUniqueId();
        if (this.monsters.containsKey(id)) {
            return this.monsters.get(id);
        } else {
            final Monster monster = new RPGMonster(this.plugin, entity);
            this.monsters.put(id, monster);

            Bukkit.getScheduler().runTaskTimer(this.plugin, new RPGSingleEntityReaper(monster), 200, 200);

            return monster;
        }
    }

    @Override
    public boolean isEntityManaged(Entity entity) {
        Validate.notNull(entity, "Cannot check for a null entity!");
        return this.monsters.containsKey(entity.getUniqueId()) || this.entities.containsKey(entity.getUniqueId()) || this.champions.containsKey(entity.getUniqueId());
    }

    @Override
    public Champion createChampion(Player player, PlayerData data) {
        Validate.notNull(player, "Cannot create a Champion with a null player!");
        Validate.notNull(data, "Cannot create a Champion with a null player data!");
        return new RPGChampion(this.plugin, player, data);
    }

    @Override
    public boolean addEntity(IEntity entity) {
        Validate.notNull(entity, "Cannot add a null IEntity!");
        Validate.isTrue(entity.isEntityValid(), "Cannot add an invalid IEntity!");
        if (entity instanceof Champion) {
            if (this.champions.containsKey(entity.getUniqueID())) {
                if (this.champions.get(entity.getUniqueID()).equals(entity)) {
                    throw new IllegalArgumentException("Third Party Plugins can't add duplicate Champions!");
                } else {
                    throw new IllegalArgumentException("The provided players differ in their ID! Can't add custom Champions with duplicate ID's!");
                }
            } else {
                this.champions.put(entity.getUniqueID(), (Champion) entity);
                return true;
            }
        } else if (entity instanceof Monster && !this.monsters.containsKey(entity.getUniqueID())) {
            if (this.entities.containsKey(entity.getUniqueID())) {
                throw new IllegalArgumentException("The provided custom entity is already registered with KraftRPG!");
            }
            this.monsters.put(entity.getUniqueID(), (Monster) entity);
            Bukkit.getScheduler().runTaskTimer(this.plugin, new RPGSingleEntityReaper(entity), 200, 200);
            return true;
        } else if (!this.entities.containsKey(entity.getUniqueID())) {
            this.entities.put(entity.getUniqueID(), entity);
            Bukkit.getScheduler().runTaskTimer(this.plugin, new RPGSingleEntityReaper(entity), 200, 200);
            return true;
        }
        return false;
    }

    @Override
    public Monster getMonster(UUID uuid) {
        Validate.notNull(uuid, "Cannot get a Monster from a null UUID!");
        return this.monsters.get(uuid);
    }

    @Override
    public Champion getChampion(UUID uuid, boolean ignoreOffline) {
        Validate.notNull(uuid, "Cannot get a Champion from a null UUID!");

        return null;
    }

    private IEntity getIEntity(Entity entity) {
        final UUID id = entity.getUniqueId();
        if (this.entities.containsKey(id)) {
            return this.entities.get(id);
        } else {
            final IEntity iEntity = new RPGEntity(this.plugin, entity, entity.toString());
            this.entities.put(id, iEntity);
            Bukkit.getScheduler().runTaskTimer(this.plugin, new RPGSingleEntityReaper(iEntity), 200, 200);
            return iEntity;
        }
    }

    private void removeEntity(IEntity entity) {
        if (entity instanceof Monster) {
            removeMonster((Monster) entity);
        } else {
            this.entities.remove(entity.getUniqueID());
        }
    }

    public void removeMonster(Monster monster) {
        this.monsters.remove(monster.getUniqueID());
    }

    @Override
    public void initialize() {
        this.entityTaskID = Bukkit.getScheduler().runTaskTimer(this.plugin, new RPGEntityTask(), 100, 1000).getTaskId();
        this.potionTaskID = Bukkit.getScheduler().runTaskTimer(this.plugin, new RPGInsentientPotionEffectTask(), 1, 100).getTaskId();

    }

    @Override
    public void shutdown() {
        Bukkit.getScheduler().cancelTask(this.entityTaskID);
        Bukkit.getScheduler().cancelTask(this.potionTaskID);
        clearPotionEffects();
    }

    /**
     * This is to specifically clear the potion effect queue for each managed
     * Insentient Entity.
     */
    private void clearPotionEffects() {
        Map<UUID, Champion> rpgPlayerMap = RPGEntityManager.this.champions;
        Map<UUID, Monster> monsterMap = RPGEntityManager.this.monsters;

        Iterator<Map.Entry<UUID, Champion>> playerIterator = rpgPlayerMap.entrySet().iterator();
        Iterator<Map.Entry<UUID, Monster>> monsterIterator = monsterMap.entrySet().iterator();
        while (playerIterator.hasNext()) {
            RPGChampion tempPlayer = (RPGChampion) playerIterator.next().getValue();
            tempPlayer.potionEffectQueue.clear();
        }
        while (monsterIterator.hasNext()) {
            RPGInsentient tempEntity = (RPGInsentient) monsterIterator.next().getValue();
            tempEntity.potionEffectQueue.clear();
        }
    }

    /**
     * A Timer task to remove the potentially GC'ed LivingEntities either due
     * to death, chunk unload, or reload.
     */
    private class RPGEntityTask implements Runnable {

        @Override
        public void run() {
            Map<UUID, Champion> rpgPlayerMap = RPGEntityManager.this.champions;
            Map<UUID, Monster> monsterMap = RPGEntityManager.this.monsters;

            Iterator<Map.Entry<UUID, Champion>> playerIterator = rpgPlayerMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, Monster>> monsterIterator = monsterMap.entrySet().iterator();

            while (playerIterator.hasNext()) {
                Champion tempPlayer = playerIterator.next().getValue();
                if (!tempPlayer.isEntityValid()) {
                    playerIterator.remove();
                }
            }

            while (monsterIterator.hasNext()) {
                Monster tempMonster = monsterIterator.next().getValue();
                if (!tempMonster.isEntityValid()) {
                    monsterIterator.remove();
                }
            }

        }
    }

    private class RPGInsentientPotionEffectTask implements Runnable {
        @Override
        public void run() {
            Map<UUID, Champion> rpgPlayerMap = RPGEntityManager.this.champions;
            Map<UUID, Monster> monsterMap = RPGEntityManager.this.monsters;

            Iterator<Map.Entry<UUID, Champion>> playerIterator = rpgPlayerMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, Monster>> monsterIterator = monsterMap.entrySet().iterator();
            while (playerIterator.hasNext()) {
                RPGChampion tempPlayer = (RPGChampion) playerIterator.next().getValue();
                if (tempPlayer.potionEffectQueue.poll() != null && tempPlayer.isEntityValid()) {
                    Player player = tempPlayer.getEntity();
                    RPGPotionEffect effect = tempPlayer.potionEffectQueue.remove();
                    if (effect.adding) {
                        player.addPotionEffect(effect.potion);
                    } else {
                        player.removePotionEffect(effect.potion.getType());
                    }
                }
            }
            while (monsterIterator.hasNext()) {
                RPGInsentient tempEntity = (RPGInsentient) monsterIterator.next().getValue();
                if (tempEntity.potionEffectQueue.poll() != null && tempEntity.isEntityValid()) {
                    LivingEntity entity = tempEntity.getEntity();
                    RPGPotionEffect effect = tempEntity.potionEffectQueue.remove();
                    if (effect.adding) {
                        entity.addPotionEffect(effect.potion);
                    } else {
                        entity.removePotionEffect(effect.potion.getType());
                    }
                }
            }
        }
    }

    private class RPGSingleEntityReaper implements Runnable {

        private IEntity monster;

        public RPGSingleEntityReaper(IEntity m) {
            this.monster = m;
        }

        @Override
        public void run() {
            if (!this.monster.isEntityValid()) {
                removeEntity(this.monster);
            }
        }

    }
}
