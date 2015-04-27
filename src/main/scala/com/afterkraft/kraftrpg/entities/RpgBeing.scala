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
package com.afterkraft.kraftrpg.entities

import java.util
import java.util.UUID

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.entity.Being
import com.afterkraft.kraftrpg.api.entity.component.Component
import com.afterkraft.kraftrpg.conversions.OptionalConversions._
import com.afterkraft.kraftrpg.conversions.VectorMath._
import com.afterkraft.kraftrpg.util.Predicates._
import com.flowpowered.math.vector.Vector3d
import com.google.common.base.Preconditions._
import org.spongepowered.api.data._
import org.spongepowered.api.entity.{Entity}
import org.spongepowered.api.text.{Texts, Text}
import org.spongepowered.api.util.RelativePositions
import org.spongepowered.api.world.{Location, World}

import scala.collection.JavaConversions._
import scala.ref.WeakReference

class RpgBeing(private val kraftRPGPlugin: KraftRPGPlugin,
               private val entity: Entity,
               private val name: String) extends Being {

  checkNotNull(entity)
  checkNotNull(kraftRPGPlugin)
  private[entities] val uniqueId = entity.getUniqueId
  private[entities] var reference = WeakReference(entity)

  override def getName: Text = this.reference.get match {
    case Some(ent) => Texts.of(ent.getType.getId)
    case _ => Texts.of(this.name)
  }

  override def getEntityType = this.reference.get match {
    case Some(x) => Some(x.getType)
    case _ => None
  }

  override def isOnGround = this.reference.get match {
    case Some(x) => x.isOnGround
    case _ => false
  }

  override def getNearbyEntities(x: Double, y: Double, z: Double) = this.reference.get match {
    case Some(ent) => ent.getWorld.getEntities(nearbyOf(ent.getLocation, new Vector3d(x, y, z)))
    case _ =>
      List[Entity]()
  }

  override def getDisplayName: Text = Texts.of(this.name)

  override def getUniqueID: UUID = this.reference.get match {
    case Some(ent) => ent.getUniqueId
    case _ => uniqueId
  }

  override def getEntity: ExOpt[Entity] = this.reference.get

  override def getWorld: World = this.reference.get match {
    case Some(ent) => ent.getWorld
    case _ => throw new IllegalStateException("Entity is not available!")
  }

  override def teleport(location: Location): Unit = this.reference.get match {
    case Some(ent) => ent.setLocation(location)
    case _ => throw new IllegalStateException("Entity is not available!")
  }

  override def teleport(location: Location, keepYawAndPitch: Boolean): Unit = this.reference.get match {
    case Some(ent) =>
      if (keepYawAndPitch) {
        var vec3d = new Vector3d(1, 2, 3)
        vec3d += vec3d

        ent.setLocationAndRotation(location, ent.getRotation, java.util.EnumSet.of(RelativePositions.PITCH,
                                                                              RelativePositions.YAW))
      }
      else {
        ent.setLocation(location)
      }
    case _ => throw new IllegalStateException("Entity is not available!")
  }

  override def isValid = this.reference.get.isDefined

  override def getLocation = this.reference.get match {
    case Some(ent) =>
      ent.getLocation
    case _ => throw new IllegalStateException("Entity is not available!")
  }

  private[entities] def getUnsafe = this.reference.apply()

  override def getComponent[T <: Component[T]](clazz: Class[T]): Opt[T] = ???

  override def getNearbyEntities(vector3d: Vector3d): util.Collection[Entity] = ???

  override def getNearbyBeings(v: Double, v1: Double, v2: Double): util.Collection[Being] = ???

  override def getNearbyBeings(vector3d: Vector3d): util.Collection[Being] = ???

  override def validateRawData(container: DataContainer): Boolean = ???

  override def getManipulators: util.Collection[DataManipulator[_]] = ???

  override def getData[T <: DataManipulator[T]](dataClass: Class[T]): Opt[T] = ???

  override def getProperty[T <: Property[_, _]](propertyClass: Class[T]): Opt[T] = ???

  override def getOrCreate[T <: DataManipulator[T]](manipulatorClass: Class[T]): Opt[T] = ???

  override def remove[T <: DataManipulator[T]](manipulatorClass: Class[T]): Boolean = ???

  override def isCompatible[T <: DataManipulator[T]](manipulatorClass: Class[T]): Boolean = ???

  override def setRawData(container: DataContainer): Unit = ???

  override def offer[T <: DataManipulator[T]](manipulatorData: T): DataTransactionResult = ???

  override def offer[T <: DataManipulator[T]](manipulatorData: T, priority: DataPriority): DataTransactionResult = ???

  override def getProperties: util.Collection[Property[_, _]] = ???

  override def toContainer: DataContainer = ???
}
