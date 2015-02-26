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

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.effects.EffectType;
import com.afterkraft.kraftrpg.api.effects.IEffect;
import com.afterkraft.kraftrpg.api.effects.common.ProjectileShot;
import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;

/**
 * Standard Effects listener for imbuing skills.
 */
public class EffectsListener extends AbstractListener {

    protected EffectsListener(RPGPlugin plugin) {
        super(plugin);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }

    /*
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof LivingEntity) {
            Insentient being = (Insentient) this.plugin.getEntityManager()
                    .getEntity((LivingEntity) event.getEntity().getShooter());
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
            Insentient being = (Insentient) this.plugin.getEntityManager()
                    .getEntity((LivingEntity) event.getEntity().getShooter());
            if (being.hasEffectType(EffectType.IMBUE)) {
                for (IEffect effect : being.getEffects()) {
                    if (effect instanceof ProjectileShot) {
                        ((ProjectileShot) effect).onProjectileLand(event.getEntity(),
                                                                   event.getEntity().getLocation());
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager())
                .getShooter() instanceof LivingEntity) {
            Projectile projectile = (Projectile) event.getDamager();
            Insentient being = (Insentient) this.plugin.getEntityManager()
                    .getEntity((LivingEntity) projectile.getShooter());
            if (being.hasEffectType(EffectType.IMBUE)) {
                for (IEffect effect : being.getEffects()) {
                    if (effect instanceof ProjectileShot) {
                        IEntity rpgEntity =
                                this.plugin.getEntityManager().getEntity(event.getEntity());
                        if (rpgEntity instanceof Insentient) {
                            ((ProjectileShot) effect)
                                    .onProjectileDamage(projectile, (Insentient) rpgEntity);
                        }
                        return;
                    }
                }
            }
        }
    }
    */
}
