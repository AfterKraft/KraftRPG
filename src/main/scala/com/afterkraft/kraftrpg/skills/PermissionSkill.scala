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

import java.{lang, util}

import com.afterkraft.kraftrpg.KraftRPGPlugin
import com.afterkraft.kraftrpg.api.entity.{Champion, Sentient, SkillCaster}
import com.afterkraft.kraftrpg.api.skills.{Permissible, SkillSetting, SkillType}
import org.spongepowered.api.data.DataView
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.text.Text

class PermissionSkill(plugin: KraftRPGPlugin, s: String) extends Permissible {

  override def getPermissions: util.Map[String, lang.Boolean] = ???

  override def tryLearning(being: Sentient): Unit = ???

  override def tryUnlearning(being: Sentient): Unit = ???

  override def shutdown(): Unit = ???

  override def isType(`type`: SkillType): Boolean = ???

  override def getName: String = ???

  override def getPermissionNode: String = ???

  override def getUsedConfigNodes: util.Collection[SkillSetting] = ???

  override def getDescription: Text = ???

  override def getDefaultConfig: DataView = ???

  override def initialize(): Unit = ???

  override def addSkillTarget(entity: Entity, caster: SkillCaster): Boolean = ???

  override def isInMessageRange(broadcaster: SkillCaster, receiver: Champion): Boolean = ???
}
