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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Champion;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.spells.SpellUseObject;
import com.afterkraft.kraftrpg.api.util.DamageManager;

/**
 * @author gabizou
 */
public class RPGDamangeManager implements DamageManager {

    private final KraftRPGPlugin plugin;

    private Map<Material, Double> defaultItemDamage;
    private Map<ProjectileType, Double> defaultProjectileDamage;
    private Map<EntityType, Double> defaultCreatureHealth;
    private Map<EntityType, Double> defaultCreatureDamage;
    private Map<EntityDamageEvent.DamageCause, Double> defaultEnvironmentDamage;
    private Map<Enchantment, Double> defaultEnchantmentDamage;
    private final Map<UUID, SpellUseObject> spellTargets = new HashMap<UUID, SpellUseObject>();

    public RPGDamangeManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
    }

    public double getHighestItemDamage(Champion champion, ItemStack item) {
        final double defaultDamage = this.defaultItemDamage.get(item.getType()) != null ? this.defaultItemDamage.get(item.getType()) : 0.0D;
        double roleDamage;
        double primaryDamage = 0.0D;
        double secondaryDamage = 0.0D;
        double activeDamage = 0.0D;
        if (champion.getPrimaryRole() != null) {
            primaryDamage = champion.getPrimaryRole().getItemDamage(item.getType());
            primaryDamage += champion.getPrimaryRole().getItemDamagePerLevel(item.getType()) * champion.getLevel(champion.getPrimaryRole());
        }
        if (champion.getSecondaryRole() != null) {
            secondaryDamage = champion.getSecondaryRole().getItemDamage(item.getType());
            secondaryDamage += champion.getSecondaryRole().getItemDamagePerLevel(item.getType()) * champion.getLevel(champion.getPrimaryRole());
        }
        if (!champion.getAdditionalRoles().isEmpty()) {
            for (Role role : champion.getAdditionalRoles()) {
                double tempRoleDamage = role.getItemDamage(item.getType()) + role.getItemDamagePerLevel(item.getType()) * champion.getLevel(role);
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

    public double getEntityDamage(EntityType type) {
        return this.defaultCreatureDamage.get(type);
    }

    public double getModifiedEntityDamage(final Monster monster, final Location location, final double baseDamage, final boolean fromSpawner) {
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
            double percent = 1 + RPGPluginProperties.mobDamageDistanceModified/100.00D;
            double modifier = Math.pow(percent, MathUtil.getDistance(spawn, location)/RPGPluginProperties.distanceTierModifier)+0.00D;
            modifiedDamage = Math.ceil(modifiedDamage * modifier);
        }
        return modifiedDamage;
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