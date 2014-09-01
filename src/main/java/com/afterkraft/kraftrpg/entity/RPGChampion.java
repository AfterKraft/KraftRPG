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

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.events.roles.ExperienceChangeEvent;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.roles.ExperienceType;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.Role.RoleType;
import com.afterkraft.kraftrpg.api.skills.Active;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Stalled;
import com.afterkraft.kraftrpg.api.storage.PlayerData;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.util.ItemUtil;
import com.afterkraft.kraftrpg.util.MathUtil;
import com.afterkraft.kraftrpg.util.Messaging;
import com.afterkraft.kraftrpg.util.PlayerUtil;


public class RPGChampion extends RPGInsentient implements Champion {
    public static final String GLOBAL_COOLDOWN_KEY = "global";
    private PlayerData data;
    private Stalled stalled;
    private transient Party party;
    private transient DamageWrapper damageWrapper;

    protected RPGChampion(RPGPlugin plugin, Player player, PlayerData data) {
        super(plugin, player, player.getName());
        this.data = data;
    }

    @Override
    public Long getCooldown(String key) {
        Validate.notNull(key, "Cannot get a null cooldown!");
        Validate.isTrue(!key.isEmpty(), "Cannot get an empty keyed cooldown!");
        if (this.data.cooldowns.containsKey(key))
            return this.data.cooldowns.get(key);
        else
            return 0L;
    }

    @Override
    public long getGlobalCooldown() {
        return getCooldown(RPGChampion.GLOBAL_COOLDOWN_KEY);
    }

    @Override
    public void setGlobalCooldown(long duration) {
        Validate.isTrue(duration < Long.MAX_VALUE - System.currentTimeMillis(), "Cannot set the duration longer than the maximum time of the system!");
        this.data.cooldowns.put(RPGChampion.GLOBAL_COOLDOWN_KEY, duration);
    }

    @Override
    public void setCooldown(String key, long duration) {
        Validate.notNull(key, "Cannot set the cooldown of a null key!");
        Validate.notEmpty(key, "Cannot set the cooldown of an empty key!");
        Validate.isTrue(duration < Long.MAX_VALUE - System.currentTimeMillis(), "Cannot set the duration longer than the maximum time of the system!");
        this.data.cooldowns.put(key, duration);
    }

    @Override
    public int getHighestSkillLevel(ISkill skill) {
        Validate.notNull(skill, "Cannot get the highest null skill level!");
        int level = 0;

        for (Role r : this.data.getAllRoles()) {
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
        Validate.notNull(skill, "Cannot check whether Champion can use null skill!");
        for (Role r : this.data.getAllRoles()) {
            if (r.hasSkillAtLevel(skill, getLevel(r))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<ISkill> getAvailableSkills() {
        Set<ISkill> skills = new HashSet<ISkill>();
        for (Role r : this.data.getAllRoles()) {
            skills.addAll(r.getAllSkillsAtLevel(getLevel(r)));
        }
        return skills;
    }

    @Override
    public Collection<String> getActiveSkillNames() {
        // TODO cache results?
        // this is basically /only/ for tab-completion

        Set<String> skillNames = new HashSet<String>();
        for (Role r : this.data.getAllRoles()) {
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
        for (Role r : this.data.getAllRoles()) {
            for (ISkill skill : r.getAllSkills()) {
                skills.add(skill);
            }
        }
        return skills;
    }

    @Override
    public boolean isSkillRestricted(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        if (this.data.primary.isSkillRestricted(skill)) {
            return true;
        } else if (this.data.profession != null && this.data.profession.isSkillRestricted(skill)) {
            return true;
        } else {
            for (Role additional : this.data.additionalRoles) {
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
        return this.data.primary.hasSkillAtLevel(skill, getLevel(this.data.primary));
    }

    @Override
    public boolean doesPrimaryRestrictSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        return this.data.primary.isSkillRestricted(skill);
    }

    @Override
    public boolean canSecondaryUseSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        return this.data.profession != null && this.data.profession.hasSkillAtLevel(skill, getLevel(this.data.profession));
    }

    @Override
    public boolean doesSecondaryRestrictSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        return this.data.profession != null && this.data.profession.isSkillRestricted(skill);
    }

    @Override
    public boolean canAdditionalUseSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        for (Role r : this.data.additionalRoles) {
            if (r.hasSkillAtLevel(skill, getLevel(r))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canSpecificAdditionalUseSkill(Role role, ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        Validate.notNull(role, "Cannot check a null role!");
        return role.hasSkillAtLevel(skill, this.getLevel(role));
    }

    @Override
    public boolean doesAdditionalRestrictSkill(ISkill skill) {
        Validate.notNull(skill, "Cannot check a null Skill!");
        if (this.data.additionalRoles.isEmpty()) {
            return false;
        } else {
            for (Role role : this.data.additionalRoles) {
                if (role.isSkillRestricted(skill)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Stalled getStalledSkill() {
        return this.stalled;
    }

    @Override
    public boolean setStalledSkill(Stalled stalledSkill) {
        if (this.stalled != null && stalledSkill != null) {
            return false;
        }
        // TODO
        this.stalled = stalledSkill;
        return true;
    }

    @Override
    public boolean cancelStalledSkill(boolean forced) {
        return false;
    }

    @Override
    public FixedPoint getExperience(Role role) {
        Validate.notNull(role, "Cannot check a null Role!");
        final FixedPoint exp = this.data.exp.get(role);
        return exp == null ? new FixedPoint() : exp.clone();
    }

    @Override
    public boolean canGainExperience(ExperienceType type) {
        Validate.notNull(type, "Cannot check on a null experience type!");
        if (!this.data.primary.canGainExperience(type)) {
            return false;
        } else if (this.data.profession != null && !this.data.profession.canGainExperience(type)) {
            return false;
        } else if (!this.data.additionalRoles.isEmpty()) {
            for (Role additional : this.data.additionalRoles) {
                if (!additional.canGainExperience(type)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public FixedPoint gainExperience(FixedPoint exp, ExperienceType type, Location location) {
        Validate.notNull(exp, "Cannot gain null experience!");
        Validate.notNull(type, "Cannot gain from a null experience type!");
        Validate.notNull(location, "Cannot gain from a null location!");
        if (this.data.primary.canGainExperience(type)) {
            FixedPoint currentExp = this.data.exp.get(this.data.primary);
            ExperienceChangeEvent event = new ExperienceChangeEvent(this, this.getLocation(), this.data.primary, currentExp, exp);
            if (!event.isCancelled()) {
                this.data.exp.put(this.data.primary, event.getFinalExperience());
            }
        }
        if (this.data.profession.canGainExperience(type)) {
            FixedPoint currentExp = this.data.exp.get(this.data.profession);
            ExperienceChangeEvent event = new ExperienceChangeEvent(this, this.getLocation(), this.data.profession, currentExp, exp);
            if (!event.isCancelled()) {
                this.data.exp.put(this.data.profession, event.getFinalExperience());
            }
        }
        for (Role role : this.data.additionalRoles) {
            if (role.canGainExperience(type)) {
                FixedPoint currentExp = this.data.exp.get(role);
                ExperienceChangeEvent event = new ExperienceChangeEvent(this, this.getLocation(), role, currentExp, exp);
                if (!event.isCancelled()) {
                    this.data.exp.put(role, event.getFinalExperience());
                }
            }
        }
        PlayerUtil.syncronizeExperienceBar(this);
        return exp;
    }

    @Override
    public void loseExperienceFromDeath(double multiplier, boolean byPVP) {
        Validate.isTrue(multiplier < 0, "Cannot use a negative multiplier!");
        // TODO implement
        PlayerUtil.syncronizeExperienceBar(this);

    }

    @Override
    public Role getPrimaryRole() {
        return this.data.primary;
    }

    @Override
    public Role getSecondaryRole() {
        return this.data.profession;
    }

    @Override
    public boolean setPrimaryRole(Role role) {
        Validate.notNull(role, "Cannot set the primary role to null!");
        if (role != null) {
            Validate.isTrue(role.getType() == RoleType.PRIMARY, "Cannot set the primary role type to a different type!");
        }
        this.data.primary = role;
        this.recalculateMaxHealth();
        return true;
    }

    @Override
    public boolean setSecondaryRole(Role role) {
        if (role != null) {
            Validate.isTrue(role.getType() == RoleType.SECONDARY, "Cannot set the secondary role type to a different type!");
        }
        this.data.profession = role;
        this.recalculateMaxHealth();
        return true;
    }

    @Override
    public Set<Role> getAdditionalRoles() {
        return Collections.unmodifiableSet(this.data.additionalRoles);
    }

    @Override
    public boolean addAdditionalRole(Role role) {
        Validate.notNull(role, "Cannot add a null Additional role!");
        Validate.isTrue(role.getType() == RoleType.ADDITIONAL, "Cannot add a different typed role to the Additional roles!");
        if (role.equals(this.data.primary) || role.equals(this.data.profession)) {
            return false;
        }
        this.data.additionalRoles.add(role);
        return true;
    }

    @Override
    public boolean removeAdditionalRole(Role role) {
        Validate.notNull(role, "Cannot remove a null additional role!");
        return this.data.additionalRoles.contains(role) &&
                !this.data.primary.equals(role) &&
                !this.data.profession.equals(role) &&
                this.data.additionalRoles.remove(role);
    }

    @Override
    public List<Role> getAllRoles() {
        ImmutableList.Builder<Role> roleBuilder = ImmutableList.builder();
        roleBuilder.add(this.data.primary);
        roleBuilder.add(this.data.profession);
        for (Role role : this.data.additionalRoles) {
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
        this.check();
        return this.party != null;
    }

    @Override
    public Party getParty() {
        this.check();
        return this.party;
    }

    @Override
    public void setParty(Party party) {
        this.check();
        Validate.notNull(party, "Cannot set the Party to null!");
        this.party = party;
    }

    @Override
    public void leaveParty() {
        this.check();
        this.party = null;
    }

    @Override
    public int getMaxMana() {
        return (this.data.primary.getMaxManaAtLevel(getLevel(this.data.primary)) + this.data.profession.getMaxManaAtLevel(getLevel(this.data.profession)));
    }

    @Override
    public void setMaxMana(int mana) {
        // Implement
        Validate.isTrue(mana > 0, "Cannot set mana to zero or negative!");

    }

    @Override
    public double getMaxHealth() {
        this.check();
        return this.getEntity().getMaxHealth();
    }

    @Override
    public DamageWrapper getDamageWrapper() {
        check();
        return this.damageWrapper;
    }

    @Override
    public void setDamageWrapper(DamageWrapper wrapper) {
        check();
        this.damageWrapper = wrapper;
    }

    @Override
    public int getNoDamageTicks() {
        this.check();
        return this.getPlayer().getNoDamageTicks();
    }

    @Override
    public int getStamina() {
        this.check();
        return this.data.currentStamina;
    }

    @Override
    public int getMaxStamina() {
        this.check();
        return this.data.maxStamina;
    }

    @Override
    public void setStamina(int stamina) {
        Validate.isTrue(stamina >= 0, "Cannot set stamina to a negative value!");
        final int currentMax = getMaxStamina();
        if (stamina > currentMax) {
            stamina = currentMax;
        }
        this.data.currentStamina = stamina;
        updateFoodLevel();
    }

    @Override
    public void modifyStamina(int staminaDiff) {
        this.check();
        this.data.currentStamina += staminaDiff;
        updateFoodLevel();
    }

    private void updateFoodLevel() {
        final double foodPercentage = (this.data.currentStamina / this.data.maxStamina);
        int foodLevel = new Double(20 * foodPercentage).intValue();
        if (foodLevel > 19) { // We cannot allow food to heal players
            foodLevel = 19;
        }
        this.getPlayer().setFoodLevel(foodLevel);
    }

    @Override
    public ItemStack[] getArmor() {
        this.check();

        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < getPlayer().getInventory().getArmorContents().length; i++) {
            armor[i] = new ItemStack(getPlayer().getInventory().getArmorContents()[i]);
        }
        return armor;
    }

    @Override
    public void setArmor(ItemStack item, int armorSlot) throws IllegalArgumentException {
        this.check();
        Validate.isTrue(armorSlot < getPlayer().getInventory().getArmorContents().length, "Cannot set the armor slot greater than the current armor!");
        getPlayer().getInventory().getArmorContents()[armorSlot] = new ItemStack(item);
    }

    @Override
    public boolean canEquipItem(ItemStack itemStack) {
        return itemStack == null ||
                !ItemUtil.isWeapon(itemStack.getType()) ||
                this.data.primary.isWeaponAllowed(itemStack.getType()) ||
                (this.data.profession != null && this.data.profession.isWeaponAllowed(itemStack.getType()));
    }

    public boolean canEquipItem(int itemSlot) {
        ItemStack item = getInventory().getItem(itemSlot);
        if (!canEquipItem(item)) {
            PlayerUtil.moveItem(this, itemSlot, item);
            return false;
        }
        return true;
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
        return this.data.isSkillVerbose || this.data.ignoredSkills.contains(skill.getName());
    }

    @Override
    public final Player getPlayer() {
        return this.getEntity();
    }

    @Override
    public String getDisplayName() {
        check();
        return this.getPlayer().getDisplayName();
    }

    @Override
    public final Player getEntity() {
        return (Player) super.getEntity();
    }

    @Override
    public double recalculateMaxHealth() {
        check();
        // TODO implement
        return 0D;
    }

    @Override
    public void heal(double amount) {
        check();
        // TODO Implement

    }

    @Override
    public boolean isDead() {
        return this.check() && this.getPlayer().isDead();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void updateInventory() {
        if (this.check()) {
            if (getPlayer().getGameMode() == GameMode.CREATIVE || getPlayer().hasPermission(KraftRPGPlugin.ADMIN_INVENTORY_BYPASS_PERMISSION)) {
                return;
            }
            int removed = PlayerUtil.checkArmor(this, getPlayer());
            for (int i = 0; i < 9; i++) {
                if (canEquipItem(i)) {
                    continue;
                }

                removed++;
            }
            if (removed > 0) {
                sendMessage(Messaging.getMessage("inventory_role_item_restriction_moved"), removed);
            }
            this.getPlayer().updateInventory();
        }
    }

    @Override
    public ItemStack getItemInHand() {
        return this.check() ? this.getPlayer().getItemInHand() : null;
    }

    @Override
    public Inventory getInventory() {
        return this.check() ? this.getPlayer().getInventory() : null;
    }

    @Override
    public void sendMessage(String message) {
        check();
        if (this.isEntityValid()) {
            this.getPlayer().sendMessage(message);
        }
    }

    @Override
    public void sendMessage(String message, Object... args) {
        check();
        Messaging.send(this.getEntity(), message, args);
    }

    @Override
    public final void setPlayer(final Player player) {
        Validate.notNull(player, "Cannot set a null player!");
        Validate.isTrue(player.getUniqueId().equals(this.uuid), "Cannot set a different Player object with differing UUID's!");
        this.setEntity(player);
    }

    @Override
    public final boolean setEntity(Player player) {
        return super.setEntity(player);
    }

    @Override
    public PlayerData getData() {
        return this.data;
    }

    @Override
    public PlayerData getDataClone() {
        return this.data.clone();
    }

    private boolean check() {
        if (!this.isValid()) {
            throw new IllegalStateException("A Champion is not linked to a valid Player!");
        }
        return true;
    }
}
