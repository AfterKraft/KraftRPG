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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.PartyMember;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.entity.effects.EffectType;
import com.afterkraft.kraftrpg.api.entity.effects.IEffect;
import com.afterkraft.kraftrpg.api.events.entity.damage.InsentientDamageEvent;
import com.afterkraft.kraftrpg.api.events.entity.damage.InsentientDamageInsentientEvent;
import com.afterkraft.kraftrpg.api.events.entity.damage.SkillDamageEvent;
import com.afterkraft.kraftrpg.api.events.entity.damage.WeaponDamageEvent;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.listeners.SkillDamageWrapper;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.SkillType;
import com.afterkraft.kraftrpg.api.skills.SkillUseObject;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.entity.party.RPGPartyManager;
import com.afterkraft.kraftrpg.util.Messaging;

public class DamageListener extends AbstractListener {

    protected DamageListener(RPGPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity) || !(plugin.getEntityManager().isEntityManaged(event.getEntity())) || !(plugin.getEntityManager().getEntity(event.getEntity()) instanceof Insentient)) {
            return;
        }

        double amount = event.getAmount();
        final Insentient being = (Insentient) plugin.getEntityManager().getEntity(event.getEntity());
        final double maxHealth = being.getMaxHealth();

        // Satiated players regenerate % of total HP rather than 1 HP
        double healPercent;
        switch (event.getRegainReason()) {
            case SATIATED:
                healPercent = plugin.getProperties().getFoodHealPercent();
                amount = Math.ceil(maxHealth * healPercent);
                break;
            case MAGIC:
                healPercent = amount / 6.0;
                amount = Math.ceil(healPercent * plugin.getProperties().getFoodHealthPerTier() * maxHealth);
                break;
            case CUSTOM:
            case WITHER_SPAWN:
            case WITHER:
                healPercent = amount / 20.0;
                amount = Math.ceil(maxHealth * healPercent);
                break;
            default:
                break;
        }
        event.setAmount(amount);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        final Entity defendingEntity = event.getEntity();
        IEntity defendingIEntity = plugin.getEntityManager().getEntity(defendingEntity);
        Entity attackingEntity = null;
        IEntity attackingIEntity = null;
        // Initialize the wrapped object for handling this event.
        DamageWrapper wrapper = new DamageWrapper(event.getCause(), event.getDamage(), event.getDamage(), event.getCause());

        double damage = event.getDamage();
        // We need to check if any exterior plugin added an IEntity to our EntityManager
        // as a SkillCaster or just plain special Monster
        // We use this val for other purposes in the remaining listeners
        final boolean isManaged = plugin.getEntityManager().isEntityManaged(defendingEntity);
        boolean alreadyProcessed = false;
        if (event instanceof EntityDamageByEntityEvent) {
            attackingEntity = ((EntityDamageByEntityEvent) event).getDamager();
            // We should try to get the IEntity if it's registered.
            if (plugin.getEntityManager().isEntityManaged(attackingEntity)) {
                attackingIEntity = plugin.getEntityManager().getEntity(attackingEntity);
            }
        }

        if (defendingIEntity == null) {
            // Means that the EntityManager did not have this entity registered.
            return;
        }
        if (defendingEntity instanceof LivingEntity) {
            if (defendingEntity.isDead() || ((LivingEntity) defendingEntity).getHealth() <= 0) {
                return;
            } else if (defendingEntity instanceof Player) {
                final Player player = (Player) defendingEntity;
                // We need to cancel the event at all times if the player is
                // in creative mode. Otherwise, we risk damaging a player
                // while in Creative mode
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    // TODO Maybe add an override option so that it can be toggled
                    event.setCancelled(true);
                    return;
                }
                if (defendingIEntity instanceof Champion) {
                    wrapper = ((Champion) defendingIEntity).getDamageWrapper();
                }
            }
        }

        // Preprocess some default things before we start performing serious
        // calculations on damages and damage sources
        if (defendingIEntity instanceof Insentient) {
            Insentient being = (Insentient) defendingIEntity;
            if (being.hasEffectType(EffectType.INVULNERABILITY)) {
                event.setCancelled(true);
            }

            // We can't damage the entity any further.
            if (((being.getNoDamageTicks() > 10) && damage > 0) || being.isDead() || being.getHealth() <= 0) {
                event.setCancelled(true);
                return;
            }

            // Check if the attackingEntity is an instance of Projectile,
            // If so, we need to re-assign the variables for the proper
            // Insentient being shooting the arrow.
            if (attackingEntity instanceof Projectile && ((Projectile) attackingEntity).getShooter() instanceof LivingEntity) {
                attackingEntity = (LivingEntity) ((Projectile) attackingEntity).getShooter();
                attackingIEntity = plugin.getEntityManager().getEntity(attackingEntity);
            }
        }

        // SkillTarget checks, since we need to see if any skills targeted the defending entity.
        if (plugin.getSkillManager().isSkillTarget(defendingEntity)) {
            alreadyProcessed = true;
            // Need to handle the damage to armor
            damage = onSkillDamage(event, attackingEntity, defendingEntity, damage);

            if (event.isCancelled()) {
                if (defendingIEntity instanceof Insentient) {
                    ((Insentient) defendingIEntity).setDamageWrapper(wrapper);
                }
                return;
            }
        } else {
            final EntityDamageEvent.DamageCause cause = event.getCause();
            switch (cause) {
                case SUICIDE: // DONE
                    if (defendingEntity instanceof LivingEntity) {
                        final LivingEntity livingEntity = (LivingEntity) defendingEntity;
                        if ((livingEntity.getLastDamageCause() != null) && (livingEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
                            final Entity tempDamager = ((EntityDamageByEntityEvent) livingEntity.getLastDamageCause()).getDamager();
                            livingEntity.setLastDamageCause(new EntityDamageByEntityEvent(tempDamager, livingEntity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1000D));
                            livingEntity.damage(1000, tempDamager);
                            event.setDamage(0);
                        } else {
                            event.setDamage(1000); //OVERKILLLLL!!
                            return;
                        }
                    }
                    break;
                case ENTITY_ATTACK: // Done
                case PROJECTILE: // Done
                    // When the attack is guaranteed to have been caused by an enemy entity
                    alreadyProcessed = true;
                    if (event.getDamage() == 0) {
                        damage = 0;
                    } else {
                        damage = onEntityDamage(event, attackingEntity, defendingEntity, attackingIEntity, defendingIEntity, event.getDamage());
                    }
                    break;
                case FALL: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.SAFEFALL);
                    break;
                case SUFFOCATION: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.RESIST_EARTH);
                    break;
                case DROWNING: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.WATER_BREATHING, EffectType.RESIST_WATER);
                    break;
                case STARVATION: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage());
                    break;
                case CONTACT: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.RESIST_EARTH);
                    break;
                case FIRE:
                case LAVA:
                case FIRE_TICK: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.RESIST_FIRE);
                    break;
                case POISON: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.RESIST_POISON);
                    break;
                case BLOCK_EXPLOSION:
                case ENTITY_EXPLOSION: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage());
                    break;
                case MELTING: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage());
                    break;
                case THORNS: // DONE
                    damage = onSpiked(event, defendingEntity, event.getDamage());
                    break;
                case WITHER: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage());
                    break;
                case MAGIC: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.RESIST_MAGICAL);
                    break;
                case VOID: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.RESIST_VOID);
                    break;
                case LIGHTNING: // DONE
                    damage = onResistDamage(event, defendingEntity, event.getDamage(), EffectType.RESIST_LIGHTNING);
                    break;
                case FALLING_BLOCK:
                case CUSTOM:
                default:
                    break;
            }

            if (event.isCancelled()) {
                if (defendingEntity instanceof Player) {
                    ((Champion) defendingIEntity).setDamageWrapper(wrapper);
                }
                return;
            }
        }

        // We need to process a few things depending on the types of Entity
        // and IEntity. More importantly to handle some effects.
        if (defendingEntity instanceof LivingEntity || defendingIEntity instanceof Insentient) {
            Insentient being = (Insentient) defendingIEntity;
            if (being.hasEffectType(EffectType.INVULNERABILITY)) {
                event.setCancelled(true);
            }

            // We can't damage the entity any further.
            if (((being.getNoDamageTicks() > 10) && damage > 0) || being.isDead() || being.getHealth() <= 0) {
                event.setCancelled(true);
                return;
            }

            // Check if the attackingEntity is an instance of Projectile,
            // If so, we need to re-assign the variables for the proper IEntity
            if (attackingEntity instanceof Projectile && ((Projectile) attackingEntity).getShooter() instanceof LivingEntity) {
                attackingEntity = (LivingEntity) ((Projectile) attackingEntity).getShooter();
                attackingIEntity = plugin.getEntityManager().getEntity(attackingEntity);
            }

            // Check that the LivingEntity isn't inflicting damage to itself.
            // If it isn't, we need to remove the appropriate effects
            if (!defendingEntity.equals(attackingEntity)) {
                if (damage > 0) {
                    for (final IEffect effect : being.getEffects()) {
                        if (effect.isType(EffectType.ROOT) || effect.isType(EffectType.INVIS) && !effect.isType(EffectType.UNBREAKABLE)) {
                            being.removeEffect(effect);
                        }
                    }
                }
            }
        }

        // We can only cancel damage events with friendlies if the PartyManager
        // is our own. Then we can continue to process and cancel the event.
        if (plugin.getPartyManager() instanceof RPGPartyManager && defendingIEntity instanceof PartyMember && attackingIEntity instanceof PartyMember) {
            if (plugin.getPartyManager().isFriendly((PartyMember) defendingIEntity, (PartyMember) attackingIEntity)) {
                event.setCancelled(true);
                return;
            }
        }

        // TODO Add check for summons and party summons

        // Here we need to check if the event is handled already and whether
        // the defender is an Insentient.
        if (!alreadyProcessed) {
            if (defendingIEntity instanceof Insentient) {
                final InsentientDamageEvent insentientDamageEvent = new InsentientDamageEvent((Insentient) defendingIEntity, event.getCause(), damage, damage, plugin.getProperties().isVaryingDamageEnabled());
                Bukkit.getPluginManager().callEvent(insentientDamageEvent);
                if (insentientDamageEvent.isCancelled()) {
                    event.setCancelled(true);
                    ((Insentient) defendingIEntity).setDamageWrapper(wrapper);
                    return;
                } else {
                    damage = insentientDamageEvent.getFinalDamage();
                }
            }
        }

        if (damage == 0) {
            event.setDamage(0);
            return;
        }

        event.setDamage(damage);

        if ((defendingEntity instanceof ComplexEntityPart) || (defendingEntity instanceof ComplexLivingEntity)) {
            // Handle ComplexLivingEntity stuffs
            ComplexLivingEntity complexEntity;
            if (defendingEntity instanceof ComplexLivingEntity) {
                complexEntity = (ComplexLivingEntity) defendingEntity;
            } else {
                complexEntity = ((ComplexEntityPart) defendingEntity).getParent();
            }
            if (((complexEntity.getNoDamageTicks() > 10) && (damage > 0)) || complexEntity.isDead() || (complexEntity.getHealth() <= 0)) {
                event.setCancelled(true);
                return;
            }
            event.setDamage(damage);
        } else if (defendingEntity instanceof LivingEntity) {
            // Perform last minute damage checks and damageTicks.
            LivingEntity livingEntity = (LivingEntity) defendingEntity;
            if (((livingEntity.getNoDamageTicks() > 10) && (damage > 0)) || livingEntity.isDead() || (livingEntity.getHealth() <= 0)) {
                event.setCancelled(true);
                return;
            }
            event.setDamage(damage);
        }

    }

    private double onSkillDamage(EntityDamageEvent event, Entity attacker, Entity defender, double damage) {
        // Ignore everything if the damage is 0. Bukkit sometimes will throw this in quick succession
        if (event.getDamage() == 0) {
            return 0;
        }
        // Get the skill use object
        final SkillUseObject skillInfo = plugin.getSkillManager().getSkillTargetInfo(defender);
        if (event instanceof EntityDamageByEntityEvent) {
            // We need to get the API interfaced stuffs
            SkillCaster caster = (SkillCaster) plugin.getEntityManager().getEntity(attacker);
            Insentient being = (Insentient) plugin.getEntityManager().getEntity(defender);

            // Check for possible resistances
            if (resistanceCheck(defender, skillInfo.getSkill())) {
                // Send the resist messages to all players in the location radius
                final Player[] players = plugin.getServer().getOnlinePlayers();
                ISkill skill = skillInfo.getSkill();
                for (final Player player : players) {
                    final Location playerLocation = player.getLocation();
                    final Champion champ = plugin.getEntityManager().getChampion(player);
                    if (champ.isIgnoringSkill(skill)) {
                        continue;
                    }
                    if (caster.getWorld().equals(playerLocation.getWorld()) && (skill.isInMessageRange(caster, champ))) {
                        Messaging.send(player, Messaging.getMessage("skill-defender-resist-effect"), Messaging.getEntityName(defender), skillInfo.getSkill().getName());
                    }
                }
                event.setCancelled(true);
                return 0;
            }
            // Call the API Event
            final SkillDamageEvent spellDamageEvent = new SkillDamageEvent(caster, being, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage, damage, plugin.getProperties().isVaryingDamageEnabled());
            plugin.getServer().getPluginManager().callEvent(spellDamageEvent);
            if (spellDamageEvent.isCancelled()) {
                event.setCancelled(true);
                return 0;
            }
            damage = spellDamageEvent.getFinalDamage();

            // Double check the armor resistance damages for Entity Attack
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                damage = CraftBukkitHandler.getInterface().getPostArmorDamage(being, damage);
            }

            // Reset the wrapper
            being.setDamageWrapper(new SkillDamageWrapper(skillInfo.getCaster(), skillInfo.getSkill(), event.getCause(), event.getDamage(), damage, EntityDamageEvent.DamageCause.ENTITY_ATTACK));

            plugin.getSkillManager().removeSkillTarget(defender, caster, skillInfo.getSkill());
        }

        return damage;
    }

    private double onEntityDamage(EntityDamageEvent event, Entity attacker, Entity defender, IEntity attackingIEntity, IEntity defendingIEntity, double damage) {
        final double initialDamage = damage;
        // Get the projectile shooter instead of the the arrow.
        if (attacker instanceof Projectile && ((Projectile) attacker).getShooter() instanceof LivingEntity) {
            attacker = (LivingEntity) ((Projectile) attacker).getShooter();
            attackingIEntity = plugin.getEntityManager().getEntity(attacker);
        }

        if (event instanceof EntityDamageByEntityEvent) {
            Insentient attackingInsentient = (Insentient) attackingIEntity;
            damage = plugin.getDamageManager().getHighestItemDamage(attackingInsentient, (Insentient) defendingIEntity, event.getDamage());

            // Cancel the event if the attackingInsentient can't equip the item
            if (!attackingInsentient.canEquipItem(attackingInsentient.getItemInHand())) {
                event.setCancelled(true);
                return 0;
            }
            // We must check the item in hand and for all possible damages it may have.
            // This needs to be improved later on as we need to handle customized damages
            if (attackingInsentient.getItemInHand().getType() != Material.AIR && plugin.getDamageManager().isStandardWeapon(attackingInsentient.getItemInHand().getType())) {
                damage = plugin.getDamageManager().getDefaultItemDamage(attackingInsentient.getItemInHand().getType(), damage);
                final WeaponDamageEvent weaponEvent = new WeaponDamageEvent(attackingInsentient, (Insentient) defendingIEntity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, attackingInsentient.getItemInHand(), initialDamage, damage, plugin.getProperties().isVaryingDamageEnabled());
                if (weaponEvent.isCancelled()) {
                    damage = 0D;
                    event.setCancelled(true);
                    event.setDamage(0D);
                    return damage;
                } else {
                    damage = weaponEvent.getFinalDamage();
                }
            } else {
                // We need to handle for when the defending entity is just being touched by something unidentified
                final InsentientDamageInsentientEvent insentientEvent = new InsentientDamageInsentientEvent(attackingInsentient, (Insentient) defendingIEntity, event.getCause(), initialDamage, damage, plugin.getProperties().isVaryingDamageEnabled());
                if (insentientEvent.isCancelled()) {
                    damage = 0D;
                    event.setCancelled(true);
                    event.setDamage(0D);
                    return damage;
                } else {
                    damage = insentientEvent.getFinalDamage();
                }
            }
        }
        return damage;
    }

    private double onResistDamage(EntityDamageEvent event, Entity damagee, double damage, EffectType... type) {
        if (!(damagee instanceof LivingEntity) || !(plugin.getEntityManager().getEntity(damagee) instanceof Insentient)) {
            return 0;
        }
        final Insentient being = (Insentient) plugin.getEntityManager().getEntity(damagee);

        for (EffectType effectType : type) {
            if (being.hasEffectType(effectType)) {
                event.setCancelled(true);
                return 0;
            }
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            damage -= plugin.getDamageManager().getFallReduction(being);

        }
        final Double damagePercent = plugin.getDamageManager().getEnvironmentalDamage(event.getCause());
        if (damage <= 0) {
            event.setCancelled(true);
            return 0;
        }
        if (damagePercent == null) {
            return damage;
        }

        damage = damage * damagePercent * being.getMaxHealth();
        return damage < 1 ? 1 : damage;
    }

    private double onSpiked(EntityDamageEvent event, Entity spiked, double damage) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity attacking = entityEvent.getDamager();
            IEntity attackingIEntity = plugin.getEntityManager().getEntity(attacking);
            if (attackingIEntity instanceof Insentient) {
                Insentient attackingInsentient = (Insentient) attackingIEntity;
                ItemStack[] armor = attackingInsentient.getArmor();
                double tempDamage = 0D;
                for (ItemStack item : armor) {
                    tempDamage += plugin.getDamageManager().getItemEnchantmentDamage(attackingInsentient, Enchantment.THORNS, item);
                }
                // TODO add considerations for the defending entity with resistances and other effects
                return tempDamage == 0 ? damage : tempDamage;
            }
        }
        return damage;
    }

    private boolean resistanceCheck(Entity defender, ISkill skill) {
        if (defender instanceof LivingEntity) {
            final Insentient being = (Insentient) plugin.getEntityManager().getEntity(defender);
            if (being.hasEffectType(EffectType.RESIST_AIR) && skill.isType(SkillType.ABILITY_PROPERTY_AIR)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_BLEED) && skill.isType(SkillType.ABILITY_PROPERTY_BLEED)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_DARK) && skill.isType(SkillType.ABILITY_PROPERTY_DARK)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_DISEASE) && skill.isType(SkillType.ABILITY_PROPERTY_DISEASE)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_EARTH) && skill.isType(SkillType.ABILITY_PROPERTY_EARTH)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_FIRE) && skill.isType(SkillType.ABILITY_PROPERTY_FIRE)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_ICE) && skill.isType(SkillType.ABILITY_PROPERTY_ICE)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_ILLUSION) && skill.isType(SkillType.ABILITY_PROPERTY_ILLUSION)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_LIGHT) && skill.isType(SkillType.ABILITY_PROPERTY_LIGHT)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_LIGHTNING) && skill.isType(SkillType.ABILITY_PROPERTY_LIGHTNING)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_MAGICAL) && skill.isType(SkillType.ABILITY_PROPERTY_MAGICAL)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_PHYSICAL) && skill.isType(SkillType.ABILITY_PROPERTY_PHYSICAL)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_POISON) && skill.isType(SkillType.ABILITY_PROPERTY_POISON)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_PROJECTILE) && skill.isType(SkillType.ABILITY_PROPERTY_PROJECTILE)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_SONG) && skill.isType(SkillType.ABILITY_PROPERTY_SONG)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_WATER) && skill.isType(SkillType.ABILITY_PROPERTY_WATER)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_POISON) && skill.isType(SkillType.ABILITY_PROPERTY_POISON)) {
                return true;
            } else if (being.hasEffectType(EffectType.RESIST_VOID) && skill.isType(SkillType.ABILITY_PROPERTY_VOID)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathEvent(EntityDeathEvent event) {

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (event.getEntity().getType() != EntityType.ARROW) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();
        ProjectileSource source = arrow.getShooter();
        double damage = 0;
        if (source instanceof BlockProjectileSource) {
            damage = plugin.getDamageManager().getEnvironmentalDamage(EntityDamageEvent.DamageCause.PROJECTILE);
            if (damage < 1) {
                damage = 1;
            }
        } else if (source instanceof Player) {
            damage = getPlayerProjectileDamage((Player) source, arrow, 0);
            if (((Player) source).getItemInHand().getType() == Material.BOW) {
                damage += getExtraBowDamage(((Player) source).getItemInHand());
            }
        } else if (source instanceof LivingEntity) {
            LivingEntity shooter = (LivingEntity) source;
            switch (shooter.getType()) {
                case SKELETON:
                case ZOMBIE:
                case PIG_ZOMBIE:
                    damage = plugin.getEntityManager().getMonster(shooter).getModifiedDamage();
                    break;
                default:
                    break;
            }
        }
        if (damage > 0) {
            CraftBukkitHandler.getInterface().modifyArrowDamage(arrow, damage);
        }
    }

    private double getPlayerProjectileDamage(Player attacker, Projectile projectile, double damage) {
        Champion champion = plugin.getEntityManager().getChampion(attacker);
        final double tempDamage = plugin.getDamageManager().getHighestProjectileDamage(champion, DamageManager.ProjectileType.valueOf(projectile));
        return tempDamage > 0 ? tempDamage : damage;
    }

    @SuppressWarnings("deprecation")
    private double getExtraBowDamage(ItemStack itemStack) {
        if (itemStack.getType() != Material.BOW) {
            return 0;
        }
        int amount = 0;
        for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
            Double val = plugin.getDamageManager().getEnchantmentDamage(entry.getKey(), entry.getValue());
            if (val == null) {
                continue;
            }
            if (entry.getKey().getId() == Enchantment.ARROW_DAMAGE.getId()) {
                amount += plugin.getDamageManager().getEnchantmentDamage(entry.getKey(), entry.getValue());
            }
        }
        return amount;
    }
}
