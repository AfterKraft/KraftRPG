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

import com.flowpowered.math.vector.Vector3d
import com.google.common.base.Predicate
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.world.Location

object Predicates {

  def nearbyOf(location: Location, vector3d: Vector3d): Predicate[Entity] = {
    new Predicate[Entity] {
      override def apply(input: Entity) = {
        val pos = location.getPosition
        val other = input.getLocation.getPosition
        pos.getX + vector3d.getX <= other.getX && pos.getY + vector3d.getY <= other.getY && pos.getZ + vector3d.getZ <= other.getZ
      }
    }
  }

}

class Predicates private() {

}
