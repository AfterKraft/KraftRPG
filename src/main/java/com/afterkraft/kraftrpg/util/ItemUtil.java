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
package com.afterkraft.kraftrpg.util;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * Standard utility for handling Items. This includes utilities for
 * serialization of item names and representation of items to the client.
 */
public class ItemUtil {

    private ItemUtil() {

    }

    /**
     * Checks if the material given is considered a generic armor item. This is
     * only covering vanilla cases.
     *
     * @param mat The material of armor to check
     * @return True if the material is an armor type
     */
    public static boolean isArmor(ItemType mat) {
        return
                mat == ItemTypes.LEATHER_HELMET
                        || mat == ItemTypes.LEATHER_LEGGINGS
                        || mat == ItemTypes.LEATHER_BOOTS
                        || mat == ItemTypes.LEATHER_CHESTPLATE
                        || mat == ItemTypes.IRON_HELMET
                        || mat == ItemTypes.IRON_LEGGINGS
                        || mat == ItemTypes.IRON_CHESTPLATE
                        || mat == ItemTypes.IRON_BOOTS
                        || mat == ItemTypes.CHAINMAIL_HELMET
                        || mat == ItemTypes.CHAINMAIL_LEGGINGS
                        || mat == ItemTypes.CHAINMAIL_BOOTS
                        || mat == ItemTypes.CHAINMAIL_CHESTPLATE
                        || mat == ItemTypes.GOLDEN_HELMET
                        || mat == ItemTypes.GOLDEN_LEGGINGS
                        || mat == ItemTypes.GOLDEN_CHESTPLATE
                        || mat == ItemTypes.GOLDEN_BOOTS
                        || mat == ItemTypes.DIAMOND_HELMET
                        || mat == ItemTypes.DIAMOND_LEGGINGS
                        || mat == ItemTypes.DIAMOND_CHESTPLATE
                        || mat == ItemTypes.DIAMOND_BOOTS
                        || mat == ItemTypes.PUMPKIN;

    }

    public static String getFriendlyName(String string) {
        return capitalize(string.toLowerCase().replaceAll("_", " "));
    }

    public static String capitalize(String string) {
        final char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i])) {
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    public static String getFriendlyName(ItemStack itemStack) {
        return itemStack.getItem().getId();

    }

    /**
     * Checks if the material is considered a Weapon. Only applies to vanilla
     * weapons/tools.
     *
     * @param mat The material of the weapon/tool to check
     * @return True if the item type is considered a weapon/tool
     */
    public static boolean isWeapon(ItemType mat) {
        return mat == ItemTypes.WOODEN_AXE || mat == ItemTypes.WOODEN_HOE
                || mat == ItemTypes.WOODEN_PICKAXE
                || mat == ItemTypes.WOODEN_SHOVEL
                || mat == ItemTypes.WOODEN_SWORD || mat == ItemTypes.STONE_AXE
                || mat == ItemTypes.STONE_HOE || mat == ItemTypes.STONE_PICKAXE
                || mat == ItemTypes.STONE_SHOVEL || mat == ItemTypes.STONE_SWORD
                || mat == ItemTypes.IRON_AXE || mat == ItemTypes.IRON_HOE
                || mat == ItemTypes.IRON_PICKAXE || mat == ItemTypes.IRON_SHOVEL
                || mat == ItemTypes.IRON_SWORD || mat == ItemTypes.GOLDEN_AXE
                || mat == ItemTypes.GOLDEN_HOE
                || mat == ItemTypes.GOLDEN_PICKAXE
                || mat == ItemTypes.GOLDEN_SHOVEL
                || mat == ItemTypes.GOLDEN_SWORD || mat == ItemTypes.DIAMOND_AXE
                || mat == ItemTypes.DIAMOND_HOE
                || mat == ItemTypes.DIAMOND_PICKAXE
                || mat == ItemTypes.DIAMOND_SHOVEL
                || mat == ItemTypes.DIAMOND_SWORD || mat == ItemTypes.BOW
                || mat == ItemTypes.FISHING_ROD;
    }
}
