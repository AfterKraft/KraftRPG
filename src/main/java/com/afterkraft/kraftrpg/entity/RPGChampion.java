package com.afterkraft.kraftrpg.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.spells.Spell;
import com.afterkraft.kraftrpg.api.spells.SpellArgument;
import com.afterkraft.kraftrpg.api.spells.SpellBind;
import com.afterkraft.kraftrpg.util.MathUtil;

/**
 * @author gabizou
 */
public class RPGChampion extends RPGEntity implements Champion {

    private Map<Material, SpellBind> binds = new ConcurrentHashMap<Material, SpellBind>();
    private Role primary;
    private Role secondary;
    private final Set<Role> additionalRoles = new HashSet<Role>();
    private final Map<String, Double> experience = new HashMap<String, Double>();

    protected RPGChampion(RPGPlugin plugin, Player player, Role primary, Role secondary) {
        super(plugin, player, player.getName());
        this.primary = primary;
        this.secondary = secondary;
    }

    public Role getPrimaryRole() {
        return this.primary;
    }

    public void setPrimaryRole(Role role) {
        if (role == null) {
            return;
        }
        this.primary = role;
        this.recalculateMaxHealth();
    }

    public Role getSecondaryRole() {
        return this.secondary;
    }

    public void setSecondaryRole(Role role) {
        if (role == null) {
            return;
        }
        this.secondary = role;
        this.recalculateMaxHealth();
    }

    public Set<Role> getAdditionalRoles() {
        return Collections.unmodifiableSet(this.additionalRoles);
    }

    public boolean addAdditionalRole(Role role) {
        // TODO employ some sort of Role interference logic
        return (role != null) &&
                !(this.additionalRoles.contains(role)) &&
                !(this.primary.equals(role) || this.secondary.equals(role)) &&
                this.additionalRoles.add(role);
    }

    public boolean removeAdditionalRole(Role role) {
        // TODO employ some sort of Role interference logic
        return (role != null) &&
                (this.additionalRoles.contains(role)) &&
                !this.primary.equals(role) &&
                !this.secondary.equals(role) &&
                this.additionalRoles.remove(role);
    }

    public int getLevel(Role role) {
        return MathUtil.getLevel(this.getExperience(role));
    }

    public double getExperience(Role role) {
        if (role == null) {
            return 0;
        }
        final Double exp = this.experience.get(role.getName());
        return exp == null ? 0 : exp;
    }

    public boolean canPrimaryUseSpell(Spell<? extends SpellArgument> spell) {
        return false;
    }

    public boolean canSecondaryUseSpell(Spell<? extends SpellArgument> spell) {
        return false;
    }

    public double recalculateMaxHealth() {
        return 0D;
    }

    public final Player getPlayer() {
        return (Player) this.getEntity();
    }

    public final void setPlayer(final Player player) {
        this.setEntity(player);
    }
}
