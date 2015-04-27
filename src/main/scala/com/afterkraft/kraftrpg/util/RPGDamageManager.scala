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

package com.afterkraft.kraftrpg.util

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.entity.Insentient
import com.afterkraft.kraftrpg.api.util.DamageManager
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.entity.EntityType
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.{Enchantment, ItemType}
import org.spongepowered.api.world.Location

/**
 * Standard Implementation of DamageManager
 */
class RpgDamageManager(val plugin: KraftRPGPlugin) extends DamageManager {

  def getHighestItemDamage(attacker: Insentient, defender: Insentient, defaultDamage: Double): Double = 4 // TODO

  def getHighestProjectileDamage(champion: Insentient, `type`: DamageManager.ProjectileType): Double = 4 // TODO

  def getDefaultItemDamage(`type`: ItemType, damage: Double): Double = 4 // TODO

  def getDefaultItemDamage(`type`: ItemType): Double = 4 // TODO

  def setDefaultItemDamage(`type`: ItemType, damage: Double) {
  }

  def doesItemDamageVary(`type`: ItemType): Boolean = {
    false
  }

  def setItemDamageVarying(`type`: ItemType, isVarying: Boolean) {
  }

  def getEntityDamage(`type`: EntityType): Double = 4 // TODO

  def getEnvironmentalDamage(cause: Cause): Double = 4 // TODO

  def getEnchantmentDamage(enchantment: Enchantment, enchantmentLevel: Int): Double = 4 // TODO

  def getItemEnchantmentDamage(being: Insentient, enchantment: Enchantment, item: ItemStack): Double = 4 // TODO

  def getFallReduction(being: Insentient): Double = 4 // TODO

  def getModifiedEntityDamage(monster: Insentient, location: Location, baseDamage: Double, fromSpawner: Cause): Double = {
    4
  }

  def getDefaultEntityHealth(entity: Living): Double = 20 // TODO

  def getModifiedEntityHealth(entity: Living): Double = 0 // TODO

  def doesEntityDealVaryingDamage(`type`: EntityType): Boolean = false // TODO

  def setEntityToDealVaryingDamage(`type`: EntityType, dealsVaryingDamage: Boolean) {
    // TODO
  }

  def isStandardWeapon(material: ItemType): Boolean = false // TODO

  def load(config: ConfigurationNode) {
  }

  def initialize() {
  }

  def shutdown() {
  }
}