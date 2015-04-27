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

package com.afterkraft.kraftrpg.entities.components

import java.lang.Iterable
import java.util

import com.afterkraft.kraftrpg.api.effects.{Effect, EffectType}
import com.afterkraft.kraftrpg.api.entity.component.EffectsComponent
import com.afterkraft.kraftrpg.conversions.OptionalConversions.Opt
import org.spongepowered.api.data.DataContainer

class RPGEffectsComponent extends AbstarctComponent[EffectsComponent]("effects") with EffectsComponent {

  override def set(es: Effect*): Unit = ???

  override def set(iterable: Iterable[Effect]): Unit = ???

  override def set(i: Int, e: Effect): Unit = ???

  override def get(i: Int): Opt[Effect] = ???

  override def remove(i: Int): Unit = ???

  override def contains(e: Effect): Boolean = ???

  override def add(e: Effect): Unit = ???

  override def add(i: Int, e: Effect): Unit = ???

  override def getAll: util.List[Effect] = ???

  override def toContainer: DataContainer = ???

  override def getEffect(name: String): Opt[Effect] = ???

  override def addEffect(effect: Effect): Unit = ???

  override def manualClearEffects(): Unit = ???

  override def removeEffect(effect: Effect): Unit = ???

  override def hasEffectType(`type`: EffectType): Boolean = ???

  override def clearEffects(): Unit = ???

  override def hasEffect(name: String): Boolean = ???

  override def manualRemoveEffect(effect: Effect): Unit = ???

  override def compareTo(o: EffectsComponent): Int = ???
}
