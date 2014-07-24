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

import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.EnterCombatReason;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.PartyMember;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.entity.effects.IEffect;
import com.afterkraft.kraftrpg.api.events.entity.InsentientKillEvent;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.api.listeners.AttackDamageWrapper;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.roles.ExperienceType;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.api.util.Properties;

public class EntityListener extends AbstractListener {
    public static final String SPAWNREASON_META_KEY = "KraftRPG: Spawn Reason";
    private final Properties properties;

    protected EntityListener(RPGPlugin plugin) {
        super(plugin);
        this.properties = plugin.getProperties();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (this.plugin.getEntityManager().isEntityManaged(event.getEntity())) {
            IEntity dyingEntity = this.plugin.getEntityManager().getEntity(event.getEntity());

            if (!(dyingEntity instanceof Insentient)) {
                return;
            }
            Insentient insentientDyingEntity = (Insentient) dyingEntity;
            // If the insentient isn't in combat with any other insentient, don't bother
            if (!this.plugin.getCombatTracker().isInCombat(insentientDyingEntity)) {
                return;
            }
            // If the damageWrapper is null, we have a problem as well.
            DamageWrapper damageWrapper = insentientDyingEntity.getDamageWrapper();
            if (damageWrapper == null) {
                return;
            }
            // Try and set the attacker from the damageWrapper
            Insentient attacker = null;
            if (damageWrapper instanceof AttackDamageWrapper) {
                attacker = ((AttackDamageWrapper) damageWrapper).getAttackingIEntity();
            }

            // We must have the sentient lose experience for dying
            if (insentientDyingEntity instanceof Sentient) {
                final Sentient being = (Sentient) insentientDyingEntity;
                double multiplier = this.properties.getExperienceLossMultiplier();
                if (attacker != null) {
                    multiplier = this.properties.getExperienceLossMultiplierForPVP();
                }
                being.loseExperienceFromDeath(multiplier, attacker != null);
            }

            // Need to call the InsentientKillEvent before actually processing anything
            if (attacker instanceof Sentient) {
                if (this.plugin.getCombatTracker().isInCombatWith(insentientDyingEntity, attacker)) {
                    Map<Insentient, EnterCombatReason> damageMap = this.plugin.getCombatTracker().getCombatants(insentientDyingEntity);
                    InsentientKillEvent killEvent = new InsentientKillEvent(attacker, insentientDyingEntity, damageMap.get(attacker));
                    this.plugin.getServer().getPluginManager().callEvent(killEvent);
                    if (insentientDyingEntity instanceof Monster || insentientDyingEntity instanceof Sentient) {
                        grantKillingExperience((Sentient) attacker, insentientDyingEntity);
                    }
                }

            }
            // Handle experience loss and leaving combat
            if (damageWrapper.getModifiedCause() != DamageCause.SUICIDE) {
                this.plugin.getCombatTracker().leaveCombat(insentientDyingEntity, LeaveCombatReason.DEATH);
            } else {
                this.plugin.getCombatTracker().leaveCombat(insentientDyingEntity, LeaveCombatReason.SUICIDE);
            }

            // Remove any nonpersistent effects
            for (final IEffect effect : insentientDyingEntity.getEffects()) {
                if (!effect.isPersistent()) {
                    insentientDyingEntity.removeEffect(effect);
                }
            }
        }
    }

    private void grantKillingExperience(Sentient attacker, Insentient dyingEntity) {

        FixedPoint addedExp = new FixedPoint();
        ExperienceType experienceType = null;

        // If this entity is on the summon map, don't award XP!
        if (attacker.equals(dyingEntity)) {
            return;
        }

        // We can use the sentient's dying experience
        if (dyingEntity instanceof Sentient) {
            // Don't award XP for Players killing themselves
            addedExp.add(this.properties.getPlayerKillingExperience());
            experienceType = ExperienceType.PVP;

        } else if (dyingEntity instanceof Monster) { // Otherwise, we have to use the Monster experience
            final Monster monster = (Monster) dyingEntity;
            addedExp = monster.getRewardExperience();
            // If EXP hasn't been assigned for this Entity then we stop here.
            if ((addedExp.intValue() == -1) && !this.properties.hasEntityRewardType(dyingEntity.getEntityType())) {
                return;
            } else if (addedExp.intValue() == -1) {
                addedExp = this.properties.getEntityReward(dyingEntity.getEntityType());
            }
            experienceType = ExperienceType.PVE;

            // Check if the kill was near a spawner
            if (this.properties.allowSpawnCamping() && (monster.getSpawnReason() == SpawnReason.SPAWNER)) {
                addedExp.mult(this.properties.getSpawnCampingMultiplier());
            }
        }

        if ((experienceType != null) && (addedExp.intValue() > 0)) {
            if (!(attacker instanceof PartyMember)) {
                attacker.gainExperience(addedExp, experienceType, dyingEntity.getLocation());
            } else {
                PartyMember member = (PartyMember) attacker;
                if (member.hasParty()) {
                    member.getParty().gainExperience(addedExp, experienceType, dyingEntity.getLocation());
                } else if (attacker.canGainExperience(experienceType)) {
                    attacker.gainExperience(addedExp, experienceType, dyingEntity.getLocation());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityTarget(EntityTargetEvent event) {
        // TODO handle invisibility effects and such
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageEarly(EntityDamageEvent event) {
        Entity e = event.getEntity();
        if (e instanceof LivingEntity) {
            this.plugin.getEntityManager().getEntity(e);
        }

        if (event instanceof EntityDamageByEntityEvent) {
            Entity d = ((EntityDamageByEntityEvent) event).getDamager();
            if (d instanceof LivingEntity) {
                this.plugin.getEntityManager().getEntity(d);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        event.getEntity().setMetadata(SPAWNREASON_META_KEY, new FixedMetadataValue(this.plugin, event.getSpawnReason()));
        if (!this.plugin.getEntityManager().isEntityManaged(event.getEntity())) {
            // We just need to make the EntityManager aware of the newly spawned
            // entity in the event another plugin hasn't done so already
            this.plugin.getEntityManager().getEntity(event.getEntity());
        }
    }
}
