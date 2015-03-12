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

import org.spongepowered.api.item.ItemType
import org.spongepowered.api.item.ItemTypes._
import org.spongepowered.api.item.inventory.ItemStack

/**
 * Standard utility for handling Items. This includes utilities for
 * serialization of item names and representation of items to the client.
 */
object ItemUtil {

  /**
   * Checks if the material given is considered a generic armor item. This is
   * only covering vanilla cases.
   *
   * @param mat The material of armor to check
   * @return True if the material is an armor type
   */
  def isArmor(mat: ItemType): Boolean = {
    (mat eq LEATHER_HELMET) || (mat eq LEATHER_LEGGINGS) || (mat eq LEATHER_BOOTS) || (mat eq
           LEATHER_CHESTPLATE )|| (mat eq IRON_HELMET) || (mat eq IRON_LEGGINGS) || (mat eq
           IRON_CHESTPLATE) || (mat eq IRON_BOOTS) || (mat eq CHAINMAIL_HELMET) || (mat eq
           CHAINMAIL_LEGGINGS) || (mat eq CHAINMAIL_BOOTS) || (mat eq CHAINMAIL_CHESTPLATE) || (mat eq
           GOLDEN_HELMET) || (mat eq GOLDEN_LEGGINGS) || (mat eq GOLDEN_CHESTPLATE) || (mat eq
           GOLDEN_BOOTS) || (mat eq DIAMOND_HELMET) || (mat eq DIAMOND_LEGGINGS) || (mat eq
           DIAMOND_CHESTPLATE) || (mat eq DIAMOND_BOOTS) || (mat eq PUMPKIN)
  }

  def getFriendlyName(string: String): String = capitalize(string.toLowerCase.replaceAll("_", " "))

  def capitalize(string: String): String = {
    val chars: Array[Char] = string.toLowerCase.toCharArray
    var found: Boolean = false
    var i: Int = 0
    while (i < chars.length) {
      {
        if (!found && Character.isLetter(chars(i))) {
          chars(i) = Character.toUpperCase(chars(i))
          found = true
        }
        else if (Character.isWhitespace(chars(i))) {
          found = false
        }
      }
      i += 1; i - 1
    }
    String.valueOf(chars)
  }

  def getFriendlyName(itemStack: ItemStack): String = {
    itemStack.getItem.getId
  }

  /**
   * Checks if the material is considered a Weapon. Only applies to vanilla
   * weapons/tools.
   *
   * @param mat The material of the weapon/tool to check
   * @return True if the item type is considered a weapon/tool
   */
  def isWeapon(mat: ItemType): Boolean = {
    (mat eq WOODEN_AXE) || (mat eq WOODEN_HOE) || (mat eq WOODEN_PICKAXE) || (mat eq WOODEN_SHOVEL) || (mat eq
           WOODEN_SWORD) || (mat eq STONE_AXE) || (mat eq STONE_HOE) || (mat eq STONE_PICKAXE) || (mat eq
           STONE_SHOVEL) || (mat eq STONE_SWORD) || (mat eq IRON_AXE) || (mat eq IRON_HOE) || (mat eq
           IRON_PICKAXE) || (mat eq IRON_SHOVEL) || (mat eq IRON_SWORD) || (mat eq GOLDEN_AXE) || (mat eq
           GOLDEN_HOE) || (mat eq GOLDEN_PICKAXE) || (mat eq GOLDEN_SHOVEL) || (mat eq GOLDEN_SWORD) || (mat eq
           DIAMOND_AXE) || (mat eq DIAMOND_HOE) || (mat eq DIAMOND_PICKAXE) || (mat eq DIAMOND_SHOVEL) || (mat eq
           DIAMOND_SWORD) || (mat eq BOW) || (mat eq FISHING_ROD)
  }
}
class ItemUtil private() { }
