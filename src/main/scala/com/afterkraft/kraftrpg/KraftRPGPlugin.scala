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
package com.afterkraft.kraftrpg

import java.io.File
import javax.inject.Inject

import com.afterkraft.kraftrpg.api.entity.combat.CombatTracker
import com.afterkraft.kraftrpg.api.entity.party.PartyManager
import com.afterkraft.kraftrpg.api.entity.EntityManager
import com.afterkraft.kraftrpg.api.listeners.ListenerManager
import com.afterkraft.kraftrpg.api.roles.RoleManager
import com.afterkraft.kraftrpg.api.storage.StorageFrontend
import com.afterkraft.kraftrpg.api.{ExternalProviderRegistration, RPGPlugin, RpgCommon}
import com.afterkraft.kraftrpg.skills.{RPGSkillConfigManager, RPGSkillManager}
import com.afterkraft.kraftrpg.storage.RPGStorageManager
import com.afterkraft.kraftrpg.util.{RPGConfigManager, RpgDamageManager, RPGPluginProperties}
import com.google.inject.Singleton
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.event.state.{ConstructionEvent, PostInitializationEvent, PreInitializationEvent, ServerAboutToStartEvent, ServerStartingEvent, ServerStoppingEvent}
import org.spongepowered.api.plugin.{Plugin, PluginContainer}
import org.spongepowered.api.service.command.CommandService
import org.spongepowered.api.service.config.DefaultConfig
import org.spongepowered.api.event.Subscribe

object KraftRPGPlugin {

  val ADMIN_INVENTORY_BYPASS_PERMISSION: String = "kraftrpg.admin.bypass.inventory"
  private var instance: KraftRPGPlugin = null
  private var cancel: Boolean = false

  def getInstance: KraftRPGPlugin = {
    KraftRPGPlugin.instance
  }
}

@Singleton
@Plugin(id = "com.afterkraft.kraftrpg", name = "KraftRPG", version = "0.0.2-SNAPSHOT")
final class KraftRPGPlugin extends RPGPlugin {

  @Inject
  private val logger: Logger = null

  @Inject
  @DefaultConfig(sharedRoot = false)
  private val mainConfig: File = null

  @Inject
  @DefaultConfig(sharedRoot = false)
  private val configLoader: ConfigurationLoader[CommentedConfigurationNode] = null

  @Inject
  private val pluginContainer: PluginContainer = null
  private var skillManager: RPGSkillManager = null
  private var skillConfigManager: RPGSkillConfigManager = null
  private var storageManager: RPGStorageManager = null
  private var properties: RPGPluginProperties = null
  private var damageManager: RpgDamageManager = null
  private var configManager: RPGConfigManager = null
  private var partyManager: PartyManager = null
  private var enabled: Boolean = false

  @Subscribe def onConstruction(event: ConstructionEvent) {
    this.logger.info("[KraftRPG] Construct START")
    this.logger.info("[KraftRPG] Construct DONE")
  }

  @Subscribe def onPreInit(event: PreInitializationEvent) {
    this.logger.info("[KraftRPG] Pre-Init START")
    RpgCommon.setGame(event.getGame)
    ExternalProviderRegistration.pluginLoaded(this)
    KraftRPGPlugin.instance = this
    RpgCommon.setPlugin(this)
    this.configManager = new RPGConfigManager(this, this.mainConfig, this.configLoader)
    this.logger.info("[KraftRPG] Config file directory is: " + this.configManager.getConfigDirectory)
    this.logger.info("[KraftRPG] Pre-Init DONE")
  }

  @Subscribe def onPostInit(event: PostInitializationEvent) {
    this.logger.info("[KraftRPG] Post-Init START")
    this.properties = RPGPluginProperties.getInstance
    this.properties.initialize()
    this.logger.info("[KraftRPG] Post-Init DONE")
  }

  @Subscribe def onPreStart(event: ServerAboutToStartEvent) {
    this.logger.info("[KraftRPG] ServerAboutToStart START")
    RpgCommon.setCommonServer(event.getGame.getServer)
    ExternalProviderRegistration.finish()
    this.storageManager = new RPGStorageManager(this)
    if (KraftRPGPlugin.cancel) {
      return
    }
    this.skillManager = new RPGSkillManager(this)
    this.skillConfigManager = new RPGSkillConfigManager(this)
    this.damageManager = new RpgDamageManager(this)
    if (ExternalProviderRegistration.getPartyManager != null) {
      this.partyManager = ExternalProviderRegistration.getPartyManager
    }

    this.storageManager.initialize()
    this.skillConfigManager.initialize()
    this.skillManager.initialize()
    this.damageManager.initialize()
    this.partyManager.initialize()
    RpgCommon.finish()
    this.enabled = true
    this.logger.info("[KraftRPG] ServerAboutToStart DONE")
  }

  @Subscribe def onStarting(event: ServerStartingEvent) {
    this.logger.info("[KraftRPG] ServerStarting START")
    this.logger.info("[KraftRPG] ServerStarting DONE")
  }

  @Subscribe def onDisable(event: ServerStoppingEvent) {
    this.logger.info("[KraftRPG] ServerStopping START")
    try {
    }
    catch {
      case e: Exception =>

        this.logger.warn("------------------------------------------------")
        this.logger.warn("|--- Something did not shut down correctly! ---|")
        this.logger.warn("|--- Please make sure to report the following -|")
        this.logger.warn("|--- error to the KraftRPG devs! --------------|")
        e.printStackTrace()
        this.logger.warn("|----------------------------------------------|")
        this.logger.warn("|---------------- End of Error ----------------|")
        this.logger.warn("------------------------------------------------")
    }
    this.logger.info("[KraftRPG] ServerStopping DONE")
  }

  override def cancelEnable() {
    KraftRPGPlugin.cancel = true
  }

  override def getSkillConfigManager: RPGSkillConfigManager = this.skillConfigManager

  override def getCombatTracker: CombatTracker = null

  override def getEntityManager: EntityManager = null

  override def getStorage: StorageFrontend = this.storageManager.getStorage

  override def getConfigurationManager: RPGConfigManager = this.configManager

  override def getDamageManager: RpgDamageManager = this.damageManager

  override def getSkillManager: RPGSkillManager = this.skillManager

  override def getListenerManager: ListenerManager = ???

  override def getRoleManager: RoleManager = ???

  override def getPartyManager: PartyManager = this.partyManager

  override def getProperties: RPGPluginProperties = this.properties

  override def isEnabled: Boolean = this.enabled

  def getLogger: Logger = this.logger

  def getConfigLoader: ConfigurationLoader[CommentedConfigurationNode] = this.configLoader

  def getPluginContainer: PluginContainer = this.pluginContainer

  private def registerCommandExecutors() {
    val service: CommandService = RpgCommon.getGame.getCommandDispatcher
  }
}
