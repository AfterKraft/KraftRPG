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
package com.afterkraft.kraftrpg.compat;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;

/**
 * Author: gabizou
 */
public class RPGHandler extends CraftBukkitHandler{
    @Override
    public double getPostArmorDamage(LivingEntity defender, double damage) {
        return 0;
    }

    @Override
    public void setPlayerExpZero(Player player) {

    }

    @Override
    public boolean damageEntity(LivingEntity target, LivingEntity attacker, double damage, EntityDamageEvent.DamageCause cause, boolean knockback) {
        return false;
    }

    @Override
    public void knockBack(LivingEntity target, LivingEntity attacker, double damage) {

    }

    @Override
    public void refreshLastPlayerDamageTime(LivingEntity entity) {

    }

    @Override
    public void sendFakePotionEffectPacket(PotionEffect effect, Player player) {

    }

    @Override
    public void sendFakePotionEffectPackets(Set<PotionEffect> effects, Player player) {

    }

    @Override
    public void removeFakePotionEffectPacket(PotionEffect effect, Player player) {

    }

    @Override
    public void removeFakePotionEffectPackets(Set<PotionEffect> effects, Player player) {

    }

    @Override
    public void bukkit_setArrowDamage(Arrow arrow, double damage) {

    }

    @Override
    protected float getSoundStrength(LivingEntity entity) {
        return 0;
    }

    @Override
    public void playClientEffect(Player player, Location startLocation, String particle, Vector offset, float speed, int count, boolean sendToAll) {

    }
}
