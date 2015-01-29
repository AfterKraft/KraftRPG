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

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.RoleManager;
import com.afterkraft.kraftrpg.api.storage.PlayerData;

/**
 * Utility for managing Player based behaviors.
 */
public final class PlayerUtil {

    private PlayerUtil() {

    }

    public static void syncronizeExperienceBar(Champion champion) {
        RoleManager roleManager = KraftRPGPlugin.getInstance().getRoleManager();
        PlayerData data = champion.getData();
        final int level;
        final int currentExp;
        final int currentLevelExp;
        final int maxLevelExp;
        if (data.displayPrimaryRole) {
            level = champion.getLevel(data.primary);
            currentExp = data.exp.get(data.primary).intValue();
            currentLevelExp = roleManager.getRoleLevelExperience(data.primary, level).intValue();
            maxLevelExp = roleManager.getRoleLevelExperience(data.primary, level + 1).intValue()
                    - currentLevelExp;
            double percent = currentExp / maxLevelExp;
            Player player = champion.getPlayer();
            if (player != null) {
                player.setTotalExperience(level * 7);
                player.setExp(new Float(percent));
                player.setLevel(level);
            }
        }
    }

    public static int checkArmor(Champion champion, Player player) {
        final PlayerInventory playerInventory = player.getInventory();
        Material itemType;
        int removedCount = 0;

        final Role primary = champion.getPrimaryRole();
        final Role profession = champion.getSecondaryRole();
        if (playerInventory.getHelmet() != null) {
            itemType = playerInventory.getHelmet().getType();
            if (itemType != Material.AIR) {
                if (ItemUtil.isArmor(itemType)) {
                    if (!primary.isArmorAllowed(itemType) && ((profession == null) || !profession
                            .isArmorAllowed(itemType))) {
                        moveItem(champion, -1, playerInventory.getHelmet());
                        playerInventory.setHelmet(null);
                        removedCount++;
                    }
                }
            }

        }

        if (playerInventory.getChestplate() != null) {
            itemType = playerInventory.getChestplate().getType();
            if (itemType != Material.AIR) {
                if (!primary.isArmorAllowed(itemType) && ((profession == null) || !profession
                        .isArmorAllowed(itemType))) {
                    moveItem(champion, -1, playerInventory.getChestplate());
                    playerInventory.setChestplate(null);
                    removedCount++;
                }
            }
        }

        if (playerInventory.getLeggings() != null) {
            itemType = playerInventory.getLeggings().getType();
            if (itemType != Material.AIR) {
                if (!primary.isArmorAllowed(itemType) && ((profession == null) || !profession
                        .isArmorAllowed(itemType))) {
                    moveItem(champion, -1, playerInventory.getLeggings());
                    playerInventory.setLeggings(null);
                    removedCount++;
                }
            }
        }
        if (playerInventory.getBoots() != null) {
            itemType = playerInventory.getBoots().getType();
            if (itemType != Material.AIR) {
                if (!primary.isArmorAllowed(itemType) && ((profession == null) || !profession
                        .isArmorAllowed(itemType))) {
                    moveItem(champion, -1, playerInventory.getBoots());
                    playerInventory.setBoots(null);
                    removedCount++;
                }
            }
        }
        return removedCount;
    }

    public static boolean moveItem(Champion champion, int itemSlot, ItemStack item) {
        Player player = champion.getPlayer();
        PlayerInventory playerInventory = player.getInventory();
        final int empty = getFirstEmptySlot(playerInventory.getContents());
        if (empty == -1) {
            champion.getWorld().dropItemNaturally(champion.getLocation(), item);
            if (itemSlot != -1) {
                playerInventory.clear(itemSlot);
            }
            champion.sendMessage(Messaging.getMessage("inventory_move_item_untrained"),
                                 ItemUtil.getFriendlyName(item));
            return false;
        } else {
            playerInventory.setItem(empty, item);
            if (itemSlot != -1) {
                playerInventory.clear(itemSlot);
            }
            champion.sendMessage(Messaging.getMessage("inventory_move_item_untrained"),
                                 ItemUtil.getFriendlyName(item));
            return true;
        }
    }

    private static int getFirstEmptySlot(ItemStack[] contents) {
        for (int i = 9; i < contents.length; i++) {
            if (contents[i] == null || contents[i].getType() == Material.AIR) {
                return i;
            }
        }
        return -1;
    }
}
