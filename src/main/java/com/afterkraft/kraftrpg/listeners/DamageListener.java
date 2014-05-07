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

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.listeners.AbstractListener;
import com.afterkraft.kraftrpg.api.util.DamageManager;

public class DamageListener extends AbstractListener {

    protected DamageListener(RPGPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageEvent(EntityDamageEvent event) {

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
