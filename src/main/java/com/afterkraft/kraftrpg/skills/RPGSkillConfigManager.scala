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

import java.io.File

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.RpgCommon
import com.afterkraft.kraftrpg.api.entity.SkillCaster
import com.afterkraft.kraftrpg.api.roles.Role
import com.afterkraft.kraftrpg.api.roles.aspects.SkillAspect
import com.afterkraft.kraftrpg.api.skills.{Skill, SkillConfigManager, SkillSetting}
import com.afterkraft.kraftrpg.common.skills.common.PermissionSkill
import com.afterkraft.kraftrpg.util.MathUtil
import com.google.common.base.Optional
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.{Maps, Sets}
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.service.persistence.data.{DataContainer, DataQuery, DataView}

import scala.collection.mutable

/**
 *
 */
class RPGSkillConfigManager extends SkillConfigManager {

  private final val customSettings: mutable.Map[SkillCaster, Map[Skill, ConfigurationNode]] = null
  protected var outsourcedSkillConfig: ConfigurationNode = null
  protected var standardSkillConfig: ConfigurationNode = null
  protected var defaultSkillConfig: ConfigurationNode = null
  private var roleSkillConfigurations: Map[String, ConfigurationNode] = null
  private var skillConfigFile: File = null
  private var outsourcedSkillConfigFile: File = null
  private var plugin: KraftRPGPlugin = null

  def this(plugin: KraftRPGPlugin) {
    this()
    this.plugin = plugin
    this.customSettings = Maps.newHashMap
  }

  def reload() {
    this.standardSkillConfig = null
    this.outsourcedSkillConfig = null
    this.initialize
  }

  def initialize() {
  }

  def saveSkillConfig() {
  }

  def getRoleSkillConfig(name: String): DataContainer = {
    null
  }

  def addRoleSkillSettings(roleName: String, skillName: String, section: DataView) {
  }

  def loadSkillDefaults(skill: Skill) {
    if (skill.isInstanceOf[PermissionSkill]) {
      return
    }
  }

  def addTemporarySkillConfigurations(skill: Skill, caster: SkillCaster, section: DataView) {
  }

  def clearTemporarySkillConfigurations(caster: SkillCaster) {
    checkNotNull(caster, "Cannot remove a null caster's custom " + "configurations!")
    this.customSettings.remove(caster)
  }

  def clearTemporarySkillConfigurations(caster: SkillCaster, skill: Skill) {
    checkNotNull(caster, "Cannot clear configurations of a null caster!")
    checkNotNull(skill, "Cannot clear configurations of a null " + "skill!")
  }

  def getRawString(skill: Skill, setting: SkillSetting): String = {
    getRawString(skill, setting.node)
  }

  def getRawString(skill: Skill, setting: DataQuery): String = {
    checkNotNull(skill, "Cannot check the config of a null skill!")
    checkNotNull(setting, "Cannot check the config with a null path!")
    if (outsourcedSkillConfig.getNode(skill.getName + "." + setting).isVirtual) {
      throw new IllegalStateException("The requested skill setting, " + setting + " was not defaulted by the skill: " + skill.getName)
    }
    outsourcedSkillConfig.getString(skill.getName + "." + setting)
  }

  def getRawBoolean(skill: Skill, setting: SkillSetting): Boolean = false // TODO

  def getRawBoolean(skill: Skill, setting: DataQuery): Boolean = false // TODO

  def getRawKeys(skill: Skill, setting: DataQuery): Set[DataQuery] = Set() // TODO

  def getRawSetting(skill: Skill, setting: SkillSetting): AnyRef = {
    check(skill, setting)
    if (!isSettingConfigured(skill, setting)) {
      throw new IllegalStateException("The skill: " + skill.getName + " has no configured defaults for: " + setting.node)
    }
    getRawSetting(skill, setting.node)
  }

  def isSettingConfigured(skill: Skill, setting: SkillSetting): Boolean = {
    checkNotNull(skill, "Cannot check the use configurations for a null skill!")
    checkNotNull(setting, "Cannot check the use configurations for a null setting!")
    skill.getDefaultConfig.contains(setting.node) || !outsourcedSkillConfig.getNode(skill.getName + "." + setting.node).isVirtual
  }

  def getRawSetting(skill: Skill, setting: DataQuery): AnyRef = {
    check(skill, setting)
    if (!isSettingConfigured(skill, setting)) {
      throw new IllegalStateException("The skill: " + skill.getName + " has no configured defaults for: " + setting)
    }
    outsourcedSkillConfig.getNode(skill.getName + "." + setting).getValue
  }

  def isSettingConfigured(skill: Skill, setting: DataQuery): Boolean = {
    checkNotNull(skill, "Cannot check the use configurations for a null skill!")
    checkNotNull(setting, "Cannot check the use configurations for a null setting!")
    skill.getDefaultConfig.contains(setting) || !outsourcedSkillConfig.getNode(skill.getName + "." + setting).isVirtual
  }

  private def check(skill: Skill, setting: DataQuery) {
    checkNotNull(skill, "Cannot get a setting for a null skill!")
    checkNotNull(setting, "Cannot get a setting for a null path!")
  }

  private def check(skill: Skill, setting: SkillSetting) {
    checkNotNull(skill, "Cannot get a setting for a null skill!")
    checkNotNull(setting, "Cannot get a setting for a null path!")
  }

  def getRawIntSetting(skill: Skill, setting: SkillSetting): Int = {
    check(skill, setting)
    getRawIntSetting(skill, setting.node)
  }

  def getRawIntSetting(skill: Skill, setting: DataQuery): Int = {
    check(skill, setting)
    val `val`: AnyRef = getRawSetting(skill, setting)
    if (`val` == null) {
      throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName + " and setting: " + setting)
    }
    else {
      val i: Integer = MathUtil.asInt(`val`)
      if (i == null) {
        throw new IllegalStateException("The configured setting is not an integer!")
      }
      i
    }
  }

  def getRawDoubleSetting(skill: Skill, setting: SkillSetting): Double = {
    check(skill, setting)
    getRawDoubleSetting(skill, setting.node)
  }

  def getRawDoubleSetting(skill: Skill, setting: DataQuery): Double = {
    check(skill, setting)
    val `val`: AnyRef = getRawSetting(skill, setting)
    if (`val` == null) {
      throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName + " and setting: " + setting)
    }
    else {
      val i: Double = MathUtil.asDouble(`val`)
      if (i == null) {
        throw new IllegalStateException("The configured setting is not an integer!")
      }
      i
    }
  }

  def getRawStringSetting(skill: Skill, setting: SkillSetting): String = {
    check(skill, setting)
    getRawStringSetting(skill, setting.node)
  }

  def getRawStringSetting(skill: Skill, setting: DataQuery): String = {
    check(skill, setting)
    val `val`: AnyRef = getRawSetting(skill, setting)
    if (`val` == null) {
      throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName + " and setting: " + setting)
    }
    `val`.toString
  }

  def getRawBooleanSetting(skill: Skill, setting: SkillSetting): Boolean = {
    check(skill, setting)
    getRawBooleanSetting(skill, setting.node)
  }

  def getRawBooleanSetting(skill: Skill, setting: DataQuery): Boolean = {
    check(skill, setting)
    val `val`: AnyRef = getRawSetting(skill, setting)
    if (`val` == null) {
      throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName + " and setting: " + setting)
    }
    else {
      `val`.asInstanceOf[Boolean]
    }
  }

  def getRawStringListSetting(skill: Skill, setting: SkillSetting): List[String] = {
    check(skill, setting)
    getRawStringListSetting(skill, setting.node)
  }

  @SuppressWarnings(Array("unchecked")) def getRawStringListSetting(skill: Skill, setting: DataQuery): List[String] = {
    check(skill, setting)
    val `val`: AnyRef = getRawSetting(skill, setting)
    if (`val` == null || !`val`.isInstanceOf[List[_]]) {
      throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName + " and setting: " + setting)
    }
    else {
      `val`.asInstanceOf[List[String]]
    }
  }

  def getRawItemStackSetting(skill: Skill, setting: SkillSetting): ItemStack = {
    check(skill, setting)
    getRawItemStackSetting(skill, setting.node)
  }

  def getRawItemStackSetting(skill: Skill, setting: DataQuery): ItemStack = {
    check(skill, setting)
    val `val`: AnyRef = getRawSetting(skill, setting)
    if (!`val`.isInstanceOf[ItemStack]) {
      throw new IllegalStateException("There was an issue getting the setting for skill: " + skill.getName + " and setting: " + setting)
    }
    RpgCommon.getGame.getRegistry.getItemBuilder.fromItemStack(`val`.asInstanceOf[ItemStack]).build
  }

  def getSetting(role: Role, skill: Skill, setting: SkillSetting): AnyRef = {
    check(role, skill, setting)
    if (!isSettingConfigured(skill, setting)) {
      throw new IllegalStateException("The skill: " + skill.getName + " has no configured defaults for: " + setting.node)
    }
    getSetting(role, skill, setting.node)
  }

  private def check(role: Role, skill: Skill, setting: SkillSetting) {
    checkNotNull(role, "Cannot get a setting for a null role!")
    checkNotNull(skill, "Cannot get a setting for a null skill!")
    checkNotNull(setting, "Cannot get a setting for a null path!")
  }

  def getIntSetting(role: Role, skill: Skill, setting: SkillSetting): Int = {
    check(role, skill, setting)
    getIntSetting(role, skill, setting.node)
  }

  def getIntSetting(role: Role, skill: Skill, setting: DataQuery): Int = {
    check(role, skill, setting)
    val `val`: AnyRef = getSetting(role, skill, setting)
    if (`val` == null) {
      throw new IllegalStateException(
        "There was an issue getting the setting for: " + role.getName + " skill: " + skill.getName + " and setting: " + setting)
    }
    else {
      MathUtil.toInt(`val`)
    }
  }

  def getDoubleSetting(role: Role, skill: Skill, setting: SkillSetting): Double = {
    check(role, skill, setting)
    getDoubleSetting(role, skill, setting.node)
  }

  def getDoubleSetting(role: Role, skill: Skill, setting: DataQuery): Double = {
    check(role, skill, setting)
    val `val`: AnyRef = getSetting(role, skill, setting)
    if (`val` == null) {
      throw new IllegalStateException(
        "There was an issue getting the setting for: " + role.getName + " skill: " + skill.getName + " and setting: " + setting)
    }
    else {
      MathUtil.toDouble(`val`)
    }
  }

  def getStringSetting(role: Role, skill: Skill, setting: SkillSetting): String = {
    check(role, skill, setting)
    getStringSetting(role, skill, setting.node)
  }

  def getStringSetting(role: Role, skill: Skill, setting: DataQuery): String = {
    check(role, skill, setting)
    val `val`: AnyRef = getSetting(role, skill, setting)
    if (`val` == null) {
      throw new IllegalStateException(
        "There was an issue getting the setting for: " + role.getName + " skill: " + skill.getName + " and setting: " + setting)
    }
    `val` match {
      case s: String =>
        s
      case _ =>
        `val`.toString
    }
  }

  def getBooleanSetting(role: Role, skill: Skill, setting: SkillSetting): Boolean = {
    check(role, skill, setting)
    getBooleanSetting(role, skill, setting.node)
  }

  def getBooleanSetting(role: Role, skill: Skill, setting: DataQuery): Boolean = {
    check(role, skill, setting)
    val `val`: AnyRef = getSetting(role, skill, setting)
    if (`val` == null) {
      throw new IllegalStateException(
        "There was an issue getting the setting for: " + role.getName + " skill: " + skill.getName + " and setting: " + setting)
    }
    else {
      `val`.asInstanceOf[Boolean]
    }
  }

  def getStringListSetting(role: Role, skill: Skill, setting: SkillSetting): List[String] = {
    check(role, skill, setting)
    getStringListSetting(role, skill, setting.node)
  }

  @SuppressWarnings(Array("unchecked")) def getStringListSetting(role: Role, skill: Skill, setting: DataQuery): List[String] = {
    check(role, skill, setting)
    val `val`: AnyRef = getSetting(role, skill, setting)
    if (`val` == null || !`val`.isInstanceOf[List[_]]) {
      throw new IllegalStateException(
        "There was an issue getting the setting for: " + role.getName + " skill: " + skill.getName + " and setting: " + setting)
    }
    else {
      `val`.asInstanceOf[List[String]]
    }
  }

  def getItemStackSetting(role: Role, skill: Skill, setting: SkillSetting): ItemStack = {
    check(role, skill, setting)
    getItemStackSetting(role, skill, setting.node)
  }

  def getItemStackSetting(role: Role, skill: Skill, setting: DataQuery): ItemStack = {
    check(role, skill, setting)
    val `val`: AnyRef = getSetting(role, skill, setting)
    if (!`val`.isInstanceOf[ItemStack]) {
      throw new IllegalStateException(
        "There was an issue getting the setting for: " + role.getName + " skill:" + skill.getName + " and setting: " + setting)
    }
    RpgCommon.getGame.getRegistry.getItemBuilder.fromItemStack(`val`.asInstanceOf[ItemStack]).build
  }

  def getLevel(caster: SkillCaster, skill: Skill): Int = {
    getUsedIntSetting(caster, skill, SkillSetting.LEVEL)
  }

  def getUsedIntSetting(caster: SkillCaster, skill: Skill, setting: SkillSetting): Int = {
    check(caster, skill, setting)
    getUsedIntSetting(caster, skill, setting.node)
  }

  def getUsedIntSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): Int = {
    check(caster, skill, setting)
    getUsedNumberSetting(caster, skill, setting).intValue
  }

  private def getUsedNumberSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): Number = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[Number]) {
      `val`.asInstanceOf[Number]
    }
    else {
      MathUtil.asDouble(`val`)
    }
  }

  def getUsedDoubleSetting(caster: SkillCaster, skill: Skill, setting: SkillSetting): Double = {
    check(caster, skill, setting)
    getUsedDoubleSetting(caster, skill, setting.node)
  }

  def getUsedDoubleSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): Double = {
    check(caster, skill, setting)
    getUsedNumberSetting(caster, skill, setting).doubleValue
  }

  def getUsedBooleanSetting(caster: SkillCaster, skill: Skill, setting: SkillSetting): Boolean = {
    check(caster, skill, setting)
    getUsedBooleanSetting(caster, skill, setting.node)
  }

  def getUsedBooleanSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): Boolean = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[Boolean]) {
      return `val`.asInstanceOf[Boolean]
    }
    throw new IllegalStateException("Undefined default for the following skill: " + skill.getName)
  }

  def getUsedStringSetting(caster: SkillCaster, skill: Skill, setting: SkillSetting): String = {
    check(caster, skill, setting)
    getUsedStringSetting(caster, skill, setting.node)
  }

  def getUsedStringSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): String = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[String]) {
      `val`.asInstanceOf[String]
    } else {
      `val`.toString
    }
  }

  def getUsedListSetting(caster: SkillCaster, skill: Skill, setting: SkillSetting): List[_] = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[List[_]]) {
      return `val`.asInstanceOf[List[_]]
    }
    throw new IllegalStateException("Illegal default for the following skill: " + skill.getName)
  }

  def getUsedListSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): List[_] = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[List[_]]) {
      return `val`.asInstanceOf[List[_]]
    }
    throw new IllegalStateException("Illegal default for the following skill: " + skill.getName)
  }

  @SuppressWarnings(Array("unchecked")) def getUsedStringListSetting(caster: SkillCaster, skill: Skill, setting: SkillSetting): List[String] = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[List[_]]) {
      return `val`.asInstanceOf[List[String]]
    }
    throw new IllegalStateException("Illegal default for the following skill: " + skill.getName)
  }

  def getUsedSetting(caster: SkillCaster, skill: Skill, setting: SkillSetting): AnyRef = {
    check(caster, skill, setting)
    getUsedSetting(caster, skill, setting.node)
  }

  private def check(caster: SkillCaster, skill: Skill, setting: SkillSetting) {
    checkNotNull(caster, "Cannot check the use configurations for a null caster!")
    checkNotNull(skill, "Cannot check the use configurations for a null skill!")
    checkNotNull(setting, "Cannot check the use configurations for a null setting!")
  }

  @SuppressWarnings(Array("unchecked")) def getUsedStringListSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): List[String] = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[List[_]]) {
      return `val`.asInstanceOf[List[String]]
    }
    throw new IllegalStateException("Illegal default for the following skill: " + skill.getName)
  }

  def getUsedItemStackSetting(caster: SkillCaster, skill: Skill, setting: SkillSetting): ItemStack = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[ItemStack]) {
      return RpgCommon.getGame.getRegistry.getItemBuilder.fromItemStack(`val`.asInstanceOf[ItemStack]).build
    }
    throw new IllegalStateException("Illegal default for the following skill: " + skill.getName)
  }

  def getUsedItemStackSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): ItemStack = {
    check(caster, skill, setting)
    val `val`: AnyRef = getUsedSetting(caster, skill, setting)
    if (`val`.isInstanceOf[ItemStack]) {
      return RpgCommon.getGame.getRegistry.getItemBuilder.fromItemStack(`val`.asInstanceOf[ItemStack]).build
    }
    throw new IllegalStateException("Illegal default for the following skill: " + skill.getName)
  }

  def getUsedSetting(caster: SkillCaster, skill: Skill, setting: DataQuery): AnyRef = {
    check(caster, skill, setting)
    if (this.customSettings.contains(caster) && this.customSettings.get(caster).contains(skill)) {
      return this.customSettings.get(caster).get(skill).getNode(setting).getValue
    }
    if (!isSettingConfigured(skill, setting)) {
      throw new IllegalStateException("The skill: " + skill.getName + " has no configured defaults for: " + setting)
    }
    if (caster.canPrimaryUseSkill(skill)) {
      return getSetting(caster.getPrimaryRole.get, skill, setting)
    }
    else if (caster.canSecondaryUseSkill(skill)) {
      return getSetting(caster.getSecondaryRole.get, skill, setting)
    }
    else if (caster.canAdditionalUseSkill(skill)) {
      import scala.collection.JavaConversions._
      for (role <- caster.getAdditionalRoles) {
        val optional: Optional[SkillAspect] = role.getAspect(classOf[SkillAspect])
        if (optional.isPresent && optional.get.hasSkillAtLevel(skill, caster.getLevel(role).get)) {
          return getSetting(role, skill, setting)
        }
      }
    }
    outsourcedSkillConfig.getNode(skill.getName + "." + setting).getValue
  }

  def getSetting(role: Role, skill: Skill, setting: DataQuery): AnyRef = {
    check(role, skill, setting)
    val config: ConfigurationNode = roleSkillConfigurations.get(role.getName)
    if (!isSettingConfigured(skill, setting)) {
      throw new IllegalStateException("The skill: " + skill.getName + " has no configured defaults for: " + setting)
    }
    val configurationSettingString: String = skill.getName + "." + setting
    if (!config.getNode(configurationSettingString).isVirtual) {
      config.getNode(configurationSettingString).getValue
    }
    else {
      getRawSetting(skill, setting)
    }
  }

  private def check(role: Role, skill: Skill, setting: DataQuery) {
    checkNotNull(role, "Cannot get a setting for a null role!")
    checkNotNull(skill, "Cannot get a setting for a null skill!")
    checkNotNull(setting, "Cannot get a setting for a null path!")
  }

  private def check(caster: SkillCaster, skill: Skill, setting: DataQuery) {
    checkNotNull(caster, "Cannot check the use configurations for a null caster!")
    checkNotNull(skill, "Cannot check the use configurations for a null skill!")
    checkNotNull(setting, "Cannot check the use configurations for a null setting!")
  }

  def shutdown() {
    this.customSettings.clear()
    roleSkillConfigurations.clear
  }

  def setClassDefaults() {
  }
}