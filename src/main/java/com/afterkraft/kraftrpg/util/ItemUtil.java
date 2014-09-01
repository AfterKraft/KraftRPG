/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.kraftrpg.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtil {

    private ItemUtil() {

    }

    /**
     * Checks if the material given is considered a generic armor item.
     * This is only covering vanilla cases.
     *
     * @param mat The material of armor to check
     * @return True if the material is an armor type
     */
    public static boolean isArmor(Material mat) {
        switch (mat) {
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:
            case IRON_CHESTPLATE:
            case IRON_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case GOLD_HELMET:
            case GOLD_LEGGINGS:
            case GOLD_CHESTPLATE:
            case GOLD_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_BOOTS:
            case PUMPKIN:
                return true;
            default:
                return false;
        }
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
    public static String getFriendlyName(String string) {
        return capitalize(string.toLowerCase().replaceAll("_", " "));
    }

    public static String getFriendlyName(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }
        return getFriendlyName(itemStack.getType().toString());

    }

    /**
     * Checks if the material is considered a Weapon. Only applies to vanilla
     * weapons/tools.
     *
     * @param mat The material of the weapon/tool to check
     * @return True if the item type is considered a weapon/tool
     */
    public static boolean isWeapon(Material mat) {
        switch (mat) {
            case WOOD_AXE:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SPADE:
            case WOOD_SWORD:
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SPADE:
            case STONE_SWORD:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SPADE:
            case IRON_SWORD:
            case GOLD_AXE:
            case GOLD_HOE:
            case GOLD_PICKAXE:
            case GOLD_SPADE:
            case GOLD_SWORD:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SPADE:
            case DIAMOND_SWORD:
            case BOW:
            case FISHING_ROD:
                return true;
            default:
                return false;
        }
    }
}
