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

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.RoleManager;
import com.afterkraft.kraftrpg.api.storage.PlayerData;

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
            maxLevelExp = roleManager.getRoleLevelExperience(data.primary, level + 1).intValue() - currentLevelExp;
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
                    if (!primary.isArmorAllowed(itemType) && ((profession == null) || !profession.isArmorAllowed(itemType))) {
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
                if (!primary.isArmorAllowed(itemType) && ((profession == null) || !profession.isArmorAllowed(itemType))) {
                    moveItem(champion, -1, playerInventory.getChestplate());
                    playerInventory.setChestplate(null);
                    removedCount++;
                }
            }
        }

        if (playerInventory.getLeggings() != null) {
            itemType = playerInventory.getLeggings().getType();
            if (itemType != Material.AIR) {
                if (!primary.isArmorAllowed(itemType) && ((profession == null) || !profession.isArmorAllowed(itemType))) {
                    moveItem(champion, -1, playerInventory.getLeggings());
                    playerInventory.setLeggings(null);
                    removedCount++;
                }
            }
        }
        if (playerInventory.getBoots() != null) {
            itemType = playerInventory.getBoots().getType();
            if (itemType != Material.AIR) {
                if (!primary.isArmorAllowed(itemType) && ((profession == null) || !profession.isArmorAllowed(itemType))) {
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
            champion.sendMessage(Messaging.getMessage("inventory_move_item_untrained"), ItemUtil.getFriendlyName(item));
            return false;
        } else {
            playerInventory.setItem(empty, item);
            if (itemSlot != -1) {
                playerInventory.clear(itemSlot);
            }
            champion.sendMessage(Messaging.getMessage("inventory_move_item_untrained"), ItemUtil.getFriendlyName(item));
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
