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
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.storage.PlayerData;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;

public class RPGEntityManager implements EntityManager {

    private final KraftRPGPlugin plugin;
    private final Map<UUID, Champion> champions;
    private final Map<UUID, Monster> monsters;
    private int entityTaskID;
    private int potionTaskID;

    private StorageFrontend storage;

    public RPGEntityManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.champions = new HashMap<UUID, Champion>();
        this.monsters = new ConcurrentHashMap<UUID, Monster>();
        this.storage = this.plugin.getStorage();
    }

    public final IEntity getEntity(LivingEntity entity) {
        if (entity instanceof Player) {
            return this.getChampion((Player) entity);
        } else {
            return this.getMonster(entity);
        }
    }

    @Override
    public IEntity getEntity(SkillCaster caster) {
        if (caster instanceof Champion) {
            return (Champion) caster;
        } else if (caster instanceof Monster) {
            return (Monster) caster;
        } else if (caster instanceof IEntity) {
            return (IEntity) caster;
        } else {
            return null;
        }
    }

    public Champion getChampion(Player player) {
        if (player == null) {
            return null;
        }
        Champion champion = this.champions.get(player.getUniqueId());
        if (champion != null) {
            if (!champion.isEntityValid() || (champion.getPlayer().getEntityId() != player.getEntityId())) {
                plugin.log(Level.WARNING, "Duplicate Champion object found! Please make sure Champions are properly removed!");
                champion.clearEffects();
                champion.setPlayer(player);
            }
            champions.put(player.getUniqueId(), champion);
        } else {
            champion = createChampion(player, new PlayerData());
            storage.saveChampion(champion);
        }
        return champion;
    }

    public Monster getMonster(LivingEntity entity) {
        final UUID id = entity.getUniqueId();
        if (monsters.containsKey(id)) {
            return monsters.get(id);
        } else {
            final Monster monster = new RPGMonster(plugin, entity);
            monsters.put(id, monster);

            Bukkit.getScheduler().runTaskLater(plugin, new RPGSingleEntityReaper(monster), 200);

            return monster;
        }
    }

    public boolean isMonsterSetup(LivingEntity entity) {
        return this.monsters.containsKey(entity.getUniqueId());
    }

    // api - StorageFrontends call this
    public Champion createChampion(Player player, PlayerData data) {
        // do NOT add to the champions map! that's done elsewhere
        Champion champ = new RPGChampion(plugin, player, data);
        return champ;
    }

    public boolean addMonster(Monster monster) {
        if (!monster.isEntityValid()) {
            return false;
        }
        final UUID id = monster.getEntity().getUniqueId();
        if (this.monsters.containsKey(id)) {
            return false;
        } else {
            this.monsters.put(id, monster);
            return true;
        }
    }

    public Monster getMonster(UUID uuid) {
        return this.monsters.get(uuid);
    }

    @Override
    public Champion getChampion(UUID uuid, boolean ignoreOffline) {
        return null;
    }

    /**
     * Attempts to add the given RPGChampion to this player mapping. This
     * should only be used to add an RPGChampion for custom RPGPlayers that
     * aren't covered by KraftRPG.
     * 
     * @param player - The RPGChampion to add to the player mapping
     * @return true if the RPGChampion addition was successful
     */
    public boolean addChampion(Champion player) {
        if (!player.isEntityValid() || this.champions.containsKey(player.getPlayer().getUniqueId())) {
            return false;
        }
        this.champions.put(player.getPlayer().getUniqueId(), player);
        return true;
    }

    public void removeMonster(Monster monster) {
        monsters.remove(((RPGEntity) monster).uuid);
    }

    @Override
    public void initialize() {
        entityTaskID = Bukkit.getScheduler().runTaskTimer(plugin, new RPGEntityTask(), 100, 1000).getTaskId();
        potionTaskID = Bukkit.getScheduler().runTaskTimer(plugin, new RPGInsentientPotionEffectTask(), 1, 100).getTaskId();

    }

    public void shutdown() {
        Bukkit.getScheduler().cancelTask(entityTaskID);
        Bukkit.getScheduler().cancelTask(potionTaskID);
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
            RPGEntityInsentient tempEntity = (RPGEntityInsentient) monsterIterator.next().getValue();
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
                RPGEntityInsentient tempEntity = (RPGEntityInsentient) monsterIterator.next().getValue();
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

        private Monster monster;

        public RPGSingleEntityReaper(Monster m) {
            this.monster = m;
        }

        @Override
        public void run() {
            if (!monster.isEntityValid()) {
                removeMonster(monster);
            }
        }

    }
}
