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

import java.io.{File, FileOutputStream, InputStream, OutputStream}

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.util.ConfigManager
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader

import scala.util.control.Breaks._

/**
 * Standard implementation of ConfigManager specific for KraftRPG
 */
final class RPGConfigManager(val plugin: KraftRPGPlugin,
                             private val mainConfig: File,
                             private val configLoader: ConfigurationLoader[CommentedConfigurationNode]) extends ConfigManager {
  private val pluginDirectory: File = mainConfig.getParentFile
  private val roleConfigFolder: File = new File(pluginDirectory, "roles")
  private val expConfigFile: File = new File(pluginDirectory, "experience.hocon")
  private val damageConfigFile: File = new File(pluginDirectory, "damages.hocon")
  private val storageConfigFile: File = new File(pluginDirectory, "storage.hocon")

  def checkForConfig(config: File) {
    if (!config.exists) {
      try {
        this.plugin.getLogger.warn("File " + config.getName + " not found - generating defaults.")
        config.getParentFile.mkdir
        config.createNewFile
        val output: OutputStream = new FileOutputStream(config, false)
        val input: InputStream = classOf[RPGConfigManager].getResourceAsStream("/defaults/" + config.getName)
        val buf: Array[Byte] = new Array[Byte](8192)
        while (true) {
          val length: Int = input.read(buf)
          breakable({
            if (length < 0) {
              break()
            }
            output.write(buf, 0, length)
          })
        }
        input.close()
        output.close()
      }
      catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }
  }

  /**
   * Gets the configuration directory.
   *
   * @return The configuration directory
   */
  def getConfigDirectory: File = {
    this.pluginDirectory
  }
}