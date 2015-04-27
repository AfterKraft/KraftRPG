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

package com.afterkraft.kraftrpg.storage

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.{ExternalProviderRegistration, Manager}
import com.afterkraft.kraftrpg.api.storage.{StorageBackend, StorageFrontend}

/**
 * Default StorageManager
 */
class RPGStorageManager(private val plugin: KraftRPGPlugin) extends Manager {
  private var storage: StorageFrontend = null

  def initialize() {
    val configuredBackend: String = this.plugin.getProperties.getStorageType
    val backend: StorageBackend = ExternalProviderRegistration.getStorageBackendMap.get(configuredBackend)
    if (backend == null) {
      this.plugin.getLogger.error("ERROR - You specified the '" + configuredBackend + "' storage type," + " but that storage " + "type is not available.")
      val sb: StringBuilder = new StringBuilder("Available storage types are:")
      import scala.collection.JavaConversions._
      for (str <- ExternalProviderRegistration.getStorageBackendMap.keySet) {
        sb.append(" '").append(str).append("'")
      }
      this.plugin.getLogger.error(sb.toString())
      this.plugin.cancelEnable()
      return
    }
    try {
      backend.initialize()
    }
    catch {
      case e: Throwable =>
        e.printStackTrace()
        this.plugin.getLogger.error("The storage backend '" + configuredBackend + "' threw an exception" + " during startup:")
        this.plugin.getLogger.error(e.getMessage)
        this.plugin.cancelEnable()
        return
    }
    this.storage = ExternalProviderRegistration.getStorageFrontendOverride.construct(this.plugin, backend)
    this.plugin.getLogger.info("Storage initialized with provider " + this.storage.getName)
  }

  def shutdown() {
    this.storage.shutdown()
  }

  def getStorage: StorageFrontend = {
    this.storage
  }
}