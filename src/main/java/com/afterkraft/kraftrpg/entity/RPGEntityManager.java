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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.service.scheduler.Task;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.entity.Summon;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Passive;
import com.afterkraft.kraftrpg.api.skills.common.Permissible;
import com.afterkraft.kraftrpg.api.storage.PlayerData;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;

/**
 * Default implementation of EntityManager.
 */
public class RPGEntityManager implements EntityManager {

    private final KraftRPGPlugin plugin;
    private final Map<UUID, Champion> champions;
    private final Map<UUID, Monster> monsters;
    private final Map<UUID, IEntity> entities;
    private final Map<UUID, Summon> summons;
    private UUID entityTaskID;
    private UUID potionTaskID;

    private StorageFrontend storage;

    public RPGEntityManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.champions = Maps.newHashMap();
        this.monsters = Maps.newConcurrentMap();
        this.entities = Maps.newConcurrentMap();
        this.summons = Maps.newConcurrentMap();
        this.storage = this.plugin.getStorage();
    }

    public final Set<Sentient> getAllSentientBeings() {
        Set<Sentient> sentients = new HashSet<>();
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
        for (Summon summon : this.summons.values()) {
            if (summon instanceof Sentient) {
                sentients.add((Sentient) summon);
            }
        }
        return sentients;
    }

    @Override
    public final Optional<? extends IEntity> getEntity(Entity entity) {
        checkArgument(entity != null,
                "Cannot get an IEntity of a null Entity!");
        if (entity instanceof Player) {
            return this.getChampion((Player) entity);
        } else if (entity instanceof Living) {
            if (this.summons.containsKey(entity.getUniqueId())) {
                return Optional.fromNullable(
                        this.summons.get(entity.getUniqueId()));
            }
            return getMonster((Living) entity);
        } else {
            return Optional.fromNullable(this.getIEntity(entity));
        }
    }

    @Override
    public Optional<Monster> getMonster(Living entity) {
        checkNotNull(entity, "Cannot get a Monster with a null Living!");
        final UUID id = entity.getUniqueId();
        if (this.monsters.containsKey(id)) {
            return Optional.fromNullable(this.monsters.get(id));
        } else {
            final Monster monster = new RPGMonster(this.plugin, entity);
            this.monsters.put(id, monster);
            return Optional.of(monster);
        }
    }

    @Override
    public Optional<Champion> getChampion(Player player) {
        checkNotNull(player, "Cannot get a Champion of a null Player!");
        Champion champion = this.champions.get(player.getUniqueId());
        if (champion != null) { // We already have this champion
            if (!champion.isEntityValid() || (champion.getPlayer().get()
                    .getUniqueId() != player
                    .getUniqueId())) {
                this.plugin.getLogger().warn("Duplicate Champion object "
                        + "found! "
                        + "Please make sure Champions are properly removed!");
                champion.clearEffects();
                champion.setPlayer(player);
            }
            this.champions.put(player.getUniqueId(), champion);
        } else { // We haven't loaded the champion yet from database.
            champion = this.storage.loadChampion(player, true).get();
            this.champions.put(player.getUniqueId(), champion);
            performSkillChecks(champion);
            this.storage.saveChampion(champion);
        }
        return Optional.of(champion);
    }

    @Override
    public Optional<? extends Champion> getChampion(UUID uuid) {
        return null;
    }

    @Override
    public boolean isEntityManaged(Entity entity) {
        checkNotNull(entity, "Cannot check for a null entity!");
        return this.monsters.containsKey(entity.getUniqueId()) || this.entities
                .containsKey(entity.getUniqueId()) || this.champions
                .containsKey(entity.getUniqueId());
    }

    @Override
    public Champion createChampionWithData(Player player, PlayerData data) {
        checkNotNull(player,
                "Cannot create a Champion with a null player!");
        checkNotNull(data,
                "Cannot create a Champion with a null player data!");
        Champion champion = new RPGChampion(this.plugin, player, data);
        champion.recalculateMaxHealth();
        champion.setMana(data.currentMana);
        champion.setStamina(data.currentStamina);
        return champion;
    }

    @Override
    public boolean addEntity(IEntity entity) {
        checkNotNull(entity, "Cannot add a null IEntity!");
        checkArgument(entity.isEntityValid(), "Cannot add an invalid IEntity!");
        if (entity instanceof Champion) {
            if (this.champions.containsKey(entity.getUniqueID())) {
                if (this.champions.get(entity.getUniqueID()).equals(entity)) {
                    throw new IllegalArgumentException(
                            "Third Party Plugins can't add duplicate "
                                    + "Champions!");
                } else {
                    throw new IllegalArgumentException(
                            "The provided players differ in their ID! "
                                    + "Can't add custom Champions with "
                                    + "duplicate ID's!");
                }
            } else {
                this.champions.put(entity.getUniqueID(), (Champion) entity);
                return true;
            }
        } else if (entity instanceof Monster && !this.monsters
                .containsKey(entity.getUniqueID())) {
            if (this.entities.containsKey(entity.getUniqueID())) {
                throw new IllegalArgumentException(
                        "The provided custom entity is already registered "
                                + "with KraftRPG!");
            }
            this.monsters.put(entity.getUniqueID(), (Monster) entity);
            return true;
        } else if (entity instanceof Summon) {
            if (this.summons.containsKey(entity.getUniqueID())) {
                throw new IllegalArgumentException(
                        "The provided custom entity is alraedy registered "
                                + "with KraftRPG!");
            }
            this.summons.put(entity.getUniqueID(), (Summon) entity);
            return true;
        } else if (!this.entities.containsKey(entity.getUniqueID())) {
            this.entities.put(entity.getUniqueID(), entity);
            return true;
        }
        return false;
    }

    @Override
    public Summon createSummon(SkillCaster owner, EntityType type) {
        checkNotNull(type, "Cannot create a null summon!");
        checkNotNull(owner,
                "Cannot create a summon for a null owner!");
        checkArgument(owner.isValid(),
                "Cannot create a summon for an invalid owner!");
        checkArgument(Living.class.isAssignableFrom(type.getEntityClass()),
                "Cannot create a summon that isn't living!");
        Optional<Entity> optional = owner.getWorld().createEntity(type, owner
                .getLocation().getPosition());
        Living entity =
                (Living) optional.get();
        owner.getWorld().spawnEntity(optional.get());
        Summon summon = new RPGSummon(this.plugin, owner, entity,
                entity.getCustomName());
        this.summons.put(summon.getUniqueID(), summon);
        return summon;
    }

    @Override
    public Set<Summon> getSummons(SkillCaster owner) {
        return Sets.newHashSet();
    }

    @Override
    public void removeChampion(Champion c) {

    }

    private IEntity getIEntity(Entity entity) {
        final UUID id = entity.getUniqueId();
        if (this.entities.containsKey(id)) {
            return this.entities.get(id);
        } else {
            final IEntity iEntity =
                    new RPGEntity(this.plugin, entity, entity.toString());
            this.entities.put(id, iEntity);
            return iEntity;
        }
    }

    private void performSkillChecks(Champion champion) {
        for (final ISkill skill : this.plugin.getSkillManager().getSkills()) {
            if (skill instanceof Permissible) {
                ((Permissible) skill).tryLearning(champion);
            } else if (skill instanceof Passive) {
                ((Passive) skill).apply(champion);
            }
        }
    }

    @Override
    public void initialize() {

        Optional<Task> optional = RpgCommon.getGame()
                .getSyncScheduler()
                .runRepeatingTask(KraftRPGPlugin.getInstance().getPluginContainer(),
                        new RPGEntityTask(), 1000);
        if (optional.isPresent()) {
            this.entityTaskID = optional.get().getUniqueId();
        } else {
            this.plugin.getLogger().error("[KraftRPG|Entities] Unable to "
                    + "schedule entities task. Expect entity expiration to "
                    + "break....");
        }
        Optional<Task> potionTask =  RpgCommon.getGame()
                .getSyncScheduler()
                .runRepeatingTask(KraftRPGPlugin.getInstance().getPluginContainer(),
                        new RPGInsentientPotionEffectTask(), 100);
        if (potionTask.isPresent()) {
            this.potionTaskID = potionTask.get().getUniqueId();
        } else {
            this.plugin.getLogger().error("[KraftRPG|Entities] Unable to "
                    + "schedule potion effects task. Expect entity potions "
                    + "to break....");
        }

    }

    @Override
    public void shutdown() {
        for (Summon summon : this.summons.values()) {
            summon.clearEffects();
            summon.remove();
        }
        for (Player player : RpgCommon.getServer().getOnlinePlayers()) {
            Optional<Champion> optional = getChampion(player);
            if (optional.isPresent()) {
                Champion champion = optional.get();
                champion.cancelStalledSkill(false);
                champion.clearEffects();
                champion.leaveParty();
                this.plugin.getCombatTracker()
                        .leaveCombat(champion, LeaveCombatReason.LOGOUT);
                this.storage.saveChampion(champion);
            }
        }
        RpgCommon.getGame().getSyncScheduler().getTaskById(this.entityTaskID)
                .get().cancel();
        RpgCommon.getGame().getSyncScheduler().getTaskById(this.potionTaskID)
                .get().cancel();
        clearPotionEffects();
    }

    /**
     * This is to specifically clear the potion effect queue for each managed
     * Insentient Entity.
     */
    private void clearPotionEffects() {
        Map<UUID, Champion> rpgPlayerMap = this.champions;
        Map<UUID, Monster> monsterMap = this.monsters;
        Map<UUID, Summon> summonMap = this.summons;
        Map<UUID, IEntity> entityMap = this.entities;

        Iterator<Map.Entry<UUID, Champion>> playerIterator =
                rpgPlayerMap.entrySet().iterator();
        Iterator<Map.Entry<UUID, Monster>> monsterIterator =
                monsterMap.entrySet().iterator();
        Iterator<Map.Entry<UUID, Summon>> summonIterator =
                summonMap.entrySet().iterator();
        Iterator<Map.Entry<UUID, IEntity>> entityIterator =
                entityMap.entrySet().iterator();

        while (playerIterator.hasNext()) {
            Champion champion = playerIterator.next().getValue();
            if (champion instanceof RPGChampion) {
                RPGChampion tempPlayer =
                        (RPGChampion) playerIterator.next().getValue();
                tempPlayer.potionEffectQueue.clear();
            }
        }
        while (monsterIterator.hasNext()) {
            Monster monster = monsterIterator.next().getValue();
            if (monster instanceof RPGInsentient) {
                RPGInsentient tempEntity = (RPGInsentient) monster;
                tempEntity.potionEffectQueue.clear();
            }
        }
        while (summonIterator.hasNext()) {
            Summon summon = summonIterator.next().getValue();
            if (summon instanceof RPGInsentient) {
                RPGInsentient tempEntity = (RPGInsentient) summon;
                tempEntity.potionEffectQueue.clear();
            }
        }
        while (entityIterator.hasNext()) {
            IEntity entity = entityIterator.next().getValue();
            if (entity instanceof RPGInsentient) {
                RPGInsentient tempEntity = (RPGInsentient) entity;
                tempEntity.potionEffectQueue.clear();
            }
        }
    }

    /**
     * A Timer task to remove the potentially GC'ed LivingEntities either due to
     * death, chunk unload, or reload.
     */
    private class RPGEntityTask implements Runnable {

        @Override
        public void run() {
            Map<UUID, Champion> rpgPlayerMap = RPGEntityManager.this.champions;
            Map<UUID, Monster> monsterMap = RPGEntityManager.this.monsters;
            Map<UUID, IEntity> entityMap = RPGEntityManager.this.entities;
            Map<UUID, Summon> summonMap = RPGEntityManager.this.summons;

            Iterator<Map.Entry<UUID, Champion>> playerIterator =
                    rpgPlayerMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, Monster>> monsterIterator =
                    monsterMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, IEntity>> entityIterator =
                    entityMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, Summon>> summonIterator =
                    summonMap.entrySet().iterator();


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

            while (entityIterator.hasNext()) {
                IEntity tempEntity = entityIterator.next().getValue();
                if (!tempEntity.isEntityValid()) {
                    entityIterator.remove();
                }
            }

            while (summonIterator.hasNext()) {
                IEntity tempEntity = summonIterator.next().getValue();
                if (!tempEntity.isEntityValid()) {
                    summonIterator.remove();
                }
            }

        }
    }

    private class RPGInsentientPotionEffectTask implements Runnable {

        @Override
        public void run() {
            Map<UUID, Champion> rpgPlayerMap = RPGEntityManager.this.champions;
            Map<UUID, Monster> monsterMap = RPGEntityManager.this.monsters;
            Map<UUID, IEntity> entityMap = RPGEntityManager.this.entities;
            Map<UUID, Summon> summonMap = RPGEntityManager.this.summons;

            Iterator<Map.Entry<UUID, Champion>> playerIterator =
                    rpgPlayerMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, Monster>> monsterIterator =
                    monsterMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, IEntity>> entityIterator =
                    entityMap.entrySet().iterator();
            Iterator<Map.Entry<UUID, Summon>> summonIterator =
                    summonMap.entrySet().iterator();

            while (playerIterator.hasNext()) {
                RPGChampion tempPlayer =
                        (RPGChampion) playerIterator.next().getValue();
                if (tempPlayer.potionEffectQueue.poll() != null && tempPlayer
                        .isEntityValid()) {
                    Player player = tempPlayer.getEntity().get();
                    RPGPotionEffect effect =
                            tempPlayer.potionEffectQueue.remove();
                    if (effect.adding) {
                        player.addPotionEffect(effect.potion, false);
                    } else {
                        player.removePotionEffect(effect.potion.getType());
                    }
                }
            }
            while (monsterIterator.hasNext()) {
                RPGInsentient tempEntity =
                        (RPGInsentient) monsterIterator.next().getValue();
                if (tempEntity.potionEffectQueue.poll() != null && tempEntity
                        .isEntityValid()) {
                    Living entity = tempEntity.getEntity().get();
                    RPGPotionEffect effect =
                            tempEntity.potionEffectQueue.remove();
                    if (effect.adding) {
                        entity.addPotionEffect(effect.potion, false);
                    } else {
                        entity.removePotionEffect(effect.potion.getType());
                    }
                }
            }
            while (entityIterator.hasNext()) {
                IEntity tempEntity = entityIterator.next().getValue();
                if (tempEntity instanceof RPGInsentient) {
                    RPGInsentient rpgEntity = (RPGInsentient) tempEntity;
                    if (rpgEntity.potionEffectQueue.poll() != null && rpgEntity
                            .isEntityValid()) {
                        Living entity = rpgEntity.getEntity().get();
                        RPGPotionEffect effect =
                                rpgEntity.potionEffectQueue.remove();
                        if (effect.adding) {
                            entity.addPotionEffect(effect.potion, false);
                        } else {
                            entity.removePotionEffect(effect.potion.getType());
                        }
                    }
                }
            }
            while (summonIterator.hasNext()) {
                Summon summon = summonIterator.next().getValue();
                if (summon instanceof RPGInsentient) {
                    RPGInsentient rpgInsentient = (RPGInsentient) summon;
                    if (rpgInsentient.potionEffectQueue.poll() != null
                            && rpgInsentient
                            .isEntityValid()) {
                        Living entity = rpgInsentient.getEntity().get();
                        RPGPotionEffect effect =
                                rpgInsentient.potionEffectQueue.remove();
                        if (effect.adding) {
                            entity.addPotionEffect(effect.potion, false);
                        } else {
                            entity.removePotionEffect(effect.potion.getType());
                        }
                    }
                }
            }
        }
    }
}
