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

import java.io.File

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.RpgCommon
import com.afterkraft.kraftrpg.api.util.{FixedPoint, Properties}
import com.afterkraft.kraftrpg.conversions.OptionalConversions.Opt
import com.google.common.base.Optional
import com.google.common.base.Preconditions.checkNotNull
import ninja.leaping.configurate.commented.{CommentedConfigurationNode, SimpleCommentedConfigurationNode}
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import ninja.leaping.configurate.objectmapping.{ObjectMapper, Setting}
import org.spongepowered.api.entity.EntityType
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.item.ItemType
import org.spongepowered.api.world.Location

import scala.collection.mutable

/**
 * Default implementation of Properties
 */
object RPGPluginProperties {

  final val STORAGE = "storage"
  final val MOBS_ALLOW_SPAWN_CAMPING = "spawn-camping"
  final val MOBS_SPAWN_CAMPING_MULTIPLIER = "spawn-camping-multiplier"
  final val MOBS_DAMAGE_DISTANCE = "spawn-distance-damage"
  final val MOBS_DAMAGE_DISTANCE_MODIFIER = "spawn-distance-damage-modifier"
  final val MOBS_HEALTH_DISTANCE = "spawn-distance-health"
  final val MOBS_HEALTH_DISTANCE_MODIFIER = "spawn-distance-health-modifier"
  final val MOBS_EXP_DISTANCE = "spawn-distance-experience"
  final val MOBS_EXP_DISTANCE_MODIFIER = "spawn-distance-experience-modifier"
  final val MOBS_DISTANCE = "spawn-distance-mod"
  final val GLOBAL_COOLDOWN = "global-cooldown"
  final val ALLOW_SPAWN_CAMPING = "allow-spawn-camping"
  final val SPAWN_CAMPING_MODIFIER = "spawn-camping-modifier"
  private val instance = new RPGPluginProperties

  /**
   * Get the instance of this properties
   *
   * @return The instance of the plugin properties
   */
  def getInstance: RPGPluginProperties = {
    instance
  }
}

class RPGPluginProperties private[util]() extends Properties {

  var creatureExperienceDrop: mutable.Map[EntityType, FixedPoint] = mutable.Map()
  private var root: CommentedConfigurationNode = SimpleCommentedConfigurationNode.root

  /**
   * Initializes the properties file
   */
  def initialize(): Unit = {
    val plugin: KraftRPGPlugin = RpgCommon.getPlugin.asInstanceOf[KraftRPGPlugin]
    checkNotNull(plugin)
    val manager: RPGConfigManager = plugin.getConfigurationManager
    checkNotNull(manager)
    val directory: File = manager.getConfigDirectory
    val mainConfig: File = new File(directory + File.separator + "main.hocon")
    try {
      if (!mainConfig.getParentFile.exists) {
        mainConfig.getParentFile.mkdirs
      }
      var loader: HoconConfigurationLoader = null
      if (!mainConfig.exists) {
        mainConfig.createNewFile
        loader = HoconConfigurationLoader.builder.setFile(mainConfig).build
        val configBase: ConfigBase = new GlobalConfig
        val configMapper = ObjectMapper.forObject(configBase)
        configMapper.serialize(this.root.getNode("main"))
        loader.save(this.root)
        this.root = loader.load
        KraftRPGPlugin.getInstance.getLogger.info(this.root.getString)
        KraftRPGPlugin.getInstance.getLogger.info(this.root.getNode("main").getNode("skills").getString)
      }
      else {
        loader = HoconConfigurationLoader.builder.setFile(mainConfig).build
        this.root = loader.load
        KraftRPGPlugin.getInstance.getLogger.info(this.root.getString)
        KraftRPGPlugin.getInstance.getLogger.info(this.root.getNode("main").getNode("skills").getString)
      }
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def getMobDamageDistanceModified: Double = {
    this.root.getNode("main").getNode("entity").getNode(RPGPluginProperties.MOBS_DAMAGE_DISTANCE_MODIFIER).getDouble
  }

  def getMobHealthDistanceModified: Double = {
    this.root.getNode("main").getNode("entity").getNode(RPGPluginProperties.MOBS_HEALTH_DISTANCE_MODIFIER).getDouble
  }

  def getMobExpDistanceModified: Double = {
    this.root.getNode("main").getNode("entity").getNode(RPGPluginProperties.MOBS_EXP_DISTANCE_MODIFIER).getDouble
  }

  def getDistanceTierModifier: Double = {
    this.root.getNode("main").getNode("entity").getNode(RPGPluginProperties.MOBS_DISTANCE).getDouble
  }

  def isMobHealthDistanceModified: Boolean = {
    this.root.getNode("main").getNode("entity").getNode(RPGPluginProperties.MOBS_DAMAGE_DISTANCE).getBoolean
  }

  def isMobExpDistanceModified: Boolean = {
    this.root.getNode("main").getNode("entity").getNode(RPGPluginProperties.MOBS_EXP_DISTANCE).getBoolean
  }

  def getDefaultGlobalCooldown: Int = {
    this.root.getNode("main").getNode("skills").getNode(RPGPluginProperties.GLOBAL_COOLDOWN).getInt
  }

  def isVaryingDamageEnabled: Boolean = false // TODO

  def isStarvingDamageEnabled: Boolean = false // TODO

  def getCombatTime: Int = 0 // TODO

  def getDefaultMaxStamina: Int = 0 // TODO

  def getDefaultStaminaRegeneration: Int = 0 // TODO

  def getStaminaIncreaseForFood(foodMaterial: ItemType): Int = 0 // TODO

  def getFoodHealPercent: Int = 0 // TODO

  def getFoodHealthPerTier: Int = 0 // TODO

  def getCombatPeriod: Long = 0 // TODO

  def getMonsterExperience(entity: Living, spawnPoint: Location): FixedPoint = {
    if (this.isMobDamageDistanceModified) {
      val expOpt = this.creatureExperienceDrop.get(entity.getType)
      if (!expOpt.isDefined) new FixedPoint()
      else {
        val exp = expOpt.get
        var value = exp.doubleValue().ceil
        val percent = 1 + this.getMobDamageDistanceModified / this.getDistanceTierModifier
        val modifier = Math.pow(percent, MathUtil.getModulatedDistance(entity.getLocation, spawnPoint) / this.getDistanceTierModifier)
        value = Math.ceil(value * modifier)
        FixedPoint.valueOf(value)
      }
    }
    FixedPoint.valueOf(0)
  }

  def getExperienceLossMultiplier: Double = 0 // TODO

  def getExperienceLossMultiplierForPVP: Double = 0 // TODO

  def getPlayerKillingExperience: FixedPoint = new FixedPoint // TODO

  def hasEntityRewardType(`type`: EntityType): Boolean = true // TODO

  def getEntityReward(`type`: EntityType): Opt[FixedPoint] = Optional.absent[FixedPoint] // TODO Search on implicit conversion and assignment

  def allowSpawnCamping: Boolean = this.root.getNode("main").getNode("combat").getNode(RPGPluginProperties.ALLOW_SPAWN_CAMPING).getBoolean

  def getSpawnCampingMultiplier: Double = this.root.getNode("main").getNode("combat").getNode(RPGPluginProperties.SPAWN_CAMPING_MODIFIER).getDouble

  def isMobDamageDistanceModified: Boolean = this.root.getNode("main").getNode("entity").getNode(RPGPluginProperties.MOBS_DAMAGE_DISTANCE).getBoolean

  def getStorageType: String = this.root.getNode("main").getNode("storage").getNode(RPGPluginProperties.STORAGE).getString.toLowerCase

  private class GlobalConfig extends ConfigBase {
  }

  private class ConfigBase {

    @Setting
    val entity = new EntityCategory

    @Setting
    val storage = new StorageCategory

    @Setting
    val combat = new CombatCategory

    @Setting
    val skills = new SkillCategory

    @ConfigSerializable
    class EntityCategory extends Category {

      @Setting(value = RPGPluginProperties.MOBS_DAMAGE_DISTANCE,
               comment = "Whether mob damage based on distance from spawn is modified")
      val isMobDamageDistanceModified = false

      @Setting(value = RPGPluginProperties.MOBS_HEALTH_DISTANCE,
               comment = "Whether mob health is modified based on the distance from spawn")
      val isMobHealthDistanceModified = false

      @Setting(value = RPGPluginProperties.MOBS_EXP_DISTANCE,
               comment = "Whether mob experience is multiplied by a factor of distance from spawn")
      val isMobExpDistanceModified = false

      @Setting(value = RPGPluginProperties.MOBS_DAMAGE_DISTANCE_MODIFIER,
               comment = "How much mob damage is multiplied by based on distance")
      val mobDamageDistanceModified = 0D

      @Setting(value = RPGPluginProperties.MOBS_HEALTH_DISTANCE_MODIFIER, comment = "How much mob health is multiplied by based on distance")
      val mobHealthDistanceModified = 0D

      @Setting(value = RPGPluginProperties.MOBS_EXP_DISTANCE_MODIFIER, comment = "How much mob experience is multiplied by based on distance")
      val mobExpDistanceModified = 0D

      @Setting(value = RPGPluginProperties.MOBS_DISTANCE, comment = "The distance between mob modifier jumps")
      val mobdistanceTier = 100.0D
    }

    @ConfigSerializable
    class CombatCategory extends Category {

      @Setting(value = RPGPluginProperties.ALLOW_SPAWN_CAMPING,
               comment = "Whether spawn camping spawners is allowed or not.")
      val spawnCamping = false

      @Setting(value = RPGPluginProperties.SPAWN_CAMPING_MODIFIER,
               comment = "How much experience is reduced by for spawn camped mobs")
      val campingMultiplier = 0.1D
    }

    @ConfigSerializable
    class SkillCategory extends Category {

      @Setting(value = RPGPluginProperties.GLOBAL_COOLDOWN,
               comment = "The global cooldown between casting skills. Measured in milliseconds.")
      val globalCooldown: Int = 10
    }

    @ConfigSerializable
    class StorageCategory extends Category {

      @Setting(value = RPGPluginProperties.STORAGE,
               comment = "The storage manage for player data")
      val storage: String = "hocon"
    }

    @ConfigSerializable
    class Category {
    }

  }

}
