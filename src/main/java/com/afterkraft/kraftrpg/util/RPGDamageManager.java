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
package com.afterkraft.kraftrpg.util;

import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import com.typesafe.config.Config;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.common.DamageCause;

/**
 * Standard Implementation of DamageManager
 */
public class RPGDamageManager implements DamageManager {

    public RPGDamageManager(RPGPlugin plugin) {
    }

    @Override
    public double getHighestItemDamage(Insentient attacker, Insentient defender,
            double defaultDamage) {
        return 4;
    }

    @Override
    public double getHighestProjectileDamage(Insentient champion,
            ProjectileType type) {
        return 4; // TODO
    }

    @Override
    public double getDefaultItemDamage(ItemType type, double damage) {
        return 4; // TODO
    }

    @Override
    public double getDefaultItemDamage(ItemType type) {
        return 4; // TODO
    }

    @Override
    public void setDefaultItemDamage(ItemType type, double damage) {

    }

    @Override
    public boolean doesItemDamageVary(ItemType type) {
        return false;
    }

    @Override
    public void setItemDamageVarying(ItemType type, boolean isVarying) {

    }

    @Override
    public double getEntityDamage(EntityType type) {
        return 4; // TODO
    }

    @Override
    public double getEnvironmentalDamage(DamageCause cause) {
        return 4; // TODO
    }

    @Override
    public double getEnchantmentDamage(Enchantment enchantment,
            int enchantmentLevel) {
        return 4; // TODO
    }

    @Override
    public double getItemEnchantmentDamage(Insentient being,
            Enchantment enchantment,
            ItemStack item) {
        return 4; // TODO
    }

    @Override
    public double getFallReduction(Insentient being) {
        return 4; // TODO
    }

    @Override
    public double getModifiedEntityDamage(Monster monster, Location location,
            double baseDamage,
            Cause fromSpawner) {
        return 4; // TODO
    }


    @Override
    public double getDefaultEntityHealth(Living entity) {
        return 20;
    }

    @Override
    public double getModifiedEntityHealth(Living entity) {
        return 0;
    }

    @Override
    public boolean doesEntityDealVaryingDamage(EntityType type) {
        return false;
    }

    @Override
    public void setEntityToDealVaryingDamage(EntityType type,
            boolean dealsVaryingDamage) {

    }

    @Override
    public boolean isStandardWeapon(ItemType material) {
        return false;
    }

    @Override
    public void load(Config config) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}
