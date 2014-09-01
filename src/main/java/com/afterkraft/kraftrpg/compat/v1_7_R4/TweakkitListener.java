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
