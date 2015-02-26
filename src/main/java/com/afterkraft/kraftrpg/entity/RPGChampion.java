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
package com.afterkraft.kraftrpg.entity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.party.Party;
import com.afterkraft.kraftrpg.api.listeners.DamageWrapper;
import com.afterkraft.kraftrpg.api.roles.ExperienceType;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.Role.RoleType;
import com.afterkraft.kraftrpg.api.roles.aspects.SkillAspect;
import com.afterkraft.kraftrpg.api.skills.Active;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.skills.Stalled;
import com.afterkraft.kraftrpg.api.storage.PlayerData;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.afterkraft.kraftrpg.util.Messaging;
import com.afterkraft.kraftrpg.util.PlayerUtil;

/**
 * Standard implementation of a Champion that is linked to a Player. Only
 * Players should be considered Champions. If a custom entity is NOT a Player,
 * do NOT use Champion, use an extension of RPGInsentient implements SkillCaster
 * instead.
 */
public class RPGChampion extends RPGInsentient implements Champion {
    public static final String GLOBAL_COOLDOWN_KEY = "global";
    private PlayerData data;
    private Stalled stalled;
    @Nullable
    private transient Party party;
    @Nullable
    private transient DamageWrapper damageWrapper;

    protected RPGChampion(RPGPlugin plugin, Player player, PlayerData data) {
        super(plugin, player, player.getName());
        this.data = data;
    }

    @Override
    public Optional<Party> getParty() {
        this.check();
        return Optional.fromNullable(this.party);
    }

    private boolean check() {
        if (!this.isValid()) {
            throw new IllegalStateException(
                    "A Champion is not linked to a valid Player!");
        }
        return true;
    }

    @Override
    public void setParty(Party party) {
        this.check();
        checkArgument(party != null, "Cannot set the Party to null!");
        this.party = party;
    }

    @Override
    public Optional<Long> getCooldown(String key) {
        checkNotNull(key, "Cannot get a null cooldown!");
        checkArgument(!key.isEmpty(), "Cannot get an empty keyed cooldown!");
        if (this.data.cooldowns.containsKey(key)) {
            return Optional.of(this.data.cooldowns.get(key));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public void leaveParty() {
        this.check();
        this.party = null;
    }

    @Override
    public int getMaxMana() {
        return 0; // TODO
    }

    @Override
    public void setMaxMana(int mana) {
        // Implement
        checkArgument(mana > 0, "Cannot set mana to zero or negative!");

    }

    @Override
    public double getMaxHealth() {
        return 20;
    }

    @Override
    public long getGlobalCooldown() {
        return getCooldown(RPGChampion.GLOBAL_COOLDOWN_KEY).get();
    }

    @Override
    public Optional<DamageWrapper> getDamageWrapper() {
        check();
        return Optional.absent();
    }

    @Override
    public void setDamageWrapper(DamageWrapper wrapper) {
        check();
        this.damageWrapper = wrapper;
    }

    @Override
    public int getNoDamageTicks() {
        this.check();
        return this.getPlayer().get().getInvulnerabilityTicks();
    }

    @Override
    public int getStamina() {
        this.check();
        return this.data.currentStamina;
    }

    @Override
    public void setGlobalCooldown(long duration) {
        checkArgument(duration < Long.MAX_VALUE - System.currentTimeMillis(),
                      "Cannot set the duration longer than the maximum time of the system!");
        this.data.cooldowns.put(RPGChampion.GLOBAL_COOLDOWN_KEY, duration);
    }

    @Override
    public void setStamina(int stamina) {
        checkArgument(stamina >= 0, "Cannot set stamina to a negative value!");
        final int currentMax = getMaxStamina();
        if (stamina > currentMax) {
            stamina = currentMax;
        }
        this.data.currentStamina = stamina;
    }

    @Override
    public int getMaxStamina() {
        this.check();
        return this.data.maxStamina;
    }

    @Override
    public void modifyStamina(int staminaDiff) {
        this.check();
        this.data.currentStamina += staminaDiff;
    }

    @Override
    public ItemStack[] getArmor() {
        this.check();
        return new ItemStack[]{};
    }

    @Override
    public void setCooldown(String key, long duration) {
        checkNotNull(key, "Cannot set the cooldown of a null key!");
        checkArgument(!key.isEmpty(),
                      "Cannot set the cooldown of an empty key!");
        checkArgument(duration < Long.MAX_VALUE - System.currentTimeMillis(),
                      "Cannot set the duration longer than the maximum time of the system!");
        this.data.cooldowns.put(key, duration);
    }

    @Override
    public void setArmor(ItemStack item, int armorSlot) {
        // TODO
    }

    @Override
    public boolean canEquipItem(ItemStack itemStack) {
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
    public Optional<Integer> getHighestSkillLevel(ISkill skill) {
        checkNotNull(skill, "Cannot get the highest null skill level!");
        int level = 0;

        for (Role r : this.data.getAllRoles()) {
            Optional<Integer> optional = getLevel(r);
            if (optional.isPresent()) {
                if (r.getAspect(SkillAspect.class).isPresent()) {
                    SkillAspect aspect = r.getAspect(SkillAspect.class).get();
                    int roleLevel = optional.get();
                    if (aspect.hasSkillAtLevel(skill, roleLevel)) {
                        if (roleLevel > level) {
                            level = roleLevel;
                        }
                    }
                }
            }
        }
        if (level == 0) {
            return Optional.absent();
        }
        return Optional.of(level);
    }

    @Override
    public void sendMessage(String message, Object... args) {
        check();
        Messaging.send(this.getPlayer().get(), message, args);
    }

    @Override
    public boolean isIgnoringSkill(ISkill skill) {
        return this.data.isSkillVerbose || this.data.ignoredSkills
                .contains(skill.getName());
    }

    @Override
    public final Optional<Player> getPlayer() {
        return this.getEntity();
    }

    @Override
    public boolean canUseSkill(ISkill skill) {
        checkNotNull(skill,
                     "Cannot check whether Champion can use null skill!");
        for (Role r : this.data.getAllRoles()) {
            int roleLevel = getLevel(r).get();
            if (r.getAspect(SkillAspect.class).isPresent()) {
                SkillAspect aspect = r.getAspect(SkillAspect.class).get();
                if (aspect.hasSkillAtLevel(skill, roleLevel)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public final void setPlayer(final Player player) {
        checkArgument(player != null, "Cannot set a null player!");
        checkArgument(player.getUniqueId().equals(this.uuid),
                      "Cannot set a different Player object with differing UUID's!");
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

    @Override
    public Collection<ISkill> getAvailableSkills() {
        ImmutableSet.Builder<ISkill> skills = ImmutableSet.builder();
        for (Role r : this.data.getAllRoles()) {
            if (r.getAspect(SkillAspect.class).isPresent()) {
                SkillAspect aspect = r.getAspect(SkillAspect.class).get();
                skills.addAll(aspect.getAllSkillsAtLevel(getLevel(r).get()));
            }
        }
        return skills.build();
    }

    @Override
    Player getUnsafeEntity() {
        return (Player) super.getUnsafeEntity();
    }

    @Override
    public final Optional<Player> getEntity() {
        return Optional.of(this.getUnsafeEntity());
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
    public Collection<String> getActiveSkillNames() {
        // TODO cache results?
        // this is basically /only/ for tab-completion

        Set<String> skillNames = new HashSet<>();
        for (Role r : this.data.getAllRoles()) {
            Optional<SkillAspect> optional = r.getAspect(SkillAspect.class);
            if (optional.isPresent()) {
                for (ISkill skill : optional.get().getAllSkillsAtLevel
                        (getLevel(r).get()
                        )) {
                    if (skill instanceof Active) {
                        skillNames.add(skill.getName());
                    }
                }
            }
        }
        return skillNames;
    }

    @Override
    public boolean isDead() {
        return this.check() && this.getPlayer().get().getHealth() <= 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void updateInventory() {
        // TODO
    }

    @Override
    public Optional<ItemStack> getItemInHand() {
        return this.check() ? this.getPlayer().get().getItemInHand()
                : Optional.<ItemStack>absent();
    }

    @Override
    public Inventory getInventory() {
        return this.getPlayer().get().getInventory();
    }

    @Override
    public Collection<ISkill> getPossibleSkillsInRoles() {
        ImmutableSet.Builder<ISkill> skills = ImmutableSet.builder();
        for (Role r : this.data.getAllRoles()) {
            if (r.getAspect(SkillAspect.class).isPresent()) {
                SkillAspect aspect = r.getAspect(SkillAspect.class).get();
                skills.addAll(aspect.getAllSkills());
            }
        }
        return skills.build();
    }

    @Override
    public void sendMessage(String message) {
        check();
        if (this.isEntityValid()) {
            this.getPlayer().get().sendMessage(message);
        }
    }

    @Override
    public Optional<FixedPoint> getExperience(Role role) {
        checkNotNull(role, "Cannot check a null Role!");
        final FixedPoint exp = this.data.exp.get(role);
        return exp == null
                ? Optional.<FixedPoint>absent()
                : Optional.of(exp.clone());
    }

    @Override
    public boolean canGainExperience(ExperienceType type) {
        checkNotNull(type, "Cannot check on a null experience type!");
        return true;
    }

    @Override
    public boolean isSkillRestricted(ISkill skill) {
        checkNotNull(skill, "Cannot check a null Skill!");

        return false;
    }

    @Override
    public FixedPoint gainExperience(FixedPoint exp, ExperienceType type,
                                     Location location) {
        return null;
    }

    @Override
    public void loseExperienceFromDeath(double multiplier, boolean byPVP) {
        checkArgument(multiplier < 0, "Cannot use a negative multiplier!");
        // TODO implement
        PlayerUtil.syncronizeExperienceBar(this);

    }

    @Override
    public Optional<Role> getPrimaryRole() {
        return Optional.of(this.data.primary);
    }

    @Override
    public Optional<Role> getSecondaryRole() {
        return Optional.of(this.data.profession);
    }

    @Override
    public boolean canPrimaryUseSkill(ISkill skill) {
        checkNotNull(skill, "Cannot check a null Skill!");
        return true;
    }

    @Override
    public boolean setPrimaryRole(@Nullable Role role) {
        if (role != null) {
            checkArgument(role.getType() == RoleType.PRIMARY,
                          "Cannot set the primary role type to a different type!");
        }
        this.data.primary = role;
        this.recalculateMaxHealth();
        return true;
    }

    @Override
    public boolean setSecondaryRole(@Nullable Role role) {
        if (role != null) {
            checkArgument(role.getType() == RoleType.SECONDARY,
                          "Cannot set the secondary role type to a different type!");
        }
        this.data.profession = role;
        this.recalculateMaxHealth();
        return true;
    }

    @Override
    public Set<Role> getAdditionalRoles() {
        return ImmutableSet.<Role>builder().addAll(this.data.additionalRoles)
                .build();
    }

    @Override
    public boolean addAdditionalRole(Role role) {
        checkNotNull(role, "Cannot add a null Additional role!");
        checkArgument(role.getType() == RoleType.ADDITIONAL,
                      "Cannot add a different typed role to the Additional roles!");
        if (role.equals(this.data.primary) || role
                .equals(this.data.profession)) {
            return false;
        }
        this.data.additionalRoles.add(role);
        return true;
    }

    @Override
    public boolean doesPrimaryRestrictSkill(ISkill skill) {
        checkNotNull(skill, "Cannot check a null Skill!");
        return false; // TODO
    }

    @Override
    public boolean removeAdditionalRole(Role role) {
        checkNotNull(role, "Cannot remove a null additional role!");
        return this.data.additionalRoles.contains(role)
                && !this.data.primary.equals(role)
                && !this.data.profession.equals(role)
                && this.data.additionalRoles.remove(role);
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
    public Optional<Integer> getLevel(Role role) {
        checkNotNull(role, "Cannot get the experience of a null Role!");
        return Optional.of(1);
    }

    @Override
    public String getDisplayName() {
        check();
        return this.getUnsafeEntity().getDisplayName().toString();
    }

    @Override
    public boolean canSecondaryUseSkill(ISkill skill) {
        checkNotNull(skill, "Cannot check a null Skill!");
        return false; // TODO
    }

    @Override
    public boolean doesSecondaryRestrictSkill(ISkill skill) {
        checkNotNull(skill, "Cannot check a null Skill!");
        return false; // TODO
    }

    @Override
    public boolean canAdditionalUseSkill(ISkill skill) {
        checkNotNull(skill, "Cannot check a null Skill!");
        // TODO
        return false;
    }

    @Override
    public boolean canSpecificAdditionalUseSkill(Role role, ISkill skill) {
        checkNotNull(skill, "Cannot check a null Skill!");
        checkNotNull(role, "Cannot check a null role!");
        return role.getAspect(SkillAspect.class).isPresent() &&
                role.getAspect(SkillAspect.class).get()
                        .hasSkillAtLevel(skill, this.getLevel(role).get());
    }

    @Override
    public boolean doesAdditionalRestrictSkill(ISkill skill) {
        checkNotNull(skill, "Cannot check a null Skill!");
        if (this.data.additionalRoles.isEmpty()) {
            return true;
        } else {
            for (Role role : this.data.additionalRoles) {
                /*
                if (role.isSkillRestricted(skill)) {

                    return true;
                }
                */
            }
            return true;
        }
    }


    @Override
    public Stalled getStalledSkill() {
        return this.stalled;
    }

    @Override
    public boolean setStalledSkill(Stalled stalledSkill) {
        checkNotNull(stalledSkill);
        // TODO
        this.stalled = stalledSkill;
        return true;
    }

    @Override
    public boolean cancelStalledSkill(boolean forced) {
        return false;
    }

}
