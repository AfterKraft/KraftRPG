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
package com.afterkraft.kraftrpg.compat.v1_7_R2;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.Set;


import net.minecraft.server.v1_7_R2.DamageSource;
import net.minecraft.server.v1_7_R2.EntityGolem;
import net.minecraft.server.v1_7_R2.EntityLiving;
import net.minecraft.server.v1_7_R2.EntityMonster;
import net.minecraft.server.v1_7_R2.EntityPlayer;
import net.minecraft.server.v1_7_R2.MobEffect;
import net.minecraft.server.v1_7_R2.PacketPlayOutEntityEffect;
import net.minecraft.server.v1_7_R2.PacketPlayOutRemoveEntityEffect;
import net.minecraft.server.v1_7_R2.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R2.event.CraftEventFactory;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Giant;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;

/**
 * Author: gabizou
 */
public class RPGHandler extends CraftBukkitHandler {


    private Field ldbpt;
    private Random random;

    public RPGHandler() {
        try {
            ldbpt = EntityLiving.class.getDeclaredField("lastDamageByPlayerTime");
            ldbpt.setAccessible(true);
        } catch (final SecurityException e) {
        } catch (final NoSuchFieldException e) {
        }
        random = new Random();
    }

    @Override
    public final double getPostArmorDamage(LivingEntity defender, double damage) {
        int i = 25 - ((CraftLivingEntity)defender).getHandle().aU();
        float f1 = (float)damage * (float) i;
        return f1/25;
    }

    @Override
    public void bukkit_setArrowDamage(Arrow arrow, double damage) {
        ((CraftArrow) arrow).getHandle().b(damage);
    }

    @Override
    public void setPlayerExpZero(Player player) {
        final EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        entityPlayer.exp = 0;
        entityPlayer.expTotal = 0;
        entityPlayer.expLevel = 0;
    }

    @Override
    public void knockBack(LivingEntity target, LivingEntity attacker,double damage) {
        final EntityLiving el = ((CraftLivingEntity )target).getHandle();
        final EntityLiving aEL = ((CraftLivingEntity) attacker).getHandle();
        el.velocityChanged = true;
        double d0 = aEL.locX - el.locX;
        double d1;

        for (d1 = aEL.locZ - el.locZ; ((d0 * d0) + (d1 * d1)) < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
            d0 = (Math.random() - Math.random()) * 0.01D;
        }

        el.az = (float) ((Math.atan2(d1, d0) * 180.0D) / 3.1415927410125732D) - el.yaw;
        el.a(aEL, (float) damage, d0, d1);
    }

    @Override
    public void refreshLastPlayerDamageTime(LivingEntity entity) {
        try {
            ldbpt.set(((CraftLivingEntity)entity).getHandle(), 60);
        } catch (final IllegalArgumentException e) {
        } catch (final IllegalAccessException e) {
        }
    }

    @Override
    public boolean damageEntity(LivingEntity target, LivingEntity attacker, double damage, DamageCause cause, boolean knockback) {
        if (target.isDead() || (target.getHealth() <= 0)) {
            return false;
        }
        //Do it ourselves cause bukkit is stubborn
        int originalNoDamageTicks = target.getNoDamageTicks();
        target.setNoDamageTicks(0);


        final EntityDamageByEntityEvent edbe = new EntityDamageByEntityEvent(attacker, target, cause, damage);
        Bukkit.getServer().getPluginManager().callEvent(edbe);
        if (edbe.isCancelled()) {
            return false;
        }

        target.setLastDamageCause(edbe);
        final double oldHealth = target.getHealth();
        double newHealth = oldHealth - edbe.getDamage();
        if (newHealth < 0) {
            newHealth = 0;
        }
        final EntityLiving el = ((CraftLivingEntity) target).getHandle();
        el.lastDamage = (float) edbe.getDamage();
        el.aw = (float) oldHealth;
        el.hurtTicks = el.ay = 10;
        el.az = 0.0F;
        if (knockback) {
            knockBack(target, attacker, edbe.getDamage());
        }
        el.world.broadcastEntityEffect(el, (byte) 2);

        el.lastDamager = ((CraftLivingEntity) attacker).getHandle();

        // Set last damage by player time via reflection.
        if (attacker instanceof Player) {
            try {
                ldbpt.set(el, 60);
            } catch (final IllegalArgumentException e) {
            } catch (final IllegalAccessException e) {
            }
        }
        el.setHealth((float) newHealth);
        //If the target would die, kill it!
        if (newHealth <= 0) {
            el.world.makeSound(el, getSoundName(target.getType()), 1.0f, getSoundStrength(target));

            if (attacker instanceof Player) {
                final EntityPlayer p = ((CraftPlayer) attacker).getHandle();
                el.killer = p;
                el.die(DamageSource.playerAttack(p));
            } else {
                final EntityLiving att = ((CraftLivingEntity) attacker).getHandle();
                el.die(DamageSource.mobAttack(att));
            }
        } else {
            target.setNoDamageTicks(originalNoDamageTicks);

            final EntityLiving attackEntity = ((CraftLivingEntity) attacker).getHandle();
            if (target instanceof Monster) {
                if ((target instanceof Blaze) || (target instanceof Enderman) || (target instanceof Spider) || (target instanceof Giant) || (target instanceof Silverfish)) {
                    final EntityMonster em = (EntityMonster) el;
                    final EntityTargetEvent event = CraftEventFactory.callEntityTargetEvent(em, attackEntity, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY);
                    if (!event.isCancelled()) {
                        em.setTarget(attackEntity);
                    }
                }
            } else if (target instanceof IronGolem) {
                final EntityGolem eg = (EntityGolem) el;
                eg.setTarget(((CraftLivingEntity) attacker).getHandle());
            } else if ((target instanceof Wolf) && (((Wolf) target).getTarget() == null)) {
                final Wolf wolf = (Wolf) target;
                wolf.setAngry(true);
                wolf.setTarget(attacker);
            }
            if (target instanceof PigZombie) {
                ((PigZombie) target).setAngry(true);
            }

        }
        return true;
    }

    @Override
    protected float getSoundStrength(LivingEntity entity) {
        EntityLiving el = ((CraftLivingEntity)entity).getHandle();
        return el.isBaby() ? ((random.nextFloat() - random.nextFloat()) * 0.2F) + 1.5F : ((random.nextFloat() - random.nextFloat()) * 0.2F) + 1.0F;
    }

    @Override
    public void playClientEffect(Player player, Location startLocation,
                                 String particle, Vector offset, float speed, int count,
                                 boolean sendToAll) {
        if (!(player instanceof CraftPlayer)) {
            throw new IllegalArgumentException("The provided player is NOT a CraftPlayer!");
        }
        try {
            PacketPlayOutWorldParticles clientEffectPacket = new PacketPlayOutWorldParticles(particle,
                    (float) startLocation.getX(),
                    (float) (startLocation.getY() + 0.5),
                    (float) startLocation.getZ(),
                    (float) (offset.getX() + 0.5),
                    (float) (offset.getY() + 0.3),
                    (float) (offset.getZ() + 0.5),
                    speed, count);

            if (sendToAll) {
                ((CraftWorld) startLocation.getWorld()).getHandle().getTracker().sendPacketToEntity(((CraftPlayer) player).getHandle(), clientEffectPacket);
            }
            else {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(clientEffectPacket);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendFakePotionEffectPacket(PotionEffect effect, Player player) {
        EntityPlayer ePlayer = ((CraftPlayer)player).getHandle();
        ePlayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(ePlayer.getId(), new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier())));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendFakePotionEffectPackets(Set<PotionEffect> effects, Player player) {
        EntityPlayer ePlayer = ((CraftPlayer)player).getHandle();
        for(PotionEffect effect : effects) {
            ePlayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(ePlayer.getId(), new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier())));
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFakePotionEffectPacket(PotionEffect effect, Player player) {
        EntityPlayer ePlayer = ((CraftPlayer)player).getHandle();
        ePlayer.playerConnection.sendPacket(new PacketPlayOutRemoveEntityEffect(ePlayer.getId(), new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier())));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFakePotionEffectPackets(Set<PotionEffect> effects, Player player) {
        EntityPlayer ePlayer = ((CraftPlayer)player).getHandle();
        for(PotionEffect effect : effects) {
            ePlayer.playerConnection.sendPacket(new PacketPlayOutRemoveEntityEffect(ePlayer.getId(), new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier())));
        }
    }

}
