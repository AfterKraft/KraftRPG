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

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.effects.EffectType;
import com.afterkraft.kraftrpg.api.effects.IEffect;
import com.afterkraft.kraftrpg.api.effects.common.ProjectileShot;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;

public class  EffectsListener extends AbstractListener {

    protected EffectsListener(RPGPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof LivingEntity) {
            Insentient being = (Insentient) this.plugin.getEntityManager().getEntity((LivingEntity) event.getEntity().getShooter());
            if (being.hasEffectType(EffectType.IMBUE)) {
                for (IEffect effect : being.getEffects()) {
                    if (effect instanceof ProjectileShot) {
                        ((ProjectileShot) effect).applyToProjectile(event.getEntity());
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLand(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof LivingEntity) {
            Insentient being = (Insentient) this.plugin.getEntityManager().getEntity((LivingEntity) event.getEntity().getShooter());
            if (being.hasEffectType(EffectType.IMBUE)) {
                for (IEffect effect : being.getEffects()) {
                    if (effect instanceof ProjectileShot) {
                        ((ProjectileShot) effect).onProjectileLand(event.getEntity(), event.getEntity().getLocation());
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {
            Projectile projectile = (Projectile) event.getDamager();
            Insentient being = (Insentient) this.plugin.getEntityManager().getEntity((LivingEntity) projectile.getShooter());
            if (being.hasEffectType(EffectType.IMBUE)) {
                for (IEffect effect : being.getEffects()) {
                    if (effect instanceof ProjectileShot) {
                        IEntity rpgEntity = this.plugin.getEntityManager().getEntity(event.getEntity());
                        if (rpgEntity instanceof Insentient) {
                            ((ProjectileShot) effect).onProjectileDamage(projectile, (Insentient) rpgEntity);
                        }
                        return;
                    }
                }
            }
        }
    }
}
