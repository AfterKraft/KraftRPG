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

package com.afterkraft.kraftrpg.skills

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.RPGPlugin
import com.afterkraft.kraftrpg.api.entity.SkillCaster
import com.afterkraft.kraftrpg.api.roles.ExperienceType
import com.afterkraft.kraftrpg.api.skills.{Active, SkillCastResult, SkillConfigManager, SkillSetting, SkillType}
import com.afterkraft.kraftrpg.api.util.FixedPoint
import com.afterkraft.kraftrpg.common.skills.StalledSkill
import com.afterkraft.kraftrpg.conversions.OptionalConversions.asOptionalConverter
import org.spongepowered.api.item.inventory.ItemStack

/**
 *
 */
object ActiveSkillRunner {

  def castSkillInitial(caster: SkillCaster, skill: Active, args: Array[String]): SkillCastResult = {
//    if (caster.isDead) {
//      return SkillCastResult.DEAD
//    }
    if (!caster.canUseSkill(skill)) {
      return SkillCastResult.NOT_AVAILABLE
    }
    if (caster.getStalledSkill != null) {
      var stalled: Active = caster.getStalledSkill.getActiveSkill
      if (skill == stalled) {
        if (caster.getStalledSkill.isReady) {
          return castSkillPart2(caster, skill, args)
        }
        else {
          if (caster.cancelStalledSkill(false)) {
            return SkillCastResult.EVENT_CANCELLED
          }
          else {
            return SkillCastResult.FAIL
          }
        }
      }
      if (skill.isType(SkillType.SELF_INTERRUPTING)) {
        if (stalled.isType(SkillType.UNINTERRUPTIBLE)) {
          return SkillCastResult.STALLING_FAILURE
        }
        else {
          if (!caster.cancelStalledSkill(false)) {
            return SkillCastResult.STALLING_FAILURE
          }
          else {
            stalled = null
          }
        }
      }
      if (stalled != null && stalled.isType(SkillType.EXCLUSIVE)) {
        return SkillCastResult.STALLING_FAILURE
      }
    }
    val now: Long = System.currentTimeMillis
    if (caster.getGlobalCooldown > now) {
      return SkillCastResult.ON_GLOBAL_COOLDOWN
    }

    val cooldown = caster.getCooldown(skill.getName)
    if (cooldown.isDefined && cooldown.get > now) {
      return SkillCastResult.ON_COOLDOWN
    }
    val plugin: RPGPlugin = KraftRPGPlugin.getInstance
    if (plugin.getSkillConfigManager.isSettingConfigured(skill, SkillSetting.DELAY)) {
      val delay: Double = plugin.getSkillConfigManager.getUsedDoubleSetting(caster, skill, SkillSetting.DELAY)
      if (delay > 0) {
        if (caster.getStalledSkill != null) {
          return SkillCastResult.STALLING_FAILURE
        }
        val stalled: StalledSkill = new StalledSkill(skill, args, caster, delay.toLong)
        caster.setStalledSkill(stalled)
        return SkillCastResult.START_DELAY
      }
    }
    castSkillPart2(caster, skill, args)
  }

  private def castSkillPart2(caster: SkillCaster, skill: Active, args: Array[String]): SkillCastResult = {
    val plugin: KraftRPGPlugin = KraftRPGPlugin.getInstance
    val confman: SkillConfigManager = plugin.getSkillConfigManager
    var healthCost: Double = 0
    var manaCost: Double = 0
    var hungerCost: Double = 0
    var reagent: ItemStack = null
    var reagentQuantity: Int = 0
    if (confman.isSettingConfigured(skill, SkillSetting.HEALTH_COST)) {
      healthCost = confman.getUsedDoubleSetting(caster, skill, SkillSetting.HEALTH_COST)
    }
    if (confman.isSettingConfigured(skill, SkillSetting.MANA_COST)) {
      manaCost = confman.getUsedDoubleSetting(caster, skill, SkillSetting.MANA_COST)
    }
    if (confman.isSettingConfigured(skill, SkillSetting.STAMINA_COST)) {
      hungerCost = confman.getUsedDoubleSetting(caster, skill, SkillSetting.STAMINA_COST)
    }
    if (confman.isSettingConfigured(skill, SkillSetting.REAGENT)) {
      reagent = confman.getUsedItemStackSetting(caster, skill, SkillSetting.REAGENT)
    }
    if (confman.isSettingConfigured(skill, SkillSetting.REAGENT_QUANTITY)) {
      reagentQuantity = confman.getUsedIntSetting(caster, skill, SkillSetting.REAGENT_QUANTITY)
    }
    if (reagentQuantity != -1 && reagent != null) {
      reagent.setQuantity(reagentQuantity)
    }
//    if (caster.getHealth < healthCost) {
//      return SkillCastResult.LOW_HEALTH
//    }
//    if (caster.getMana < manaCost) {
//      return SkillCastResult.LOW_MANA
//    }
    var result: SkillCastResult = null
    try {
      try {
        if (!skill.parse(caster, args)) {
          return SkillCastResult.SYNTAX_ERROR
        }
      }
      catch {
        case t: Throwable =>
          plugin.getLogger.error("parsing arguments", t, caster, args)
          t.printStackTrace()
          return SkillCastResult.FAIL
      }
      try {
        result = skill.checkCustomRestrictions(caster, false)
      }
      catch {
        case t: Throwable =>
          plugin.getLogger.error("checking restrictions", t, caster, args)
          t.printStackTrace()
          return SkillCastResult.FAIL
      }
      if (result eq SkillCastResult.ON_WARMUP) {
        skill.onWarmUp(caster)
        return result
      }
      else if (result ne SkillCastResult.NORMAL) {
        return result
      }
      result = null
      try {
        result = skill.useSkill(caster)
      }
      catch {
        case t: Throwable =>
          plugin.getLogger.error("using skill", t, caster, args)
      }
    } finally {
      try skill.cleanState(caster)
      catch {
        case t: Throwable =>
          plugin.getLogger.error("cleaning skill state", t, caster, args)
      }
    }
    if (result == null) {
      result = SkillCastResult.FAIL
    }
    if (result eq SkillCastResult.NORMAL) {
//      caster.setHealth(caster.getHealth - healthCost)
//      caster.setMana(caster.getMana - manaCost.toInt)
//      caster.modifyStamina(-hungerCost.toInt)
      val exp: Double = plugin.getSkillConfigManager.getUsedDoubleSetting(caster, skill, SkillSetting.EXP_ON_CAST)
      if (exp > 0) {
        if (caster.canGainExperience(ExperienceType.SKILL)) {
          caster.gainExperience(FixedPoint.valueOf(exp), ExperienceType.SKILL, caster.getLocation)
        }
      }
      val now: Long = System.currentTimeMillis
      val globalCD: Long = plugin.getProperties.getDefaultGlobalCooldown
      val cooldown: Long = confman.getUsedIntSetting(caster, skill, SkillSetting.COOLDOWN)
      caster.setGlobalCooldown(now + globalCD)
      caster.setCooldown(skill.getName, now + cooldown)
    }
    result
  }
}

final class ActiveSkillRunner private() {

}