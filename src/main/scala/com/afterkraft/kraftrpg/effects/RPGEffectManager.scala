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
package com.afterkraft.kraftrpg.effects

import java.util.UUID
import javax.annotation.Nullable
import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.RpgCommon
import com.afterkraft.kraftrpg.api.effects.{EffectManager, Expirable, Managed, Periodic, Timed}
import com.afterkraft.kraftrpg.api.entity.Insentient
import com.google.common.base.Optional
import org.spongepowered.api.service.scheduler.Task

import scala.collection.mutable

/**
 * Default implementation of EffectManager
 */
object RPGEffectManager {

  private val EFFECT_INTERVAL: Int = 2
}

class RPGEffectManager(private final val plugin: KraftRPGPlugin) extends EffectManager {

  private final val managedEffects: mutable.Set[Managed] = new mutable.HashSet[Managed]
  private final val pendingRemovals: mutable.Set[Managed] = new mutable.HashSet[Managed]
  private final val pendingAdditions: mutable.Set[Managed] = new mutable.HashSet[Managed]
  @Nullable private var taskID: UUID = null


  def manageEffect(being: Insentient, effect: Timed) {
    if (effect.isInstanceOf[Expirable] || effect.isInstanceOf[Periodic]) {
      this.pendingAdditions.add(new RPGManagedEffect(being, effect))
    }
  }

  def queueRemoval(being: Insentient, effect: Timed) {
    val mEffect: RPGManagedEffect = new RPGManagedEffect(being, effect)
    if (this.managedEffects.contains(mEffect)) {
      this.pendingRemovals.add(mEffect)
    }
  }

  def initialize() {
    if (this.taskID != null) {
      throw new IllegalStateException("RPGEffectManager is already initalized!")
    }
    val optional: Optional[Task] = RpgCommon.getGame.getSyncScheduler
      .runRepeatingTask(KraftRPGPlugin.getInstance.getPluginContainer, new EffectUpdater, RPGEffectManager.EFFECT_INTERVAL)
    if (optional.isPresent) {
      this.taskID = optional.get.getUniqueId
    }
    else {
      this.plugin.getLogger.error("[KraftRPG|Effects] Unable to " + "schedule effects task. Expect effects to break....")
    }
  }

  def shutdown() {
    if (this.taskID != null) {
      RpgCommon.getGame.getSyncScheduler.getTaskById(this.taskID).get.cancel
      this.taskID = null
    }
  }

  private[effects] class EffectUpdater extends Runnable {

    override def run() {
      val removals: Set[Managed] = Set.apply(RPGEffectManager.this.pendingRemovals.toArray:_*)
      RPGEffectManager.this.pendingRemovals.empty
      for (managed <- removals) {
        RPGEffectManager.this.managedEffects.remove(managed)
      }

      val additions: Set[Managed] = Set.apply(RPGEffectManager.this.pendingAdditions.toArray:_*)
      RPGEffectManager.this.pendingAdditions.empty
      for (managed <- additions) {
        RPGEffectManager.this.managedEffects.add(managed)
      }

      for (managed <- RPGEffectManager.this.managedEffects) {
        managed.getEffect match {
          case expirable: Expirable =>
            if (expirable.isExpired) {
              try {
                managed.getSentientBeing.removeEffect(managed.getEffect)
              }
              catch {
                case e: Exception =>
                  RPGEffectManager.this.plugin.getLogger
                    .error("There " + "was an " + "error attempting to " + "remove effect: "
                           + managed.getEffect.getName)
                  e.printStackTrace()
              }
            }
          case _ =>
        }
        managed.getEffect match {
          case periodic: Periodic =>
            try {
              if (periodic.isReady) {
                periodic.tick(managed.getSentientBeing)
              }
            }
            catch {
              case e: Exception =>
                RPGEffectManager.this.plugin.getLogger
                  .error("[KraftRPG] There was an error " + "attempting to " + "tick effect: "
                         + managed.getEffect.getName)
                e.printStackTrace()
            }
          case _ =>
        }
      }
    }
  }

}
