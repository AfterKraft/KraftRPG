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
package com.afterkraft.kraftrpg.compat.v1_7_R4;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectChangeEvent;
import org.bukkit.potion.PotionEffect;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;

public class TweakkitListener extends AbstractListener {
    protected TweakkitListener(RPGPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPotionEffectChangeEvent(EntityPotionEffectChangeEvent event) {
        switch (event.getCause()) {
            case POTION:
                handlePotionDamage(event.getEffect(), event.getEntity());
                break;
            case MOB:
                handleMob(event.getEffect(), event.getEntity());
                break;
            case BEACON:
                handleBeacon(event.getEffect(), event.getLocation());
                break;
            case FOOD:
                handleFood(event.getEffect(), event.getEntity());
                break;
            case GOLDEN_APPLE:
                handleGoldenApple(event.getEffect(), event.getEntity());
                break;
            case ENCHANTED_GOLDEN_APPLE:
                handleEnchantedGoldenApple(event.getEffect(), event.getEntity());
                break;
        }
    }

    private void handlePotionDamage(PotionEffect effect, Entity entity) {

    }

    private void handleMob(PotionEffect effect, Entity entity) {

    }

    private void handleBeacon(PotionEffect effect, Location location) {

    }

    private void handleFood(PotionEffect effect, Entity entity) {

    }

    private void handleGoldenApple(PotionEffect effect, Entity entity) {

    }

    private void handleEnchantedGoldenApple(PotionEffect effect, Entity entity) {

    }
}
