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
package com.afterkraft.kraftrpg.commands;

import java.util.List;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.message.Messages;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.aspects.SkillAspect;
import com.afterkraft.kraftrpg.api.skills.Active;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.SkillCastResult;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.skills.ActiveSkillRunner;

/**
 * TODO Add documentation
 */
public class RPGSkillCommand implements CommandCallable {
    private KraftRPGPlugin plugin;

    public RPGSkillCommand(KraftRPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean call(CommandSource source, String arguments,
                        List<String> parents) throws
            CommandException {

        if (parents.size() == 1) {
            source.sendMessage(TextColors.RED + "No skill provided");
            return true;
        }
        if (!(source instanceof Player)) {
            source.sendMessage(TextColors.RED + "Only players can use skills");
            return true;
        }

        String skillName = parents.get(1);
        Optional<ISkill> optionalSkill = this.plugin
                .getSkillManager().getSkill(skillName);
        if (!optionalSkill.isPresent()) {
            source.sendMessage(TextColors.RED + "Skill " + TextColors.YELLOW
                                       + skillName + TextColors.RED
                                       + " not found.");
            return true;
        }
        Champion champ =
                this.plugin.getEntityManager().getChampion((Player) source).get();

        ISkill sk = optionalSkill.get();
        if (!champ.canUseSkill(sk)) {
            // Let's make some extra effort for a nice error message
            for (Role r : champ.getData().getAllRoles()) {
                if (r.getAspect(SkillAspect.class).isPresent()) {
                    SkillAspect skillAspect = r.getAspect(SkillAspect.class)
                            .get();
                    Optional<Integer> level = skillAspect.getLevelRequired(sk);
                    if (level.isPresent() && level.get() != 0) {
                        source.sendMessage(
                                TextColors.RED + "You cannot use the skill "
                                        + TextColors.YELLOW + skillName
                                        + TextColors.RED
                                        + " until you are a " + TextColors.BLUE
                                        + "Lv " + level.get()
                                        + " " + r.getName() + TextColors.RED
                                        + ".");
                        return true;
                    }
                }
            }
            source.sendMessage(TextColors.RED + "The skill " + TextColors.YELLOW
                                       + skillName + TextColors.RED
                                       + " is not available to you.");
            return true;
        }

        if (parents.size() == 3) {
            String secondArg = parents.get(2);
            if (secondArg.equals("?") || secondArg.equals("help")) {
                // TODO do help
                source.sendMessage(sk.getDescription());
                return true;
            }
        }

        if (!(sk instanceof Active)) {
            source.sendMessage(TextColors.RED + "The skill " + TextColors.YELLOW
                                       + skillName + TextColors.RED
                                       + " cannot be triggered. Try "
                                       + TextColors.LIGHT_PURPLE + "/skill "
                                       + skillName + " ?");
        } else {
            String[] cutArgs = parents
                    .subList(1, parents.size() - 1).toArray(new String[]{});
            SkillCastResult result;
            result = ActiveSkillRunner
                            .castSkillInitial(champ, (Active) sk, cutArgs);
            Message message;
            switch (result) {
                case CUSTOM_NO_MESSAGE_FAILURE:
                    // no message
                    break;
                case DEAD:
                    source.sendMessage(
                            TextColors.RED + "Cannot use skills while dead!.");
                    break;
                case EVENT_CANCELLED:
                    source.sendMessage(
                            TextColors.RED + "A plugin cancelled the skill.");
                    break;
                case FAIL:
                    source.sendMessage(TextColors.RED + "The skill failed.");
                    break;
                case INVALID_TARGET:
                    source.sendMessage(TextColors.RED + "Invalid target.");
                    break;
                case LOW_HEALTH:
                    source.sendMessage(
                            TextColors.RED
                                    + "Not enough health! You need at least "
                                    + TextColors.YELLOW
                                    + this.plugin.getSkillConfigManager()
                                    .getUsedIntSetting(champ,
                                                       sk,
                                                       SkillSetting.HEALTH_COST)
                                    + TextColors.RED
                                    + " HP (you have "
                                    + champ.getHealth() + ").");
                    break;
                case LOW_MANA:
                    message = Messages.of("Not enough mana! You need at least "
                                                  + TextColors.YELLOW
                                                  + this.plugin
                            .getSkillConfigManager()
                            .getUsedIntSetting(champ,
                                               sk, SkillSetting.MANA_COST)
                                                  + TextColors.RED
                                                  + " mana (you have "
                                                  + champ.getMana() + ").")
                            .builder().color(TextColors.RED).build();
                    source.sendMessage(message);
                    break;
                case LOW_STAMINA:
                    source.sendMessage(
                            TextColors.RED
                                    + "Not enough hunger! You need at least "
                                    + TextColors.YELLOW
                                    + this.plugin.getSkillConfigManager()
                                    .getUsedIntSetting(champ,
                                                       sk,
                                                       SkillSetting.STAMINA_COST)
                                    + TextColors.RED
                                    + " quarter-food bars (you have " + champ
                                    .getStamina()
                                    + ").");
                    break;
                case MISSING_REAGENT:
                    ItemStack item = this.plugin.getSkillConfigManager()
                            .getUsedItemStackSetting(
                                    champ, sk, SkillSetting.REAGENT);
                    int invAmount = 0;
                    /* TODO REDO
                    for (Integer i : champ.getInventory().all(item.getItem())
                            .keySet()) {
                        invAmount += i;
                    }
                    source.sendMessage(TextColors.RED + "Not enough "
                                               + StringUtils
                            .capitalize(item.getType().toString())
                                               + "! You need at least "
                                               + TextColors.YELLOW + item
                            .getAmount()
                                               + TextColors.RED
                                               + " items (you have "
                                               + invAmount
                                               + ").");
                                               */
                    break;
                case NORMAL:
                    // no message
                    break;
                case NOT_AVAILABLE:
                    // Shouldn't happen unless skill returns it
                    source.sendMessage(
                            TextColors.RED + "You can't use this skill.");
                    break;
                case NO_COMBAT:
                    source.sendMessage(
                            TextColors.RED
                                    + "Cannot use this skill in combat.");
                    break;
                case ON_COOLDOWN:
                    source.sendMessage(TextColors.RED + "The skill " + skillName
                                               + " is on cooldown. " + (
                            (champ.getCooldown(skillName).get()
                                    - System.currentTimeMillis()) / 1000)
                                               + " seconds left.");
                    break;
                case ON_GLOBAL_COOLDOWN:
                    message = Messages.of("You must wait "
                                                  + (
                            (champ.getGlobalCooldown() - System
                                    .currentTimeMillis())
                                    / 1000)
                                                  + " seconds before "
                                                  + "using another skill"
                                                  + ".").builder()
                            .color(TextColors.RED).build();
                    source.sendMessage(message);
                    break;
                case ON_WARMUP:
                    // no message (?)
                    break;
                case STALLING_FAILURE:
                    message = Messages.of("Could not use skill due to "
                                                  + "your currently"
                                                  + " pending skill.").builder()
                            .color(TextColors.RED).build();
                    source.sendMessage(message);
                    break;
                case START_DELAY:
                    break;
                case SYNTAX_ERROR:
                    source.sendMessage(TextColors.RED
                                               + "You have a syntax error in your command. Try "
                                               + TextColors.LIGHT_PURPLE
                                               + "/skill ? " + skillName
                                               + TextColors.RED + " .");
                    break;
                case UNTARGETABLE_TARGET:
                    source.sendMessage(
                            TextColors.RED + "You may not target that.");
                    break;
                default:
                    break;

            }
        }
        return true;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return false;
    }

    @Override
    public Optional<String> getShortDescription() {
        return Optional.of("The main skill command.");
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Use /skill <skillName> to cast a skill");
    }

    @Override
    public String getUsage() {
        return "Use /skill <skillName> to cast a skill.";
    }

    @Override
    public List<String> getSuggestions(CommandSource source,
                                       String arguments) throws
            CommandException {
        if (!(source instanceof Player)) {
            return Lists.newArrayList();
        }
        Champion champ = this.plugin.getEntityManager()
                .getChampion((Player) source).get();

        if (arguments.length() == 0) {
            List<String> temp = Lists.newArrayList();
            temp.addAll(champ.getActiveSkillNames());
            return temp;
        } else {

            Optional<ISkill> sk =
                    this.plugin.getSkillManager().getSkill(arguments);
            if (!(sk instanceof Active)) {
                return Lists.newArrayList();
            }
            Active skill = (Active) sk;

            try {

                return skill.tabComplete(champ, arguments.split(" "), 1);
            } catch (Throwable t) {
                this.plugin.getLogger().error("Error tab completing the "
                                                      + "skill: " + sk.get()
                        .getName() + "tab "
                                                      + "completing", t, new
                        Object[]
                        {source, arguments});
                return Lists.newArrayList();
            }
        }
    }
}
