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

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
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
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.PartyMember;
import com.afterkraft.kraftrpg.api.entity.effects.EffectType;
import com.afterkraft.kraftrpg.api.entity.effects.IEffect;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.entity.party.PartyManager;
import com.afterkraft.kraftrpg.api.events.entity.damage.InsentientDamageEvent;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.entity.party.RPGPartyManager;

public class DamageListener extends AbstractListener {

    protected DamageListener(RPGPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        final Entity defendingEntity = event.getEntity();
        IEntity defendingIEntity = null;
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
            attackingIEntity = plugin.getEntityManager().getEntity(attackingEntity);
        }

        defendingIEntity = plugin.getEntityManager().getEntity(defendingEntity);
        if (defendingIEntity == null) {
            // Means that the EntityManager did not have this entity registered.
            return;
        }
        if (defendingEntity instanceof LivingEntity) {
            if (defendingEntity.isDead() || ((LivingEntity) defendingEntity).getHealth() <= 0) {
                return;
            } else if (defendingEntity instanceof Player) {
                final Player player = (Player) defendingEntity;
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    return;
                }
                if (defendingIEntity instanceof Champion) {
                    wrapper = ((Champion) defendingIEntity).getDamageWrapper();
                }
            }
        }

        // SkillTarget checks, since we need to see if any skills targetted
        // the entity.
        if (plugin.getSkillManager().isSkillTarget(defendingEntity)) {
            switch (event.getCause()) {
                case ENTITY_ATTACK:
                    alreadyProcessed = true;
                    damage = onEntityDamageByAttackSkill(event, attackingEntity, defendingEntity, damage);
                    break;
                default:
                    alreadyProcessed = true;
                    damage = onSkillDamage(event, defendingEntity, damage);
            }

            if (event.isCancelled()) {
                if (defendingEntity instanceof Player) {
                    ((Champion) defendingIEntity).setDamageWrapper(wrapper);
                }
                return;
            }
        } else {
            final EntityDamageEvent.DamageCause cause = event.getCause();
            switch (cause) {
                case SUICIDE:
                    if (defendingEntity instanceof Player) {
                        final Player player = (Player) defendingEntity;
                        if ((player.getLastDamageCause() != null) && (player.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
                            final Entity tempDamager = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                            player.setLastDamageCause(new EntityDamageByEntityEvent(tempDamager, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1000D));
                            player.damage(1000, tempDamager);
                            event.setDamage(0);
                        } else {
                            event.setDamage(1000); //OVERKILLLLL!!
                            return;
                        }
                    }
                    break;
                case ENTITY_ATTACK:
                case PROJECTILE:
                case FALLING_BLOCK:
                    alreadyProcessed = true;
                    damage = onEntityDamage(event, attackingEntity, defendingEntity);
                    break;
                case FALL:
                    damage = onFall(event, defendingEntity, event.getDamage());
                    break;
                case SUFFOCATION:
                    damage = onSuffocation(event, defendingEntity, event.getDamage());
                    break;
                case DROWNING:
                    damage = onDrowning(event, defendingEntity, event.getDamage());
                    break;
                case STARVATION:
                    damage = onStarving(event, defendingEntity, event.getDamage());
                    break;
                case CONTACT:
                    damage = onContact(event, defendingEntity, event.getDamage());
                    break;
                case FIRE:
                case LAVA:
                case FIRE_TICK:
                    damage = onFlame(event, defendingEntity, event.getDamage());
                    break;
                case POISON:
                    damage = onPoison(event, defendingEntity, event.getDamage());
                    break;
                case BLOCK_EXPLOSION:
                case ENTITY_EXPLOSION:
                    damage = onExplosion(event, defendingEntity, event.getDamage());
                    break;
                case MELTING:
                    damage = onMelting(event, defendingEntity, event.getDamage());
                    break;
                case THORNS:
                    damage = onSpiked(event, defendingEntity, event.getDamage());
                    break;
                case WITHER:
                    damage = onWither(event, defendingEntity, event.getDamage());
                    break;
                case MAGIC:
                    damage = onMagicDamage(event, defendingEntity, event.getDamage());
                    break;
                case VOID:
                    damage = onVoid(event, defendingEntity, event.getDamage());
                    break;
                case LIGHTNING:
                    damage = onLightningStrike(event, defendingEntity, event.getDamage());
                    break;
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
            LivingEntity livingEntity = (LivingEntity) defendingEntity;
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



        if (defendingEntity instanceof Player && defendingIEntity instanceof Champion) {
            final Champion champion = (Champion) defendingIEntity;
            champion.updateInventory();

            if (!isManaged) {
                final InsentientDamageEvent insentientDamageEvent = new InsentientDamageEvent(null, champion, damage, damage, plugin.getProperties().isVaryingDamageEnabled());
            }


        }

    }


    private double onEntityDamageByAttackSkill(EntityDamageEvent event, Entity attacker, Entity defender, double damage) {

        return 0D;
    }

    private double onSkillDamage(EntityDamageEvent event, Entity defendingEntity,  double damage) {

        return 0D;
    }

    private double onEntityDamage(EntityDamageEvent event, Entity attacker, Entity defender) {

        return 0D;
    }

    private double onFall(EntityDamageEvent event, Entity falling, double damage) {
        return 0D;
    }

    private double onSuffocation(EntityDamageEvent event, Entity suffocating, double damage) {

        return 0D;
    }

    private double onDrowning(EntityDamageEvent event, Entity drowning, double damage) {

        return 0D;
    }

    private double onStarving(EntityDamageEvent event, Entity starving, double damage) {

        return 0D;
    }

    private double onContact(EntityDamageEvent event, Entity defending, double damage) {

        return 0D;
    }

    private double onFlame(EntityDamageEvent event, Entity burning, double damage) {

        return 0D;
    }
    private double onPoison(EntityDamageEvent event, Entity poisoned, double damage) {

        return 0D;
    }

    private double onExplosion(EntityDamageEvent event, Entity exploded, double damage) {

        return 0D;
    }

    private double onMelting(EntityDamageEvent event, Entity melting, double damage) {

        return 0D;
    }

    private double onSpiked(EntityDamageEvent event, Entity melting, double damage) {

        return 0D;
    }

    private double onFallingBlock(EntityDamageEvent event, Entity smacked, double damage) {

        return 0D;
    }

    private double onWither(EntityDamageEvent event, Entity withered, double damage) {

        return 0D;
    }

    private double onMagicDamage(EntityDamageEvent event, Entity victim, double damage) {

        return 0D;
    }

    private double onVoid(EntityDamageEvent event, Entity fallen, double damage) {

        return 0D;
    }

    private double onLightningStrike(EntityDamageEvent event, Entity struck, double damage) {

        return 0D;
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
            Double val = plugin.getDamageManager().getEnchantmentDamage(entry.getKey());
            if (val == null) {
                continue;
            }
            if (entry.getKey().getId() == Enchantment.ARROW_DAMAGE.getId()) {
                amount += plugin.getDamageManager().getEnchantmentDamage(entry.getKey()) * entry.getValue();
            }
        }
        return amount;
    }
}
