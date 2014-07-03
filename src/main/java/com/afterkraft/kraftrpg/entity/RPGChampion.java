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
package com.afterkraft.kraftrpg.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.roles.ExperienceType;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.skills.Active;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Stalled;
import com.afterkraft.kraftrpg.api.storage.PlayerData;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.api.util.SkillRequirement;
import com.afterkraft.kraftrpg.util.MathUtil;


public class RPGChampion extends RPGInsentient implements Champion {
    private PlayerData data;
    private Stalled stalled;
    private transient Party party;

    protected RPGChampion(RPGPlugin plugin, Player player, PlayerData data) {
        super(plugin, player, player.getName());
        this.data = data;
    }

    @Override
    public void removeSkillRequirement(SkillRequirement skillRequirement) {

    }

    @Override
    public Long getCooldown(String key) {
        if (key != null) {
            return data.cooldowns.get(key);
        } else {
            return 0L;
        }
    }

    @Override
    public long getGlobalCooldown() {
        return data.cooldowns.get("global");
    }

    @Override
    public void setGlobalCooldown(long duration) {
        data.cooldowns.put("global", duration);
    }

    @Override
    public void setCooldown(String key, long duration) {
        if (key != null) {
            data.cooldowns.put(key, duration);
        }
    }

    @Override
    public int getHighestSkillLevel(ISkill skill) {
        int level = 0;

        for (Role r : data.allRoles()) {
            int roleLevel = getLevel(r);
            if (r.hasSkillAtLevel(skill, roleLevel)) {
                if (roleLevel > level) {
                    level = roleLevel;
                }
            }
        }
        return level;
    }

    @Override
    public boolean canUseSkill(ISkill skill) {
        for (Role r : data.allRoles()) {
            if (r.hasSkillAtLevel(skill, getLevel(r))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<ISkill> getAvailableSkills() {
        Set<ISkill> skills = new HashSet<ISkill>();
        for (Role r : data.allRoles()) {
            skills.addAll(r.getAllSkillsAtLevel(getLevel(r)));
        }
        return skills;
    }

    @Override
    public Collection<String> getActiveSkillNames() {
        // TODO cache results?
        // this is basically /only/ for tab-completion

        Set<String> skillNames = new HashSet<String>();
        for (Role r : data.allRoles()) {
            for (ISkill skill : r.getAllSkillsAtLevel(getLevel(r))) {
                if (skill instanceof Active) {
                    skillNames.add(skill.getName());
                }
            }
        }
        return skillNames;
    }

    @Override
    public Collection<ISkill> getPossibleSkillsInRoles() {
        Set<ISkill> skills = new HashSet<ISkill>();
        for (Role r : data.allRoles()) {
            for (ISkill skill : r.getAllSkills()) {
                skills.add(skill);
            }
        }
        return skills;
    }

    @Override
    public boolean isSkillRestricted(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        if (data.primary.isSkillRestricted(skill)) {
            return true;
        } else if (data.profession != null && data.profession.isSkillRestricted(skill)) {
            return true;
        } else {
            for (Role additional : data.additionalRoles) {
                if (additional.isSkillRestricted(skill)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canPrimaryUseSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");

        return data.primary.hasSkillAtLevel(skill, getLevel(data.primary));
    }

    @Override
    public boolean doesPrimaryRestrictSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");

        return false;
    }

    @Override
    public boolean canSecondaryUseSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        return data.profession.hasSkillAtLevel(skill, getLevel(data.profession));
    }

    @Override
    public boolean doesSecondaryRestrictSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        return false;
    }

    @Override
    public boolean canAdditionalUseSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        for (Role r : data.additionalRoles) {
            if (r.hasSkillAtLevel(skill, getLevel(r))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canSpecificAdditionalUseSkill(Role role, ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        return role.hasSkillAtLevel(skill, getLevel(role));
    }

    @Override
    public boolean doesAdditionalRestrictSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        return false;
    }

    @Override
    public Stalled getStalledSkill() {
        return stalled;
    }

    @Override
    public boolean setStalledSkill(Stalled stalledSkill) {
        if (stalled != null && stalledSkill != null) {
            return false;
        }
        // TODO
        stalled = stalledSkill;
        return true;
    }

    @Override
    public boolean cancelStalledSkill(boolean forced) {
        return false;
    }

    @Override
    public FixedPoint getExperience(Role role) {
        Validate.notNull(role, "Cannot check a null Role!");
        final FixedPoint exp = data.exp.get(role);
        return exp == null ? new FixedPoint() : exp;
    }

    @Override
    public boolean canGainExperience(ExperienceType type) {
        return false;
    }

    @Override
    public FixedPoint gainExperience(FixedPoint exp, ExperienceType type, Location location) {
        return null;
    }

    @Override
    public Role getPrimaryRole() {
        return data.primary;
    }

    @Override
    public Role getSecondaryRole() {
        return data.profession;
    }

    @Override
    public boolean setPrimaryRole(Role role) {
        if (role == null || role.getType() != Role.RoleType.PRIMARY) {
            return false;
        }
        data.primary = role;
        this.recalculateMaxHealth();
        return true;
    }

    @Override
    public boolean setSecondaryRole(Role role) {
        if (role == null || role.getType() != Role.RoleType.SECONDARY) {
            return false;
        }
        data.profession = role;
        this.recalculateMaxHealth();
        return true;
    }

    @Override
    public Set<Role> getAdditionalRoles() {
        return Collections.unmodifiableSet(data.additionalRoles);
    }

    @Override
    public boolean addAdditionalRole(Role role) {
        if (role == null || role.getType() != Role.RoleType.ADDITIONAL) {
            return false;
        }
        if (role.equals(data.primary) || role.equals(data.profession)) {
            return false;
        }
        data.additionalRoles.add(role);
        return true;
    }

    @Override
    public boolean removeAdditionalRole(Role role) {
        // TODO employ some sort of Role interference logic
        return (role != null) && (data.additionalRoles.contains(role)) && !data.primary.equals(role) && !data.profession.equals(role) && data.additionalRoles.remove(role);
    }

    @Override
    public List<Role> getAllRoles() {
        ImmutableList.Builder<Role> roleBuilder = ImmutableList.builder();
        roleBuilder.add(data.primary);
        roleBuilder.add(data.profession);
        for (Role role : data.additionalRoles) {
            roleBuilder.add(role);
        }
        return roleBuilder.build();
    }

    @Override
    public int getLevel(Role role) {
        Validate.notNull(role, "Cannot get the experience of a null Role!");
        return MathUtil.getLevel(this.getExperience(role));
    }

    @Override
    public boolean hasParty() {
        return party != null;
    }

    @Override
    public Party getParty() {
        return party;
    }

    @Override
    public void setParty(Party party) {
        Validate.notNull(party, "Cannot set the Party to null!");
        this.party = party;
    }

    @Override
    public void leaveParty() {
        this.party = null;
    }

    @Override
    public int getMaxMana() {
        return 0;
    }

    @Override
    public void setMaxMana(int mana) {

    }

    @Override
    public double getMaxHealth() {
        return 0;
    }

    @Override
    public DamageWrapper getDamageWrapper() {
        return null;
    }

    @Override
    public void setDamageWrapper(DamageWrapper wrapper) {

    }

    @Override
    public int getNoDamageTicks() {
        return getPlayer().getNoDamageTicks();
    }

    @Override
    public float getStamina() {
        if (!this.isEntityValid()) {
            return 0F;
        }
        return getEntity().getFoodLevel() * 4 + getEntity().getSaturation() - getEntity().getExhaustion();
    }

    @Override
    public void modifyStamina(float staminaDiff) {
        if (!this.isEntityValid()) {
            return;
        }
        if (staminaDiff < 0) {
            // adding to exhaustion when negative
            getEntity().setExhaustion(getEntity().getExhaustion() - staminaDiff);
        } else {
            getEntity().setSaturation(getEntity().getSaturation() + staminaDiff);
        }
    }

    @Override
    public ItemStack[] getArmor() {
        if (!this.isEntityValid()) {
            return new ItemStack[4];
        } else {
            ItemStack[] armor = new ItemStack[4];
            for (int i = 0; i < getPlayer().getInventory().getArmorContents().length; i++) {
                armor[i] = new ItemStack(getPlayer().getInventory().getArmorContents()[i]);
            }
            return armor;
        }
    }

    @Override
    public void setArmor(ItemStack item, int armorSlot) throws IllegalArgumentException {
        Validate.isTrue(armorSlot < getPlayer().getInventory().getArmorContents().length, "Cannot set the armor slot greater than the current armor!");
        getPlayer().getInventory().getArmorContents()[armorSlot] = new ItemStack(item);
    }

    @Override
    public boolean canEquipItem(ItemStack itemStack) {
        return false;
    }

    @Override
    public FixedPoint getRewardExperience() {
        return new FixedPoint(); // TODO implement this
    }

    @Override
    public void setRewardExperience(FixedPoint experience) {
        // TODO implement this
    }

    @Override
    public boolean isIgnoringSkill(ISkill skill) {
        return false;
    }

    @Override
    public final Player getPlayer() {
        return this.getEntity();
    }

    @Override
    public final Player getEntity() {
        return (Player) super.getEntity();
    }

    public double recalculateMaxHealth() {
        return 0D;
    }

    @Override
    public void heal(double amount) {

    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void updateInventory() {
        if (this.isEntityValid()) {
            this.getPlayer().updateInventory();
        }
    }

    @Override
    public ItemStack getItemInHand() {
        return this.isEntityValid() ? this.getPlayer().getItemInHand() : null;
    }

    @Override
    public Inventory getInventory() {
        return this.isEntityValid() ? this.getPlayer().getInventory() : null;
    }

    @Override
    public void sendMessage(String message) {
        if (this.isEntityValid()) {
            this.getPlayer().sendMessage(message);
        }
    }

    @Override
    public final void setPlayer(final Player player) {
        this.setEntity(player);
    }

    @Override
    public boolean setEntity(Player player) {
        return super.setEntity(player);
    }

    @Override
    public PlayerData getData() {
        return data;
    }

    @Override
    public PlayerData getDataClone() {
        return data.clone();
    }

}
