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
package com.afterkraft.kraftrpg.conversions

import scala.language.implicitConversions
import com.flowpowered.math.vector.{Vector3d => FlowVector3d}

object VectorMath {

  implicit def asFlowConverter(vector3d: FlowVector3d): AsScalaVec3d = {
    new AsScalaVec3d(vector3d.getX, vector3d.getY, vector3d.getZ)
  }

  object AsScalaVec3d {
    def apply(x: Double, y: Double, z: Double) = new AsScalaVec3d(x, y, z)
  }

  final class AsScalaVec3d(val x: Double, val y: Double, val z: Double) {

    def + (vector3d: AsScalaVec3d): AsScalaVec3d = AsScalaVec3d(x + vector3d.x, y + vector3d.y, z + vector3d.z)

    def - (vector3d: AsScalaVec3d): AsScalaVec3d = AsScalaVec3d(x - vector3d.x, y - vector3d.y, z - vector3d.z)

    def * (vector3d: AsScalaVec3d): AsScalaVec3d = AsScalaVec3d(x * vector3d.x, y * vector3d.y, z * vector3d.z)

    def / (vector3d: AsScalaVec3d): AsScalaVec3d = AsScalaVec3d(x / vector3d.x, y / vector3d.y, z / vector3d.z)

    def < (vector3d: AsScalaVec3d): Boolean = x < vector3d.x && y < vector3d.y && z < vector3d.z

    def <= (vector3d: AsScalaVec3d): Boolean = x <= vector3d.x && y <= vector3d.y && z <= vector3d.z

    def > (vector3d: AsScalaVec3d): Boolean = x > vector3d.x && y > vector3d.y && z > vector3d.z

    def >= (vector3d: AsScalaVec3d): Boolean = x >= vector3d.x && y >= vector3d.y && z >= vector3d.z

    def += (vector: AsScalaVec3d): AsScalaVec3d = AsScalaVec3d(x + vector.x, y + vector.y, z + vector.z)

    def == (vector3d: AsScalaVec3d): Boolean = x == vector3d.x && y == vector3d.y && z == vector3d.z

    override def hashCode(): Int = (super.hashCode() * (41 + x) * (41 + y) * (41 + z)).toInt

    override def equals(obj: scala.Any): Boolean = obj match {
      case that: AsScalaVec3d => this.x == that.x && this.y == that.y && this.z == that.z
    }
  }

}