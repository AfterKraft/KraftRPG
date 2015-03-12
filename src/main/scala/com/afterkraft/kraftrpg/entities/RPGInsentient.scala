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
package com.afterkraft.kraftrpg.entities

import java.util

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.effects.{Effect, EffectType}
import com.afterkraft.kraftrpg.api.entity.Insentient
import com.afterkraft.kraftrpg.api.entity.resource.Resource
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper
import com.afterkraft.kraftrpg.api.skills.Skill
import com.afterkraft.kraftrpg.api.util.FixedPoint
import com.afterkraft.kraftrpg.conversions.OptionalConversions.{Opt, ExOpt}
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.item.inventory.{Inventory, ItemStack}
import org.spongepowered.api.potion.{PotionEffect, PotionEffectType}

class RpgInsentient(private val kraftRPGPlugin: KraftRPGPlugin,
                    private var entity: Living,
                    private var name: String) extends RpgBeing(kraftRPGPlugin,
                                                               entity,
                                                               name) with Insentient {

  override def getResource[T <: Resource](clazz: Class[T]): Opt[T] = ???

  override def getEntity: ExOpt[Living] = super.getEntity.asInstanceOf[ExOpt[Living]]

  override def addEffect(effect: Effect) = ???

  override def getDamageWrapper: Opt[DamageWrapper] = ???

  override def setDamageWrapper(wrapper: DamageWrapper) = ???

  override def manualClearEffects() = ???

  override def addPotionEffect(potion: PotionEffect) = ???

  override def canEquipItem(itemStack: ItemStack): Boolean = ???

  override def getHealth: Double = ???

  override def setHealth(health: Double) = ???

  override def getItemInHand: Opt[ItemStack] = ???

  override def removeEffect(effect: Effect) = ???

  override def getMaxHealth: Double = ???

  override def hasEffectType(`type`: EffectType): Boolean = ???

  override def removeMaxHealth(key: String): Boolean = ???

  override def getArmor: Array[ItemStack] = ???

  override def recalculateMaxHealth(): Double = ???

  override def getInventory: Inventory = ???

  override def updateInventory() = ???

  override def clearEffects() = ???

  override def getNoDamageTicks: Int = ???

  override def hasPotionEffect(`type`: PotionEffectType): Boolean = ???

  override def heal(amount: Double) = ???

  override def getRewardExperience: FixedPoint = ???

  override def setRewardExperience(experience: FixedPoint) = ???

  override def isIgnoringSkill(skill: Skill): Boolean = ???

  override def getEffects: util.Set[Effect] = ???

  override def clearHealthBonuses() = ???

  override def setArmor(item: ItemStack, armorSlot: Int) = ???

  override def modifyStamina(staminaDiff: Int) = ???

  override def removePotionEffect(`type`: PotionEffectType) = ???

  override def getStamina: Int = ???

  override def setStamina(stamina: Int) = ???

  override def getMaxStamina: Int = ???

  override def hasEffect(name: String): Boolean = ???

  override def getMana: Int = ???

  override def setMana(mana: Int) = ???

  override def manualRemoveEffect(effect: Effect) = ???

  override def addMaxHealth(key: String, value: Double): Boolean = ???

  override def sendMessage(message: String) = ???

  override def sendMessage(message: String, args: AnyRef*) = ???

  override def getEffect(name: String): Opt[Effect] = ???

  override def isDead: Boolean = ???

  override def getMaxMana: Int = ???

  override def setMaxMana(mana: Int) = ???
}
