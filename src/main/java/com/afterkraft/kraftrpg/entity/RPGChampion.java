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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.EnterCombatReason;
import com.afterkraft.kraftrpg.api.entity.LeaveCombatReason;
import com.afterkraft.kraftrpg.api.entity.effects.IEffect;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.entity.roles.ExperienceType;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.entity.roles.RoleType;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Stalled;
import com.afterkraft.kraftrpg.api.storage.PlayerData;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.api.util.SkillRequirement;
import com.afterkraft.kraftrpg.util.MathUtil;


public class RPGChampion extends RPGEntityInsentient implements Champion {
    private PlayerData data;
    private Set<IEffect> effects = new HashSet<IEffect>();
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
    public boolean isDead() {
        return false;
    }

    @Override
    public Location getLocation() {
        if (this.isEntityValid()) {
            return this.getPlayer().getLocation();
        }
        return null;
    }

    @Override
    public final Player getPlayer() {
        return (Player) this.getEntity();
    }

    @Override
    public final Player getEntity() {
        return (Player) this.getEntity();
    }

    @Override
    public final void setPlayer(final Player player) {
        this.setEntity(player);
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
        return 0;
    }

    @Override
    public boolean canUseSkill(ISkill skill) {
        return false;
    }

    @Override
    public boolean isSkillRestricted(ISkill skill) {
        return false;
    }

    @Override
    public boolean canPrimaryUseSkill(ISkill skill) {
        return false;
    }

    @Override
    public boolean doesPrimaryRestrictSkill(ISkill skill) {
        return false;
    }

    @Override
    public boolean canSecondaryUseSkill(ISkill skill) {
        return false;
    }

    @Override
    public boolean doesSecondaryRestrictSkill(ISkill skill) {
        return false;
    }

    @Override
    public boolean canAdditionalUseSkill(ISkill skill) {
        return false;
    }

    @Override
    public boolean canSpecificAdditionalUseSkill(Role role, ISkill skill) {
        return false;
    }

    @Override
    public Role getPrimaryRole() {
        return data.primary;
    }

    @Override
    public boolean doesAdditionalRestrictSkill(ISkill skill) {
        return false;
    }

    @Override
    public Stalled getStalledSkill() {
        return null;
    }

    @Override
    public boolean setStalledSkill(Stalled stalledSkill) {
        return false;
    }

    @Override
    public boolean setStalledSkill(ISkill skill) {
        return false;
    }

    @Override
    public boolean cancelStalledSkill(boolean forced) {
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
        if (this.isEntityValid()) {
            return this.getPlayer().getItemInHand();
        }
        return null;
    }

    @Override
    public Inventory getInventory() {
        if (this.isEntityValid()) {
            return this.getPlayer().getInventory();
        }
        return null;
    }

    @Override
    public Role getSecondaryRole() {
        return data.profession;
    }

    @Override
    public void enterCombatWith(LivingEntity target, EnterCombatReason reason) {

    }

    @Override
    public void leaveCombatWith(LivingEntity target, LeaveCombatReason reason) {

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
        this.party = party;
    }

    @Override
    public void leaveParty() {
        this.party = null;
    }


    @Override
    public boolean setPrimaryRole(Role role) {
        if (role == null || role.getType() != RoleType.PRIMARY) {
            return false;
        }
        data.primary = role;
        this.recalculateMaxHealth();
        return true;
    }

    @Override
    public boolean setSecondaryRole(Role role) {
        if (role == null || role.getType() != RoleType.SECONDARY) {
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
        if (role == null || role.getType() != RoleType.ADDITIONAL) {
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
    public int getLevel(Role role) {
        return MathUtil.getLevel(this.getExperience(role));
    }

    @Override
    public FixedPoint getExperience(Role role) {
        if (role == null) {
            return new FixedPoint();
        }
        final FixedPoint exp = data.exp.get(role.getName());
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


    public double recalculateMaxHealth() {
        return 0D;
    }


    @Override
    public void heal(double amount) {

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
