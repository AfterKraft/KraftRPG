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
package com.afterkraft.kraftrpg.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.skills.Active;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.SkillCastResult;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.skills.ActiveSkillRunner;

public class RPGSkillCommand implements TabExecutor {
    private RPGPlugin plugin;

    public RPGSkillCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return null;
        Champion champ = plugin.getEntityManager().getChampion((Player) sender);

        if (args.length == 0) {
            String lastArg = args[args.length - 1];
            List<String> temp = new ArrayList<String>();

            StringUtil.copyPartialMatches(lastArg, champ.getActiveSkillNames(), temp);
            return temp;
        } else {
            String skillName = args[0];

            ISkill sk = plugin.getSkillManager().getSkill(skillName);
            if (!(sk instanceof Active)) return null;
            Active skill = (Active) sk;

            try {
                return skill.tabComplete(champ, args, 1);
            } catch (Throwable t) {
                plugin.logSkillThrowing(sk, "tab completing", t, new Object[] { sender, label, args });
                return null;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "No skill provided");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use skills");
            return true;
        }

        String skillName = args[0];
        ISkill sk = plugin.getSkillManager().getSkill(skillName);
        if (sk == null) {
            sender.sendMessage(ChatColor.RED + "Skill " + ChatColor.YELLOW + skillName + ChatColor.RED + " not found.");
            return true;
        }
        Champion champ = plugin.getEntityManager().getChampion((Player) sender);

        if (!champ.canUseSkill(sk)) {
            // Let's make some extra effort for a nice error message
            for (Role r : champ.getData().allRoles()) {
                int i = r.getLevelRequired(sk);
                if (i != 0) {
                    sender.sendMessage(ChatColor.RED + "You cannot use the skill " + ChatColor.YELLOW + skillName + ChatColor.RED + " until you are a " + ChatColor.BLUE + "Lv " + i + " " + r.getName() + ChatColor.RED + ".");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "The skill " + ChatColor.YELLOW + skillName + ChatColor.RED + " is not available to you.");
            return true;
        }

        if (args.length == 2) {
            String secondArg = args[1];
            if (secondArg.equals("?") || secondArg.equals("help")) {
                // TODO do help
                sender.sendMessage(sk.getDescription());
                return true;
            }
        }

        if (!(sk instanceof Active)) {
            sender.sendMessage(ChatColor.RED + "The skill " + ChatColor.YELLOW + skillName + ChatColor.RED + " cannot be triggered. Try " + ChatColor.LIGHT_PURPLE + "/skill " + skillName + " ?");
        }

        String[] cutArgs = Arrays.copyOfRange(args, 1, args.length);
        // TODO do i really want to make a new instance? not really.
        // but it'll do for now
        SkillCastResult result = new ActiveSkillRunner(plugin).castSkillInitial(champ, (Active) sk, cutArgs);

        switch (result) {
            case CUSTOM_NO_MESSAGE_FAILURE:
                // no message
                break;
            case DEAD:
                sender.sendMessage(ChatColor.RED + "Cannot use skills while dead!.");
                break;
            case EVENT_CANCELLED:
                sender.sendMessage(ChatColor.RED + "A plugin cancelled the skill.");
                break;
            case FAIL:
                sender.sendMessage(ChatColor.RED + "The skill failed.");
                break;
            case INVALID_TARGET:
                sender.sendMessage(ChatColor.RED + "Invalid target.");
                break;
            case LOW_HEALTH:
                sender.sendMessage(ChatColor.RED + "Not enough health! You need at least " + ChatColor.YELLOW + plugin.getSkillConfigManager().getUseSetting(champ, sk, SkillSetting.HEALTH_COST, -1, false) + ChatColor.RED + " HP (you have " + champ.getHealth() + ").");
                break;
            case LOW_MANA:
                sender.sendMessage(ChatColor.RED + "Not enough mana! You need at least " + ChatColor.YELLOW + plugin.getSkillConfigManager().getUseSetting(champ, sk, SkillSetting.MANA_COST, -1, false) + ChatColor.RED + " mana (you have " + champ.getMana() + ").");
                break;
            case LOW_STAMINA:
                sender.sendMessage(ChatColor.RED + "Not enough hunger! You need at least " + ChatColor.YELLOW + plugin.getSkillConfigManager().getUseSetting(champ, sk, SkillSetting.STAMINA_COST, -1, false) + ChatColor.RED + " quarter-food bars (you have " + champ.getStamina() + ").");
                break;
            case MISSING_REAGENT:
                ItemStack item = plugin.getSkillConfigManager().getUseSettingItem(champ, sk, SkillSetting.REAGENT, null);
                int invAmount = 0;
                for (Integer i : champ.getInventory().all(item.getType()).keySet()) {
                    invAmount += i;
                }
                sender.sendMessage(ChatColor.RED + "Not enough " + StringUtils.capitalize(item.getType().toString()) + "! You need at least " + ChatColor.YELLOW + item.getAmount() + ChatColor.RED + " items (you have " + invAmount + ").");
                break;
            case NORMAL:
                // no message
                break;
            case NOT_AVAILABLE:
                sender.sendMessage(ChatColor.RED + "You can't use this skill."); // Shouldn't happen unless skill returns it
                break;
            case NO_COMBAT:
                sender.sendMessage(ChatColor.RED + "Cannot use this skill in combat.");
                break;
            case ON_COOLDOWN:
                sender.sendMessage(ChatColor.RED + "The skill " + skillName + " is on cooldown. " + ((champ.getCooldown(skillName) - System.currentTimeMillis()) / 1000) + " seconds left.");
                break;
            case ON_GLOBAL_COOLDOWN:
                sender.sendMessage(ChatColor.RED + "You must wait " + ((champ.getGlobalCooldown() - System.currentTimeMillis()) / 1000) + " seconds before using another skill.");
                break;
            case ON_WARMUP:
                // no message (?)
                break;
            case REMOVED_EFFECT:
                // XXX what is this?
                break;
            case SKIP_POST_USAGE:
                // XXX what is this?
                break;
            case STALLING_FAILURE:
                sender.sendMessage(ChatColor.RED + "Could not use skill due to your currently pending skill.");
                break;
            case START_DELAY:
                break;
            case SYNTAX_ERROR:
                sender.sendMessage(ChatColor.RED + "You have a syntax error in your command. Try " + ChatColor.LIGHT_PURPLE + "/skill ? " + skillName + ChatColor.RED + " .");
                break;
            case UNTARGETTABLE_TARGET:
                sender.sendMessage(ChatColor.RED + "You may not target that.");
                break;
            default:
                break;

        }

        return true;
    }

}
