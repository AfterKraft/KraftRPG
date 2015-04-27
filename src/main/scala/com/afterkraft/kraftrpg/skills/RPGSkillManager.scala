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
import com.afterkraft.kraftrpg.api.entity.SkillCaster
import com.afterkraft.kraftrpg.api.skills._
import com.afterkraft.kraftrpg.api.{ExternalProviderRegistration, RpgCommon}
import com.afterkraft.kraftrpg.conversions.OptionalConversions._
import com.google.common.base.Optional
import com.google.common.base.Preconditions.{checkArgument, checkNotNull}
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.data.{DataQuery, DataView}

import scala.collection.immutable.HashSet
import scala.util.control.Breaks._

/**
 * Default implementation of SkillManager for KraftRPG.
 */
object RPGSkillManager {

  private val DEFAULT_ALLOWED_NODES: Set[DataQuery] = {
    try {

      var set = new HashSet[DataQuery]
      import scala.collection.JavaConversions._
      for (setting <- SkillSetting.AUTOMATIC_SETTINGS) {
        set += setting.node()
        if (setting.scalingNode.isPresent) {
          set += setting.scalingNode.get
        }
      }
      set
    } catch {
      case e: Exception =>
        new HashSet[DataQuery]
    }
  }
}

class RPGSkillManager(private final val plugin: KraftRPGPlugin) extends SkillManager {
  private val mutable = new collection.mutable.HashMap[String, Skill]()
  private val permissive = new collection.mutable.HashMap[String, Permissible]();
  private val passiveTemp = new collection.mutable.HashMap[String, Passive]();

  import scala.collection.JavaConversions._
  for (skill <- ExternalProviderRegistration.getRegisteredSkills) {
    def addInitSkill(skill: Skill): Unit = {
      checkNotNull(skill, "Cannot add a null skill!", "")
      if (checkSkillConfig(skill)) {
        skill match {
          case permissible: Permissible =>
            permissive += ((skill.getName.toLowerCase, permissible)); return
          case passive: Passive =>
            passiveTemp += ((passive.getName.toLowerCase, passive)); return
          case _ =>
            mutable += ((skill.getName.toLowerCase, skill)); return
        }
      }
    }
    addInitSkill(skill)
  }
  private final val passiveMap = Map.newBuilder.++=(passiveTemp).result()
  private final val permissiveMap = Map.newBuilder.++=(permissive).result()
  private final val skillMap = Map.newBuilder.++=(mutable).result()

  private val listener = new SkillManagerListener


  /**
   * Load all the skills.
   */
  def initialize() {

    RpgCommon.getGame.getEventManager.register(this.plugin, listener)
  }

  def addSkill(skill: Skill) {
  }

  def shutdown() {
  }

  def hasSkill(skillName: String): Boolean = {
    checkNotNull(skillName, "Cannot check a null skill name!", "")
    checkArgument(!skillName.isEmpty, "Cannot check an empty skill name!", "")
    false
  }

  def getSkill(name: String): Opt[Skill] = {
    checkNotNull(name, "Cannot get a null skill name!", "")
    this.skillMap.get(name.toLowerCase)
  }

  def loadPermissionSkill(name: String): Boolean = { // TODO Reorder this somewhere else before this is instantiated
    checkNotNull(name, "Cannot load a null permission skill name!", "")
    if ((name == null) || (this.skillMap.get(name.toLowerCase) != null)) {
      return true
    }
    val oSkill: PermissionSkill = new PermissionSkill(this.plugin, name)
    true
  }

  def getSkills = {
    import scala.collection.JavaConversions._
    this.skillMap.values
  }

  def isLoaded(name: String): Boolean = {
    checkNotNull(name, "Cannot check if a null skill name is loaded!", "")
    this.skillMap.contains(name.toLowerCase)
  }

  def removeSkill(skill: Skill) {
    checkNotNull(skill, "Cannot remove a null skill!", "")
  }

  def getDelayedSkill(caster: SkillCaster): Optional[Stalled] = {
    checkNotNull(caster, "Cannot get the stalled skill of a null caster!", "")
    Optional.absent[Stalled] // TODO
  }

  def setCompletedSkill(caster: SkillCaster) {
    checkNotNull(caster, "Cannot set a null caster to complete a skill!", "")
  }

  def addSkillTarget(o: Entity, caster: SkillCaster, skill: Skill) {
    checkNotNull(o, "Cannot add a null entity as a skill target!", "")
    checkNotNull(caster, "Cannot add a null caster!", "")
    checkNotNull(skill, "Cannot add a skill target to a null skill!", "")
    Optional.absent[Stalled] // TODO

  }

  def getSkillTargetInfo(o: Entity): Optional[SkillUseObject] = {
    checkNotNull(o, "Cannot get the skill target info on a null entity!", "")
    Optional.absent[SkillUseObject] // TODO
  }

  def isSkillTarget(o: Entity): Boolean = {
    checkNotNull(o, "Cannot check a null skill target!", "")
    false // TODO
  }

  def removeSkillTarget(entity: Entity, caster: SkillCaster, skill: Skill) {
    checkNotNull(entity, "Cannot remove a null entity skill target!", "")
    checkNotNull(caster, "Cannot remove a null caster skill target!", "")
    checkNotNull(skill, "Cannot remove a null skill from a skill target!", "")
    // TODO
  }

  private def checkSkillConfig(skill: Skill): Boolean = {
    if (skill.getUsedConfigNodes == null || skill.getUsedConfigNodes.isEmpty) {
      return true
    }
    import scala.collection.JavaConversions._
    val settings: Iterable[SkillSetting] = skill.getUsedConfigNodes
    val section: DataView = skill.getDefaultConfig
    def setupKeys(skill: Skill) = {
      val builder = Iterable.newBuilder[DataQuery]
      for (setting <- settings) {
        breakable({
                    setting match {
                      case SkillSetting.CUSTOM_PER_CASTER =>
                        break()
                      case SkillSetting.CUSTOM =>
                        break()
                      case _ => setting.scalingNode().isPresent match {
                        case true => builder += setting.node(); builder += setting.scalingNode().get()
                        case false => builder += setting.node()
                      }
                    }
                  })
      }
      builder.result()
    }
    val allowedKeys: Iterable[DataQuery] = setupKeys(skill)
    for (configKey <- section.getKeys(false)) {
      if (!allowedKeys.contains(configKey)) {
        this.plugin.getLogger.error("Error in skill " + skill.getName + ":")
        this.plugin.getLogger.error("  Extra default configuration " + "value " + configKey + " not declared in " + "getUsedConfigNodes()")
        return false
      }
    }
    true
  }

  private class SkillManagerListener {

  }

}