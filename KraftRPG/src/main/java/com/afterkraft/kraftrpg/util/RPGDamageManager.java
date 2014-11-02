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
package com.afterkraft.kraftrpg.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Insentient;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.Sentient;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.skills.SkillUseObject;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.api.util.Utilities;

/**
 * Standard Implementation of DamageManager
 */
public class RPGDamageManager implements DamageManager {

    private final RPGPlugin plugin;
    private final Map<UUID, SkillUseObject> skillTargets = new HashMap<UUID, SkillUseObject>();
    private Map<Material, Double> defaultItemDamage;
    private Map<ProjectileType, Double> defaultProjectileDamage;
    private Map<EntityType, Double> defaultCreatureHealth;
    private Map<EntityType, Double> defaultCreatureDamage;
    private Map<EntityDamageEvent.DamageCause, Double> defaultEnvironmentDamage;
    private Map<Enchantment, Double> defaultEnchantmentDamage;

    public RPGDamageManager(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public double getHighestItemDamage(Insentient attacker, Insentient defender,
                                       double defaultDamage) {
        final ItemStack weapon = attacker.getItemInHand();

        Double tmpDamage = getHighestItemDamage(attacker, defaultDamage);
        if (tmpDamage != null && defender instanceof LivingEntity) {
            tmpDamage += getExtraDamage(weapon, (LivingEntity) defender);
        }
        return tmpDamage == null ? defaultDamage : tmpDamage;
    }

    private double getHighestItemDamage(Insentient being, double defaultDamage) {
        if (being instanceof Sentient) {
            return getHighestItemDamage((Sentient) being, defaultDamage);
        } else if (being instanceof Monster) {
            return getHighestMonsterDamage((Monster) being, defaultDamage);

        } else {
            return getDefaultItemDamage(being.getItemInHand().getType());
        }
    }

    private double getExtraDamage(ItemStack item, LivingEntity target) {
        if (!Utilities.isStandardWeapon(item.getType())) {
            return 0;
        }
        int amount = 0;

        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            Double val = getEnchantmentDamage(entry.getKey(), entry.getValue());
            if (val == null) {
                continue;
            }
            boolean extraDamage;
            Enchantment id = entry.getKey();
            switch (target.getType()) {
                case CAVE_SPIDER:
                case SPIDER:
                case SILVERFISH:
                    extraDamage =
                            id == Enchantment.DAMAGE_ARTHROPODS || id == Enchantment.DAMAGE_ALL;
                    break;
                case ZOMBIE:
                case SKELETON:
                case PIG_ZOMBIE:
                case WITHER:
                    extraDamage = id == Enchantment.DAMAGE_UNDEAD || id == Enchantment.DAMAGE_ALL;
                    break;
                default:
                    extraDamage = id == Enchantment.DAMAGE_ALL || (id == Enchantment.FIRE_ASPECT
                            && !target.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE));
                    break;
            }
            if (extraDamage) {
                amount += getEnchantmentDamage(entry.getKey(), entry.getValue());
            }
        }

        return amount;
    }

    private double getHighestItemDamage(Sentient being, double damage) {
        final ItemStack item = being.getItemInHand();
        final double defaultDamage = this.defaultItemDamage.get(item.getType()) != null
                ? this.defaultItemDamage.get(item.getType()) : damage;
        double roleDamage;
        double primaryDamage = 0.0D;
        double secondaryDamage = 0.0D;
        double activeDamage = 0.0D;
        if (being.getPrimaryRole() != null) {
            primaryDamage = being.getPrimaryRole().getItemDamage(item.getType());
            primaryDamage += being.getPrimaryRole().getItemDamagePerLevel(item.getType()) * being
                    .getLevel(being.getPrimaryRole());
        }
        if (being.getSecondaryRole() != null) {
            secondaryDamage = being.getSecondaryRole().getItemDamage(item.getType());
            secondaryDamage +=
                    being.getSecondaryRole().getItemDamagePerLevel(item.getType()) * being
                            .getLevel(being.getPrimaryRole());
        }
        if (!being.getAdditionalRoles().isEmpty()) {
            for (Role role : being.getAdditionalRoles()) {
                double tempRoleDamage = role.getItemDamage(item.getType())
                        + role.getItemDamagePerLevel(item.getType()) * being.getLevel(role);
                activeDamage = tempRoleDamage > activeDamage ? tempRoleDamage : activeDamage;
            }
        }
        if (primaryDamage == 0.0D && secondaryDamage == 0.0D && activeDamage == 0.0D) {
            roleDamage = defaultDamage;
        } else {
            roleDamage = Math.max(Math.max(primaryDamage, secondaryDamage), activeDamage);
        }
        return roleDamage > 0 ? roleDamage : defaultDamage;
    }

    private double getHighestMonsterDamage(Monster monster, double defaultDamage) {
        return monster.getModifiedDamage() >= 0 ? monster.getModifiedDamage() : defaultDamage;
    }

    @Override
    public double getHighestProjectileDamage(Insentient champion, ProjectileType type) {
        return 0;
    }

    @Override
    public double getDefaultItemDamage(Material type, double damage) {
        return 0;
    }

    @Override
    public double getDefaultItemDamage(Material type) {
        return 0;
    }

    @Override
    public void setDefaultItemDamage(Material type, double damage) {

    }

    @Override
    public boolean doesItemDamageVary(Material type) {
        return false;
    }

    @Override
    public void setItemDamageVarying(Material type, boolean isVarying) {

    }

    @Override
    public double getEntityDamage(EntityType type) {
        return this.defaultCreatureDamage.get(type);
    }

    @Override
    public double getEnvironmentalDamage(EntityDamageEvent.DamageCause cause) {
        return 0;
    }

    @Override
    public double getEnchantmentDamage(Enchantment enchantment, int enchantmentLevel) {
        return 0;
    }

    @Override
    public double getItemEnchantmentDamage(Insentient being, Enchantment enchantment,
                                           ItemStack item) {
        return 0;
    }

    @Override
    public double getFallReduction(Insentient being) {
        return 0;
    }

    @Override
    public double getModifiedEntityDamage(final Monster monster, final Location location,
                                          final double baseDamage,
                                          final CreatureSpawnEvent.SpawnReason fromSpawner) {
        if (monster == null || !monster.isEntityValid()) {
            return 0D;
        }
        Location spawn = monster.getSpawnLocation();
        double modifiedDamage = 1D;
        LivingEntity entity = monster.getEntity();
        if (entity instanceof Slime) {
            final Slime slime = (Slime) entity;
            switch (slime.getSize()) {
                case 1:
                    if (slime instanceof MagmaCube) {
                        modifiedDamage -= baseDamage / 3;
                    } else {
                        modifiedDamage = 0;
                        break;
                    }
                    break;
                case 2:
                    modifiedDamage -= baseDamage / 3;
                    break;
                default:
                    break;
            }
        }
        if (this.plugin.getProperties().isMobDamageDistanceModified()) {
            double percent = 1 + RPGPluginProperties.mobDamageDistanceModified / 100.00D;
            double modifier = Math.pow(percent, MathUtil.getModulatedDistance(spawn, location)
                    / RPGPluginProperties.distanceTierModifier) + 0.00D;
            modifiedDamage = Math.ceil(modifiedDamage * modifier);
        }
        return modifiedDamage;
    }

    @Override
    public double getDefaultEntityHealth(LivingEntity entity) {
        final Double val = this.defaultCreatureHealth.get(entity.getType());
        return val != null ? val : entity.getMaxHealth();
    }

    @Override
    public double getModifiedEntityHealth(LivingEntity entity) {
        return 0;
    }

    @Override
    public boolean doesEntityDealVaryingDamage(EntityType type) {
        return false;
    }

    @Override
    public void setEntityToDealVaryingDamage(EntityType type, boolean dealsVaryingDamage) {

    }

    @Override
    public boolean isStandardWeapon(Material material) {
        return false;
    }

    @Override
    public void load(Configuration config) {
        Set<String> keys;

        this.defaultCreatureHealth = new EnumMap<EntityType, Double>(EntityType.class);
        ConfigurationSection section = config.getConfigurationSection("creature-health");
        boolean error = false;
        if (section != null) {
            keys = section.getKeys(false);
            if (keys != null) {
                for (final String key : keys) {
                    try {
                        final EntityType type = EntityType.valueOf(key.toUpperCase(Locale.ENGLISH));
                        if (type == null) {
                            throw new IllegalArgumentException();
                        }

                        double health = section.getDouble(key, 20);
                        if (health <= 0) {
                            health = 20;
                        }
                        this.defaultCreatureHealth.put(type, health);

                    } catch (final IllegalArgumentException e) {
                        this.plugin.log(Level.WARNING,
                                        "Invalid creature type (" + key
                                                + ") found in damages.yml.");
                        error = true;
                    }
                }
            }
        }

        this.defaultCreatureDamage = new EnumMap<EntityType, Double>(EntityType.class);
        section = config.getConfigurationSection("creature-damage");
        if (section != null) {
            keys = section.getKeys(false);
            if (keys != null) {
                for (final String key : keys) {
                    try {
                        final EntityType type = EntityType.valueOf(key.toUpperCase(Locale.ENGLISH));
                        if (type == null) {
                            throw new IllegalArgumentException();
                        }

                        this.defaultCreatureDamage.put(type, section.getDouble(key, 10));
                    } catch (final IllegalArgumentException e) {
                        this.plugin.log(Level.WARNING,
                                        "Invalid creature type (" + key
                                                + ") found in damages.yml.");
                        error = true;
                    }
                }
            }
        }
        if (error) {
            this.plugin.log(Level.WARNING,
                            "Remember, creature-names are case-sensetive, and must be "
                                    + "exactly the same as found in the defaults!");
        }

        this.defaultItemDamage = new EnumMap<Material, Double>(Material.class);
        section = config.getConfigurationSection("item-damage");
        if (section != null) {
            keys = section.getKeys(false);
            if (keys != null) {
                for (final String key : keys) {
                    try {
                        final Material item = Material.matchMaterial(key);
                        if (item == null) {
                            throw new IllegalArgumentException();
                        }

                        this.defaultItemDamage.put(item, section.getDouble(key, 2));
                    } catch (final IllegalArgumentException e) {
                        this.plugin.log(Level.WARNING,
                                        "Invalid item type (" + key + ") found in damages.yml.");
                    }
                }
            }
        }

        this.defaultEnvironmentDamage = new EnumMap<EntityDamageEvent.DamageCause, Double>(
                EntityDamageEvent.DamageCause.class);
        section = config.getConfigurationSection("environmental-damage");
        if (section != null) {
            keys = section.getKeys(false);
            if (keys != null) {
                for (final String key : keys) {
                    try {
                        final EntityDamageEvent.DamageCause cause =
                                EntityDamageEvent.DamageCause.valueOf(key.toUpperCase());
                        if (cause == null) {
                            throw new IllegalArgumentException();
                        }
                        final double damage = section.getDouble(key, 0.0);
                        this.defaultEnvironmentDamage.put(cause, damage);
                    } catch (final IllegalArgumentException e) {
                        this.plugin.log(Level.WARNING, "Invalid environmental damage type (" + key
                                + ") found in damages.yml");
                    }
                }
            }
        }

        this.defaultProjectileDamage = new EnumMap<ProjectileType, Double>(ProjectileType.class);
        section = config.getConfigurationSection("projectile-damage");
        if (section != null) {
            keys = section.getKeys(false);
            if (keys != null) {
                for (final String key : keys) {
                    final ProjectileType type = ProjectileType.valueOf(key.toUpperCase());
                    if (type == null) {
                        continue;
                    }
                    this.defaultProjectileDamage.put(type, section.getDouble(key, 0));
                }
            }
        }

        this.defaultEnchantmentDamage = new HashMap<Enchantment, Double>();
        section = config.getConfigurationSection("enchantment-damage");
        if (section != null) {
            keys = section.getKeys(false);
            if (keys != null) {
                for (final String key : keys) {
                    final Enchantment enchant = Enchantment.getByName(key);
                    if (enchant == null) {
                        continue;
                    }
                    this.defaultEnchantmentDamage.put(enchant, section.getDouble(key, 0));
                }
            }
        }
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}
