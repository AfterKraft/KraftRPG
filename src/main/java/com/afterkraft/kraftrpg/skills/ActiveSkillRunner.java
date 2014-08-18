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
package com.afterkraft.kraftrpg.skills;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.events.skills.SkillCastEvent;
import com.afterkraft.kraftrpg.api.skills.Active;
import com.afterkraft.kraftrpg.api.skills.SkillCastResult;
import com.afterkraft.kraftrpg.api.skills.SkillConfigManager;
import com.afterkraft.kraftrpg.api.skills.SkillSetting;
import com.afterkraft.kraftrpg.api.skills.SkillType;
import com.afterkraft.kraftrpg.api.skills.StalledSkill;


public final class ActiveSkillRunner {

    private ActiveSkillRunner() {
    }

    public static SkillCastResult castSkillInitial(SkillCaster caster, Active skill, String[] args) {
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

        Long cooldown = caster.getCooldown(skill.getName());
        if (cooldown != null && cooldown > now) {
            return SkillCastResult.ON_COOLDOWN;
        }

        RPGPlugin plugin = KraftRPGPlugin.getInstance();
        if (plugin.getSkillConfigManager().isSettingConfigured(skill, SkillSetting.DELAY)) {
            // Newly stalled skill
            double delay = plugin.getSkillConfigManager().getUsedDoubleSetting(caster, skill, SkillSetting.DELAY);

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
        RPGPlugin plugin = KraftRPGPlugin.getInstance();
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
            reagentQuantity = confman.getUsedIntSetting(caster, skill, SkillSetting.REAGENT_QUANTITY);
        }
        if (reagentQuantity != -1 && reagent != null) {
            reagent.setAmount(reagentQuantity);
        }

        SkillCastEvent skillEvent = new SkillCastEvent(caster, skill, manaCost, healthCost, hungerCost, reagent);
        plugin.getServer().getPluginManager().callEvent(skillEvent);
        if (skillEvent.isCancelled()) {
            return SkillCastResult.EVENT_CANCELLED;
        }

        healthCost = skillEvent.getHealthCost();
        manaCost = skillEvent.getManaCost();
        hungerCost = skillEvent.getStaminaCostAsExhaustion();
        reagent = skillEvent.getItemCost();

        if (caster.getHealth() < healthCost) {
            return SkillCastResult.LOW_HEALTH;
        }
        if (caster.getMana() < manaCost) {
            return SkillCastResult.LOW_MANA;
        }
        if (caster.getEntity() instanceof Player) {
            Player p = (Player) caster.getEntity();
            float foodLevel = (p.getFoodLevel() + p.getSaturation()) * 4 - p.getExhaustion();
            if (foodLevel < hungerCost) {
                return SkillCastResult.LOW_STAMINA;
            }
        }
        if (reagent != null && !caster.getInventory().containsAtLeast(reagent, reagent.getAmount())) {
            return SkillCastResult.MISSING_REAGENT;
        }

        SkillCastResult result;
        try {
            try {
                if (!skill.parse(caster, args)) {
                    return SkillCastResult.SYNTAX_ERROR;
                }
            } catch (Throwable t) {
                plugin.logSkillThrowing(skill, "parsing arguments", t, new Object[]{caster, args});
                t.printStackTrace();
                return SkillCastResult.FAIL;
            }

            try {
                result = skill.checkCustomRestrictions(caster, false);
            } catch (Throwable t) {
                plugin.logSkillThrowing(skill, "checking restrictions", t, new Object[]{caster, args});
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
                plugin.logSkillThrowing(skill, "using skill", t, new Object[]{caster, args});
            }
        } finally {
            try {
                skill.cleanState(caster);
            } catch (Throwable t) {
                plugin.logSkillThrowing(skill, "cleaning skill state", t, new Object[]{caster, args});
            }
        }

        if (result == null) {
            result = SkillCastResult.FAIL;
        }

        if (result == SkillCastResult.NORMAL) {
            caster.setHealth(caster.getHealth() - healthCost);
            caster.setMana(caster.getMana() - (int) manaCost);
            caster.modifyStamina((float) -hungerCost);
            caster.getInventory().removeItem(reagent);

            double exp = plugin.getSkillConfigManager().getUsedDoubleSetting(caster, skill, SkillSetting.EXP_ON_CAST);
            if (exp > 0) {

                //                 if (caster.canGainExperience(ExperienceType.SKILL)) {
                //                     caster.gainExperience(FixedPoint.valueOf(plugin.getSkillConfigManager().getUsedIntSetting(caster, this, SkillSetting.EXP, 0,false)), ExperienceType.SKILL, caster.getLocation());
                //                 }
                // TODO caster.get
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
