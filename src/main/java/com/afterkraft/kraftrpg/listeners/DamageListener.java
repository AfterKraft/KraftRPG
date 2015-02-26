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
package com.afterkraft.kraftrpg.listeners;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.entity.EntityChangeHealthEvent;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.event.Order;
import org.spongepowered.api.util.event.Subscribe;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.effects.EffectType;
import com.afterkraft.kraftrpg.api.effects.IEffect;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.PartyMember;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.entity.Summon;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.listeners.SkillDamageWrapper;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.SkillUseObject;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.api.util.DamageManager.ProjectileType;
import com.afterkraft.kraftrpg.entity.party.RPGPartyManager;
import com.afterkraft.kraftrpg.util.Messaging;

/**
 * DamageListener to handle all Damage related events.
 */
public class DamageListener extends AbstractListener {

    protected DamageListener(RPGPlugin plugin) {
        super(plugin);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
    /*

    @Subscribe(order = Order., ignoreCancelled = true)
    public void onEntityRegainHealth(EntityChangeHealthEvent event) {
        if (!(event.getEntity() instanceof Living) || !(this.plugin
                .getEntityManager()
                .isEntityManaged(event.getEntity())) || !(this.plugin
                .getEntityManager()
                .getEntity(event.getEntity()) instanceof Insentient)) {
            return;
        }

        double amount = event.getAmount();
        final Insentient being =
                (Insentient) this.plugin.getEntityManager()
                        .getEntity(event.getEntity());
        final double maxHealth = being.getMaxHealth();

        // Satiated players regenerate % of total HP rather than 1 HP
        double healPercent;
        switch (event.getRegainReason()) {
            case SATIATED:
                healPercent = this.plugin.getProperties().getFoodHealPercent();
                amount = Math.ceil(maxHealth * healPercent);
                break;
            case MAGIC:
                healPercent = amount / 6.0;
                amount = Math.ceil(healPercent * this.plugin.getProperties()
                        .getFoodHealthPerTier()
                                           * maxHealth);
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

    @Subscribe(order = Order.HIGH, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        final Entity defendingEntity = event.getEntity();
        IEntity defendingIEntity =
                this.plugin.getEntityManager().getEntity(defendingEntity);
        Entity attackingEntity = null;
        IEntity attackingIEntity = null;
        // Initialize the wrapped object for handling this event.
        DamageWrapper wrapper =
                new DamageWrapper(event.getCause(), event.getDamage(),
                                  event.getFinalDamage(),
                                  event.getCause());

        // TODO Somewhere... write some armor/item damage
        // handling that may ignore some of these damages
        // TODO Somehow, we should create and apply our own functions for these new DamageModifiers.
        final double initialDamage =
                event.getOriginalDamage(DamageModifier.BASE);
        final double initialArmor =
                event.getOriginalDamage(DamageModifier.ARMOR);
        final double initialAbsorbtion =
                event.getOriginalDamage(DamageModifier.ABSORPTION);
        final double initialBlocking =
                event.getOriginalDamage(DamageModifier.BLOCKING);
        final double initialMagic =
                event.getOriginalDamage(DamageModifier.MAGIC);
        final double initialResistance =
                event.getOriginalDamage(DamageModifier.RESISTANCE);
        double armorPercentage = (initialArmor / initialDamage);
        double absorptionPercentage = (initialAbsorbtion / initialDamage);
        double blockingPercentage = (initialBlocking / initialDamage);
        double magicPercentage = (initialMagic / initialDamage);
        double resistancePercentage = (initialResistance / initialResistance);
        double damage = event.getDamage();
        // We need to check if any exterior plugin added an IEntity to our EntityManager
        // as a SkillCaster or just plain special Monster
        // We use this val for other purposes in the remaining listeners
        final boolean isManaged =
                this.plugin.getEntityManager().isEntityManaged(defendingEntity);
        boolean alreadyProcessed = false;
        if (event instanceof EntityDamageByEntityEvent) {
            attackingEntity = ((EntityDamageByEntityEvent) event).getDamager();
            // We should try to get the IEntity if it's registered.
            if (this.plugin.getEntityManager()
                    .isEntityManaged(attackingEntity)) {
                attackingIEntity = this.plugin.getEntityManager()
                        .getEntity(attackingEntity);
            }
            // Cancel the PVP events if the attacking and
            // defending are players and world pvp is false.
            if ((!defendingEntity.getWorld().getPVP() && !defendingIEntity
                    .getWorld().getPVP()) && (
                    defendingEntity instanceof Player
                            || defendingIEntity instanceof Champion) && (
                    attackingEntity instanceof Player
                            || attackingIEntity instanceof Champion)) {
                event.setCancelled(true);
                ((Insentient) attackingIEntity)
                        .sendMessage(ChatColor.RED + Messaging
                                .getMessage("pvp_disabled"));
                return;
            }
        }

        if (defendingIEntity == null) {
            // Means that the EntityManager did not have this entity registered.
            return;
        }
        if (defendingEntity instanceof LivingEntity) {
            if (defendingEntity.isDead()
                    || ((LivingEntity) defendingEntity).getHealth() <= 0) {
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
                return;
            }

            // We can't damage the entity any further.
            if (((being.getNoDamageTicks() > 10) && damage > 0) || being
                    .isDead()
                    || being.getHealth() <= 0) {
                event.setCancelled(true);
                return;
            }

            // Check if the attackingEntity is an instance of Projectile,
            // If so, we need to re-assign the variables for the proper
            // Insentient being shooting the arrow.
            if (attackingEntity instanceof Projectile
                    && ((Projectile) attackingEntity)
                    .getShooter() instanceof LivingEntity) {
                attackingEntity = (LivingEntity) ((Projectile) attackingEntity)
                        .getShooter();
                attackingIEntity = this.plugin.getEntityManager()
                        .getEntity(attackingEntity);
            }

            // Cancel the PVP events if the attacking
            // and defending are players and world pvp is false.
            if (!defendingEntity.getWorld().getPVP() && (
                    defendingEntity instanceof Player
                            || defendingIEntity instanceof Champion) && (
                    attackingEntity instanceof Player
                            || attackingIEntity instanceof Champion)) {
                event.setCancelled(true);
                // By default, a player is always a Champion. If not, then there's a serious error.
                ((Insentient) attackingIEntity)
                        .sendMessage(ChatColor.RED + Messaging
                                .getMessage("pvp_disabled"));
                return;
            }

            // Check that the LivingEntity isn't inflicting damage to itself.
            // If it isn't, we need to remove the appropriate effects
            if (!defendingEntity.equals(attackingEntity)) {
                if (damage > 0) {
                    for (final IEffect effect : being.getEffects()) {
                        if (effect.isType(EffectType.ROOT)
                                || effect.isType(EffectType.INVIS) && !effect
                                .isType(EffectType.UNBREAKABLE)) {
                            being.removeEffect(effect);
                        }
                    }
                }
            }
        }

        // Handle summon damages.
        if (attackingIEntity instanceof Summon) {
            SkillCaster owner = ((Summon) attackingIEntity).getSummoner();
            if (defendingIEntity.equals(owner)) {
                event.setCancelled(true);
                return;
            }
            if (this.plugin.getPartyManager() instanceof RPGPartyManager
                    && defendingIEntity instanceof PartyMember) {
                if (this.plugin.getPartyManager()
                        .isFriendly((PartyMember) defendingIEntity, owner)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // We can only cancel damage events with friendlies if the PartyManager
        // is our own. Then we can continue to process and cancel the event.
        if (this.plugin.getPartyManager() instanceof RPGPartyManager
                && defendingIEntity instanceof PartyMember
                && attackingIEntity instanceof PartyMember) {
            if (this.plugin.getPartyManager()
                    .isFriendly((PartyMember) defendingIEntity,
                                (PartyMember) attackingIEntity)) {
                event.setCancelled(true);
                return;
            }
        }

        // TODO Rewrite this all for a new InsentientPreDamageEvent
        // to handle damage modification functions
        // TODO Handle all through a new proxy event. Do not use EntityDamageEvent anymore.

        // SkillTarget checks, since we need to see if any skills targeted the defending entity.
        if (this.plugin.getSkillManager().isSkillTarget(defendingEntity)) {
            alreadyProcessed = true;
            // Need to handle the damage to armor
            damage = onSkillDamage(event, attackingEntity, defendingEntity,
                                   damage);

            if (event.isCancelled()) {
                if (defendingIEntity instanceof Insentient) {
                    ((Insentient) defendingIEntity).setDamageWrapper(wrapper);
                }
                return;
            }
        } else {
            final EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == DamageCause.SUICIDE) {
                if (defendingEntity instanceof LivingEntity) {
                    final LivingEntity livingEntity =
                            (LivingEntity) defendingEntity;
                    if ((livingEntity.getLastDamageCause() != null)
                            && (livingEntity
                            .getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
                        final Entity tempDamager =
                                ((EntityDamageByEntityEvent) livingEntity
                                        .getLastDamageCause())
                                        .getDamager();
                        Map<DamageModifier, Double> modifiers =
                                new HashMap<>();
                        modifiers.put(DamageModifier.BASE, 1000D);
                        Map<DamageModifier, Function<? super Double, Double>>
                                uselessMap =
                                new HashMap<>();
                        livingEntity.setLastDamageCause(
                                new EntityDamageByEntityEvent(tempDamager,
                                                              livingEntity,
                                                              DamageCause.ENTITY_ATTACK,
                                                              modifiers,
                                                              uselessMap));
                        livingEntity.damage(1000, tempDamager);
                        event.setDamage(0);
                    } else {
                        event.setDamage(1000); //OVERKILLLLL!!
                        return;
                    }
                }
            } else if (cause == DamageCause.ENTITY_ATTACK
                    || cause == DamageCause.PROJECTILE) {
                // When the attack is guaranteed to have been caused by an enemy entity
                alreadyProcessed = true;
                if (event.getDamage() == 0) {
                    damage = 0;
                } else {
                    damage = onEntityDamage(event, attackingEntity,
                                            defendingEntity,
                                            attackingIEntity, defendingIEntity,
                                            event.getDamage());
                }
            } else if (cause == DamageCause.STARVATION
                    || cause == DamageCause.WITHER
                    || cause == DamageCause.MELTING
                    || cause == DamageCause.BLOCK_EXPLOSION
                    || cause == DamageCause.ENTITY_EXPLOSION) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage());
            } else if (cause == DamageCause.FALL) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage(),
                                        EffectType.SAFEFALL);
            } else if (cause == DamageCause.SUFFOCATION
                    || cause == DamageCause.CONTACT) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage(),
                                        EffectType.RESIST_EARTH);
            } else if (cause == DamageCause.DROWNING) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage(),
                                        EffectType.WATER_BREATHING,
                                        EffectType.RESIST_WATER);
            } else if (cause == DamageCause.FIRE || cause == DamageCause.LAVA
                    || cause == DamageCause.FIRE_TICK) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage(),
                                        EffectType.RESIST_FIRE);
            } else if (cause == DamageCause.POISON) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage(),
                                        EffectType.RESIST_POISON);
            } else if (cause == DamageCause.THORNS) {
                damage = onSpiked(event, defendingEntity, event.getDamage());
            } else if (cause == DamageCause.MAGIC) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage(),
                                        EffectType.RESIST_MAGICAL);
            } else if (cause == DamageCause.VOID) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage(),
                                        EffectType.RESIST_VOID);
            } else if (cause == DamageCause.LIGHTNING) {
                damage = onResistDamage(event, defendingEntity,
                                        event.getDamage(),
                                        EffectType.RESIST_LIGHTNING);
            }

            if (event.isCancelled()) {
                if (defendingEntity instanceof Player) {
                    ((Champion) defendingIEntity).setDamageWrapper(wrapper);
                }
                return;
            }
        }

        // Here we need to check if the event is handled already and whether
        // the defender is an Insentient.
        if (!alreadyProcessed) {
            if (defendingIEntity instanceof Insentient) {
                Map<DamageType, Double> modifiers = new EnumMap<>(
                        ImmutableMap.of(DamageType.PHYSICAL, damage));
                final InsentientDamageEvent insentientDamageEvent =
                        new InsentientDamageEvent((Insentient) defendingIEntity,
                                                  event, modifiers,
                                                  this.plugin.getProperties()
                                                          .isVaryingDamageEnabled());
                Bukkit.getPluginManager().callEvent(insentientDamageEvent);
                if (insentientDamageEvent.isCancelled()) {
                    event.setCancelled(true);
                    ((Insentient) defendingIEntity).setDamageWrapper(wrapper);
                    return;
                } else {
                    damage = insentientDamageEvent.getTotalDamage();
                }
            }
        }

        if (damage == 0) {
            event.setDamage(0);
            return;
        }

        setEventDamage(event, damage, absorptionPercentage, armorPercentage,
                       blockingPercentage,
                       magicPercentage, resistancePercentage);

        if ((defendingEntity instanceof ComplexEntityPart)
                || (defendingEntity instanceof ComplexLivingEntity)) {
            // Handle ComplexLivingEntity stuffs
            ComplexLivingEntity complexEntity;
            if (defendingEntity instanceof ComplexLivingEntity) {
                complexEntity = (ComplexLivingEntity) defendingEntity;
            } else {
                complexEntity =
                        ((ComplexEntityPart) defendingEntity).getParent();
            }
            if (((complexEntity.getNoDamageTicks() > 10) && (damage > 0))
                    || complexEntity.isDead()
                    || (complexEntity.getHealth() <= 0)) {
                event.setCancelled(true);
                return;
            }
            setEventDamage(event, damage, absorptionPercentage, armorPercentage,
                           blockingPercentage,
                           magicPercentage, resistancePercentage);

        } else if (defendingEntity instanceof LivingEntity) {
            // Perform last minute damage checks and damageTicks.
            LivingEntity livingEntity = (LivingEntity) defendingEntity;
            if (((livingEntity.getNoDamageTicks() > 10) && (damage > 0))
                    || livingEntity.isDead()
                    || (livingEntity.getHealth() <= 0)) {
                event.setCancelled(true);
                return;
            }
            setEventDamage(event, damage, absorptionPercentage, armorPercentage,
                           blockingPercentage,
                           magicPercentage, resistancePercentage);
        }
    }

    private double onSkillDamage(EntityDamageEvent event, Entity attacker,
                                 Entity defender,
                                 double damage) {
        // Ignore everything if the damage is 0.
        // Bukkit sometimes will throw this in quick succession
        if (event.getDamage() == 0) {
            return 0;
        }
        // Get the skill use object
        final SkillUseObject skillInfo =
                this.plugin.getSkillManager().getSkillTargetInfo(defender);
        if (event instanceof EntityDamageByEntityEvent) {
            // We need to get the API interfaced stuffs
            SkillCaster caster = (SkillCaster) this.plugin.getEntityManager()
                    .getEntity(attacker);
            Insentient being = (Insentient) this.plugin.getEntityManager()
                    .getEntity(defender);

            // Check for possible resistances
            if (resistanceCheck(defender, skillInfo.getSkill())) {
                // Send the resist messages to all players in the location radius
                final Collection<? extends Player> players =
                        this.plugin.getServer().getOnlinePlayers();
                ISkill skill = skillInfo.getSkill();
                for (final Player player : players) {
                    final Location playerLocation = player.getLocation();
                    final Champion champ =
                            this.plugin.getEntityManager().getChampion(player);
                    if (champ.isIgnoringSkill(skill)) {
                        continue;
                    }
                    if (caster.getWorld().equals(playerLocation.getWorld())
                            && (skill
                            .isInMessageRange(caster, champ))) {
                        Messaging.send(player, Messaging
                                               .getMessage(
                                                       "skill-defender-resist-effect"),
                                       Messaging.getEntityName(defender),
                                       skillInfo.getSkill().getName());
                    }
                }
                event.setCancelled(true);
                return 0;
            }
            // Double check the armor resistance damages for Entity Attack
            if (event.getCause() == DamageCause.ENTITY_ATTACK
                    || event.getCause() == DamageCause.PROJECTILE) {
                damage = RpgCommon.getHandler()
                        .getPostArmorDamage(being, event, damage);
            }

            // Reset the wrapper
            being.setDamageWrapper(
                    new SkillDamageWrapper(skillInfo.getCaster(),
                                           skillInfo.getSkill(),
                                           event.getCause(), event.getDamage(),
                                           damage,
                                           DamageCause.ENTITY_ATTACK));

            this.plugin.getSkillManager()
                    .removeSkillTarget(defender, caster, skillInfo.getSkill());
        }

        return damage;
    }

    private double onEntityDamage(EntityChangeHealthEvent event,
                                  Entity attacker, Entity defender,
                                  IEntity attackingIEntity,
                                  IEntity defendingIEntity, double damage) {
        // Get the projectile shooter instead of the the arrow.
        if (attacker instanceof Projectile && ((Projectile) attacker)
                .getShooter() instanceof Living) {
            attacker = (Living) ((Projectile) attacker).getShooter();
            attackingIEntity =
                    this.plugin.getEntityManager().getEntity(attacker).get();
        }

        if (event instanceof EntityA) {
            dou
            Insentient attackingInsentient = (Insentient) attackingIEntity;
            damage = this.plugin.getDamageManager()
                    .getHighestItemDamage(attackingInsentient,
                                          (Insentient) defendingIEntity,
                                          event.getDamage());

            // Cancel the event if the attackingInsentient can't equip the item
            if (!attackingInsentient
                    .canEquipItem(attackingInsentient.getItemInHand())) {
                event.setCancelled(true);
                return 0;
            }
            // We must check the item in hand and for all possible damages it may have.
            // This needs to be improved later on as we need to handle customized damages
            Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (event.getCause() == DamageCause.PROJECTILE
                    && damager instanceof Projectile) {
                damage = this.plugin.getDamageManager()
                        .getHighestProjectileDamage(
                                attackingInsentient, (ProjectileType
                                        .valueOf(damager.getType().name())));
                Map<DamageType, Double> modifiers = new EnumMap<>(
                        ImmutableMap.of(DamageType.PHYSICAL, damage));
                final ProjectileDamageEvent projectileDamageEvent =
                        new ProjectileDamageEvent(attackingInsentient,
                                                  (Insentient) defendingIEntity,
                                                  (Projectile) damager,
                                                  (EntityDamageByEntityEvent) event,
                                                  attackingInsentient
                                                          .getItemInHand(),
                                                  modifiers,
                                                  this.plugin.getProperties()
                                                          .isVaryingDamageEnabled());
                Bukkit.getPluginManager().callEvent(projectileDamageEvent);
                if (projectileDamageEvent.isCancelled()) {
                    damage = 0D;
                    event.setCancelled(true);
                    event.setDamage(0D);
                    return damage;
                } else {
                    damage = projectileDamageEvent.getTotalDamage();
                }
            } else if (attackingInsentient.getItemInHand().getType()
                    != Material.AIR && this.plugin
                    .getDamageManager()
                    .isStandardWeapon(
                            attackingInsentient.getItemInHand().getType())) {
                damage = this.plugin.getDamageManager()
                        .getDefaultItemDamage(
                                attackingInsentient.getItemInHand().getType(),
                                damage);
                Map<DamageType, Double> modifiers = new EnumMap<>(
                        ImmutableMap.of(DamageType.PHYSICAL, damage));
                final WeaponDamageEvent weaponEvent =
                        new WeaponDamageEvent(attackingInsentient,
                                              (Insentient) defendingIEntity,
                                              (EntityDamageByEntityEvent) event,
                                              attackingInsentient
                                                      .getItemInHand(),
                                              modifiers,
                                              this.plugin.getProperties()
                                                      .isVaryingDamageEnabled());
                Bukkit.getPluginManager().callEvent(weaponEvent);
                if (weaponEvent.isCancelled()) {
                    damage = 0D;
                    event.setCancelled(true);
                    event.setDamage(0D);
                    return damage;
                } else {
                    damage = weaponEvent.getTotalDamage();
                }
            } else {
                // We need to handle for when the defending entity is just being
                // touched by something unidentified
                Map<DamageType, Double> modifiers = new EnumMap<>(
                        ImmutableMap.of(DamageType.PHYSICAL, damage));
                final InsentientDamageInsentientEvent insentientEvent =
                        new InsentientDamageInsentientEvent(attackingInsentient,
                                                            (Insentient) defendingIEntity,
                                                            (EntityDamageByEntityEvent) event,
                                                            modifiers,
                                                            this.plugin
                                                                    .getProperties()
                                                                    .isVaryingDamageEnabled());
                Bukkit.getPluginManager().callEvent(insentientEvent);
                if (insentientEvent.isCancelled()) {
                    damage = 0D;
                    event.setCancelled(true);
                    event.setDamage(0D);
                    return damage;
                } else {
                    damage = insentientEvent.getTotalDamage();
                }
            }
        }
        return damage;
    }

    private double onResistDamage(EntityDamageEvent event, Entity damagee,
                                  double damage,
                                  EffectType... type) {
        if (!(damagee instanceof LivingEntity) || !(this.plugin
                .getEntityManager()
                .getEntity(damagee) instanceof Insentient)) {
            return 0;
        }
        final Insentient being =
                (Insentient) this.plugin.getEntityManager().getEntity(damagee);

        for (EffectType effectType : type) {
            if (being.hasEffectType(effectType)) {
                event.setCancelled(true);
                return 0;
            }
        }
        if (event.getCause() == DamageCause.FALL) {
            damage -= this.plugin.getDamageManager().getFallReduction(being);

        }
        final Double damagePercent =
                this.plugin.getDamageManager()
                        .getEnvironmentalDamage(event.getCause());
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

    private double onSpiked(EntityDamageEvent event, Entity spiked,
                            double damage) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent =
                    (EntityDamageByEntityEvent) event;
            Entity attacking = entityEvent.getDamager();
            IEntity attackingIEntity =
                    this.plugin.getEntityManager().getEntity(attacking);
            if (attackingIEntity instanceof Insentient) {
                Insentient attackingInsentient = (Insentient) attackingIEntity;
                ItemStack[] armor = attackingInsentient.getArmor();
                double tempDamage = 0D;
                for (ItemStack item : armor) {
                    tempDamage += this.plugin.getDamageManager()
                            .getItemEnchantmentDamage(attackingInsentient,
                                                      Enchantment.THORNS,
                                                      item);
                }
                // TODO add considerations for the defending
                // entity with resistances and other effects
                return tempDamage == 0 ? damage : tempDamage;
            }
        }
        return damage;
    }

    private void setEventDamage(EntityDamageEvent event, double damage,
                                double absorbtion,
                                double armor, double blocking, double magic,
                                double resistance) {
        event.setDamage(DamageModifier.BASE, damage);
        event.setDamage(DamageModifier.ABSORPTION, absorbtion * damage);
        event.setDamage(DamageModifier.ARMOR, armor * damage);
        event.setDamage(DamageModifier.BLOCKING, blocking * damage);
        event.setDamage(DamageModifier.MAGIC, magic * damage);
        event.setDamage(DamageModifier.RESISTANCE, resistance * damage);
    }

    private boolean resistanceCheck(Entity defender, ISkill skill) {
        if (defender instanceof LivingEntity) {
            final Insentient being =
                    (Insentient) this.plugin.getEntityManager()
                            .getEntity(defender);
            for (EffectType type : EffectType.values()) {
                if (type.isSkillResisted(being, skill)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Subscribe(order = Order.MONITOR)
    public void onEntityDeathEvent(EntityDeathEvent event) {

    }

    @Subscribe(order = Order.HIGH)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (event.getEntity().getType() != EntityType.ARROW) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();
        ProjectileSource source = arrow.getShooter();
        double damage = 0;
        if (source instanceof BlockProjectileSource) {
            damage = this.plugin.getDamageManager()
                    .getEnvironmentalDamage(DamageCause.PROJECTILE);
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
            IEntity entity = this.plugin.getEntityManager()
                    .getEntity((LivingEntity) source);
            if (entity instanceof Summon) {
                // TODO handle summon damages
            } else if (entity instanceof Monster) {
                switch (shooter.getType()) {
                    case SKELETON:
                    case ZOMBIE:
                    case PIG_ZOMBIE:
                        damage = this.plugin.getEntityManager()
                                .getMonster(shooter)
                                .getModifiedDamage();
                        break;
                    default:
                        break;
                }
            }
        }
        if (damage > 0) {
            RpgCommon.getHandler().modifyArrowDamage(arrow, damage);
        }
    }

    private double getPlayerProjectileDamage(Player attacker, Projectile
            projectile,
                                             double damage) {
        Champion champion = this.plugin.getEntityManager()
                .getChampion(attacker).get();
        final double tempDamage =
                this.plugin.getDamageManager().getHighestProjectileDamage(
                        champion, DamageManager.ProjectileType
                                .valueOf(projectile.getType().name()));
        return tempDamage > 0 ? tempDamage : damage;
    }

    @SuppressWarnings("deprecation")
    private double getExtraBowDamage(ItemStack itemStack) {
        if (itemStack.getItem() != ItemTypes.BOW) {
            return 0;
        }
        int amount = 0;
        for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments()
                .entrySet()) {
            Double val = this.plugin.getDamageManager()
                    .getEnchantmentDamage(entry.getKey(), entry.getValue());
            if (val == null) {
                continue;
            }
            if (entry.getKey().getId().equals(Enchantments.POWER.getId())) {
                amount += this.plugin.getDamageManager()
                        .getEnchantmentDamage(entry.getKey(), entry.getValue());
            }
        }
        return amount;
    }
    */
}
