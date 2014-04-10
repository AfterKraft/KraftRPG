package com.afterkraft.kraftrpg.entity.roles;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.entity.roles.RoleState;
import com.afterkraft.kraftrpg.api.spells.Spell;
import com.afterkraft.kraftrpg.api.spells.SpellArgument;

/**
 * @author gabizou
 */
public class RPGRole implements Role {

    private final RPGPlugin plugin;
    private final String name;
    private final RoleState state;

    private final Map<Material, Double> itemDamages = new EnumMap<Material, Double>(Material.class);
    private final Map<Material, Double> itemDamagePerLevel = new EnumMap<Material, Double>(Material.class);

    public RPGRole(RPGPlugin plugin, String name, RoleState state) {
        this.plugin = plugin;
        this.name = name;
        this.state = state;
    }

    public final RoleState getState() {
        return this.state;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasSpell(Spell<? extends SpellArgument> spell) {
        return false;
    }

    public boolean hasSpell(String name) {
        return false;
    }

    public double getItemDamage(Material type) {
        return this.itemDamages.get(type) != null ? this.itemDamages.get(type) : 0.0D;
    }

    public void setItemDamage(Material type, double damage) {
        this.itemDamages.put(type, damage);
    }

    public double getItemDamagePerLevel(Material type) {
        return this.itemDamagePerLevel.get(type) != null ? this.itemDamagePerLevel.get(type) : 0.0D;
    }

    public void setItemDamagePerLevel(Material type, double damage) {
        this.itemDamagePerLevel.put(type, damage);
    }

}
