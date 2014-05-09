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

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.SkillCaster;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.skills.SkillUseObject;
import com.afterkraft.kraftrpg.api.util.DamageManager;


public class RPGDamageManager implements DamageManager {

    private final KraftRPGPlugin plugin;
    private final Map<UUID, SkillUseObject> skillTargets = new HashMap<UUID, SkillUseObject>();
    private Map<Material, Double> defaultItemDamage;
    private Map<ProjectileType, Double> defaultProjectileDamage;
    private Map<EntityType, Double> defaultCreatureHealth;
    private Map<EntityType, Double> defaultCreatureDamage;
    private Map<EntityDamageEvent.DamageCause, Double> defaultEnvironmentDamage;
    private Map<Enchantment, Double> defaultEnchantmentDamage;

    public RPGDamageManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
    }

    public double getHighestItemDamage(SkillCaster caster, ItemStack item) {
        final double defaultDamage = this.defaultItemDamage.get(item.getType()) != null ? this.defaultItemDamage.get(item.getType()) : 0.0D;
        double roleDamage;
        double primaryDamage = 0.0D;
        double secondaryDamage = 0.0D;
        double activeDamage = 0.0D;
        if (caster.getPrimaryRole() != null) {
            primaryDamage = caster.getPrimaryRole().getItemDamage(item.getType());
            primaryDamage += caster.getPrimaryRole().getItemDamagePerLevel(item.getType()) * caster.getLevel(caster.getPrimaryRole());
        }
        if (caster.getSecondaryRole() != null) {
            secondaryDamage = caster.getSecondaryRole().getItemDamage(item.getType());
            secondaryDamage += caster.getSecondaryRole().getItemDamagePerLevel(item.getType()) * caster.getLevel(caster.getPrimaryRole());
        }
        if (!caster.getAdditionalRoles().isEmpty()) {
            for (Role role : caster.getAdditionalRoles()) {
                double tempRoleDamage = role.getItemDamage(item.getType()) + role.getItemDamagePerLevel(item.getType()) * caster.getLevel(role);
                activeDamage = tempRoleDamage > activeDamage ? tempRoleDamage : activeDamage;
            }
        }
        if (primaryDamage == 0.0D && secondaryDamage == 0.0D && activeDamage == 0.0D) {
            roleDamage = defaultDamage;
        } else {
            roleDamage = Math.max(Math.max(primaryDamage, secondaryDamage), activeDamage);
        }
        return roleDamage;
    }

    @Override
    public double getHighestProjectileDamage(Champion champion, ProjectileType type) {
        return 0;
    }

    public double getEntityDamage(EntityType type) {
        return this.defaultCreatureDamage.get(type);
    }

    @Override
    public double getEnvironmentalDamage(EntityDamageEvent.DamageCause cause) {
        return 0;
    }

    @Override
    public double getEnchantmentDamage(Enchantment enchantment) {
        return 0;
    }

    public double getModifiedEntityDamage(final Monster monster, final Location location, final double baseDamage, final CreatureSpawnEvent.SpawnReason fromSpawner) {
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
                case 2:
                    modifiedDamage -= baseDamage / 3;
                default:
                    break;
            }
        }
        if (plugin.getProperties().isMobDamageDistanceModified()) {
            double percent = 1 + RPGPluginProperties.mobDamageDistanceModified / 100.00D;
            double modifier = Math.pow(percent, MathUtil.getModulatedDistance(spawn, location) / RPGPluginProperties.distanceTierModifier) + 0.00D;
            modifiedDamage = Math.ceil(modifiedDamage * modifier);
        }
        return modifiedDamage;
    }

    @Override
    public double getDefaultEntityHealth(LivingEntity entity) {
        final Double val = defaultCreatureHealth.get(entity.getType());
        return val != null ? val : entity.getMaxHealth();
    }

    @Override
    public double getModifiedEntityHealth(LivingEntity entity) {
        return 0;
    }


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
                        plugin.log(Level.WARNING, "Invalid creature type (" + key + ") found in damages.yml.");
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
                        plugin.log(Level.WARNING, "Invalid creature type (" + key + ") found in damages.yml.");
                        error = true;
                    }
                }
            }
        }
        if (error) {
            plugin.log(Level.WARNING, "Remember, creature-names are case-sensetive, and must be exactly the same as found in the defaults!");
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
                        plugin.log(Level.WARNING, "Invalid item type (" + key + ") found in damages.yml.");
                    }
                }
            }
        }

        this.defaultEnvironmentDamage = new EnumMap<EntityDamageEvent.DamageCause, Double>(EntityDamageEvent.DamageCause.class);
        section = config.getConfigurationSection("environmental-damage");
        if (section != null) {
            keys = section.getKeys(false);
            if (keys != null) {
                for (final String key : keys) {
                    try {
                        final EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.valueOf(key.toUpperCase());
                        if (cause == null) {
                            throw new IllegalArgumentException();
                        }
                        final double damage = section.getDouble(key, 0.0);
                        this.defaultEnvironmentDamage.put(cause, damage);
                    } catch (final IllegalArgumentException e) {
                        plugin.log(Level.WARNING, "Invalid environmental damage type (" + key + ") found in damages.yml");
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
