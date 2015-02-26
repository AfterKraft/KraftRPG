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
package com.afterkraft.kraftrpg.skills;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import com.google.common.base.Optional;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.roles.ExperienceType;
import com.afterkraft.kraftrpg.api.skills.Active;
import com.afterkraft.kraftrpg.api.skills.SkillCastResult;
import com.afterkraft.kraftrpg.api.skills.SkillConfigManager;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.api.skills.SkillType;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.common.skills.StalledSkill;

/**
 *
 */
public final class ActiveSkillRunner {

    private ActiveSkillRunner() {
    }

    public static SkillCastResult castSkillInitial(SkillCaster caster, Active skill,
                                                   String[] args) {
        if (caster.isDead()) {
            return SkillCastResult.DEAD;
        }

        if (!caster.canUseSkill(skill)) {
            return SkillCastResult.NOT_AVAILABLE;
        }

        // Already-stalled skill processing

        if (caster.getStalledSkill() != null) {
            Active stalled = caster.getStalledSkill().getActiveSkill();
            if (skill.equals(stalled)) {
                if (caster.getStalledSkill().isReady()) {
                    return castSkillPart2(caster, skill, args); // JUMP POINT
                    // it's basically a goto, lol
                } else {
                    if (caster.cancelStalledSkill(false)) {
                        return SkillCastResult.EVENT_CANCELLED;
                    } else {
                        return SkillCastResult.FAIL;
                    }
                }
            }
            if (skill.isType(SkillType.SELF_INTERRUPTING)) {
                if (stalled.isType(SkillType.UNINTERRUPTIBLE)) {
                    return SkillCastResult.STALLING_FAILURE;
                } else {
                    if (!caster.cancelStalledSkill(false)) {
                        return SkillCastResult.STALLING_FAILURE;
                    } else {
                        stalled = null;
                    }
                }
            }
            if (stalled != null && stalled.isType(SkillType.EXCLUSIVE)) {
                return SkillCastResult.STALLING_FAILURE;
            }
        }

        // Cooldowns

        long now = System.currentTimeMillis();
        if (caster.getGlobalCooldown() > now) {
            return SkillCastResult.ON_GLOBAL_COOLDOWN;
        }

        Optional<Long> cooldown = caster.getCooldown(skill.getName());
        if (cooldown.isPresent() && cooldown.get() > now) {
            return SkillCastResult.ON_COOLDOWN;
        }

        RPGPlugin plugin = KraftRPGPlugin.getInstance();
        if (plugin.getSkillConfigManager().isSettingConfigured(skill, SkillSetting.DELAY)) {
            // Newly stalled skill
            double delay = plugin.getSkillConfigManager()
                    .getUsedDoubleSetting(caster, skill, SkillSetting.DELAY);

            if (delay > 0) {
                if (caster.getStalledSkill() != null) {
                    return SkillCastResult.STALLING_FAILURE;
                }
                StalledSkill stalled = new StalledSkill(skill, args, caster, (long) delay);
                caster.setStalledSkill(stalled);
                return SkillCastResult.START_DELAY;
            }
        }
        return castSkillPart2(caster, skill, args);
    }

    private static SkillCastResult castSkillPart2(SkillCaster caster, Active skill, String[] args) {
        KraftRPGPlugin plugin = KraftRPGPlugin.getInstance();
        SkillConfigManager confman = plugin.getSkillConfigManager();
        if (plugin.getCombatTracker().isInCombat(caster)) {
            if (confman.getUsedBooleanSetting(caster, skill, SkillSetting.NO_COMBAT_USE)) {
                return SkillCastResult.NO_COMBAT;
            }
        }

        double healthCost = 0;
        double manaCost = 0;
        double hungerCost = 0;
        ItemStack reagent = null;
        int reagentQuantity = 0;

        if (confman.isSettingConfigured(skill, SkillSetting.HEALTH_COST)) {
            healthCost = confman.getUsedDoubleSetting(caster, skill, SkillSetting.HEALTH_COST);
        }
        if (confman.isSettingConfigured(skill, SkillSetting.MANA_COST)) {
            manaCost = confman.getUsedDoubleSetting(caster, skill, SkillSetting.MANA_COST);
        }
        if (confman.isSettingConfigured(skill, SkillSetting.STAMINA_COST)) {
            hungerCost = confman.getUsedDoubleSetting(caster, skill, SkillSetting.STAMINA_COST);
        }
        if (confman.isSettingConfigured(skill, SkillSetting.REAGENT)) {
            reagent = confman.getUsedItemStackSetting(caster, skill, SkillSetting.REAGENT);
        }
        if (confman.isSettingConfigured(skill, SkillSetting.REAGENT_QUANTITY)) {
            reagentQuantity =
                    confman.getUsedIntSetting(caster, skill, SkillSetting.REAGENT_QUANTITY);
        }
        if (reagentQuantity != -1 && reagent != null) {
            reagent.setQuantity(reagentQuantity);
        }

        /*
        SkillCastEvent skillEvent =
                new SkillCastEvent(caster, skill, manaCost, healthCost, hungerCost, reagent);
        plugin.getServer().getPluginManager().callEvent(skillEvent);
        if (skillEvent.isCancelled()) {
            return SkillCastResult.EVENT_CANCELLED;
        }


        healthCost = skillEvent.getHealthCost();
        manaCost = skillEvent.getManaCost();
        hungerCost = skillEvent.getStaminaCostAsExhaustion();
        reagent = skillEvent.getItemCost();
        */
        if (caster.getHealth() < healthCost) {
            return SkillCastResult.LOW_HEALTH;
        }
        if (caster.getMana() < manaCost) {
            return SkillCastResult.LOW_MANA;
        }
        /*
        if (caster.getEntity() instanceof Player) {
            Player p = (Player) caster.getEntity();
            float foodLevel = (p.getHunger() + p.getSaturation()) * 4 - p
                    .getExhaustion();
            if (foodLevel < hungerCost) {
                return SkillCastResult.LOW_STAMINA;
            }
        }
        if (reagent != null && !caster.getInventory()
                .containsAtLeast(reagent, reagent.getAmount())) {
            return SkillCastResult.MISSING_REAGENT;
        }
        */

        SkillCastResult result;
        try {
            try {
                if (!skill.parse(caster, args)) {
                    return SkillCastResult.SYNTAX_ERROR;
                }
            } catch (Throwable t) {
                plugin.getLogger().error("parsing arguments", t, new
                        Object[]{caster,
                        args});
                t.printStackTrace();
                return SkillCastResult.FAIL;
            }

            try {
                result = skill.checkCustomRestrictions(caster, false);
            } catch (Throwable t) {
                plugin.getLogger().error("checking restrictions", t,
                                         new Object[]{caster, args});
                t.printStackTrace();
                return SkillCastResult.FAIL;
            }

            if (result == SkillCastResult.ON_WARMUP) {
                skill.onWarmUp(caster);
                return result;
            } else if (result != SkillCastResult.NORMAL) {
                return result;
            }

            result = null;
            try {
                result = skill.useSkill(caster);
            } catch (Throwable t) {
                plugin.getLogger().error("using skill", t,
                                         new Object[]{caster, args});
            }
        } finally {
            try {
                skill.cleanState(caster);
            } catch (Throwable t) {
                plugin.getLogger().error("cleaning skill state", t,
                                        new Object[]{caster, args});
            }
        }

        if (result == null) {
            result = SkillCastResult.FAIL;
        }

        if (result == SkillCastResult.NORMAL) {
            caster.setHealth(caster.getHealth() - healthCost);
            caster.setMana(caster.getMana() - (int) manaCost);
            caster.modifyStamina((int) -hungerCost);
//            caster.getInventory().removeItem(reagent);

            double exp = plugin.getSkillConfigManager()
                    .getUsedDoubleSetting(caster, skill, SkillSetting.EXP_ON_CAST);
            if (exp > 0) {

                if (caster.canGainExperience(ExperienceType.SKILL)) {
                    caster.gainExperience(FixedPoint.valueOf(exp), ExperienceType.SKILL,
                                          caster.getLocation());
                }
            }

            long now = System.currentTimeMillis();
            long globalCD = plugin.getProperties().getDefaultGlobalCooldown();
            long cooldown = confman.getUsedIntSetting(caster, skill, SkillSetting.COOLDOWN);
            caster.setGlobalCooldown(now + globalCD);
            caster.setCooldown(skill.getName(), now + cooldown);
        }

        return result;
    }

}
