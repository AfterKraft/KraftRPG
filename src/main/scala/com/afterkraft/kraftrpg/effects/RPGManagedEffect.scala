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

import java.util.concurrent.{Delayed, TimeUnit}

import com.afterkraft.kraftrpg.api.effects.{Effect, Managed, Timed}
import com.afterkraft.kraftrpg.api.entity.Insentient
import com.google.common.base.{Preconditions, Objects}

/**
 * Default implementatino of a Managed Effect.
 */
class RPGManagedEffect(final val entity: Insentient, final val effect: Timed) extends Managed {
  Preconditions.checkNotNull(effect)
  Preconditions.checkNotNull(entity)

  def getEffect: Effect = this.effect.asInstanceOf[Effect]

  def getSentientBeing: Insentient = this.entity

  override def hashCode: Int = Objects.hashCode(this.effect, this.entity)

  override def equals(obj: Any): Boolean = {
    var result = false
    obj match {
      case effect: RPGManagedEffect =>
        result = effect.effect.eq(this.effect) && effect.entity.eq(this.entity)
      case _ =>
    }
    result
  }

  def getDelay(timeUnit: TimeUnit): Long = this.effect.getDelay(timeUnit)

  def compareTo(o: Delayed): Int = this.effect.compareTo(o)
}
