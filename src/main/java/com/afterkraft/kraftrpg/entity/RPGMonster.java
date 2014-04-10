package com.afterkraft.kraftrpg.entity;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.handler.EntityAttribute;

/**
 * @author gabizou
 */
public class RPGMonster extends RPGEntity implements Monster {

    private final EntityAttribute damageAttribute = new EntityAttribute("Damage", EntityAttribute.EntityAttributeType.DAMAGE);
    private final EntityAttribute expAttribute = new EntityAttribute("Experience", EntityAttribute.EntityAttributeType.EXPERIENCE);
    private final EntityAttribute spawnxAttribute = new EntityAttribute("Damage", EntityAttribute.EntityAttributeType.SPAWNX);
    private final EntityAttribute spawnyAttribute = new EntityAttribute("Damage", EntityAttribute.EntityAttributeType.SPAWNY);
    private final EntityAttribute spawnzAttribute = new EntityAttribute("Damage", EntityAttribute.EntityAttributeType.SPAWNZ);
    private final EntityAttribute reasonattribute = new EntityAttribute("Damage", EntityAttribute.EntityAttributeType.FROMSPAWNER);

    private int experience = -1;
    private double baseDamage = 0;
    private double damage = 0;
    private SpawnReason spawnReason = null;

    private Location spawnPoint;

    public RPGMonster(RPGPlugin plugin, LivingEntity entity, boolean fromSpawner) {
        this(plugin, entity, entity.getCustomName(), fromSpawner);
    }

    protected RPGMonster(RPGPlugin plugin, LivingEntity entity, String name, boolean fromSpawner) {
        super(plugin, entity, name);
        this.spawnReason = fromSpawner ? SpawnReason.SPAWNER : SpawnReason.NATURAL;

        Location location = entity.getLocation();
        double spawnx = this.spawnxAttribute.loadOrCreate(entity, location.getX());
        double spawny = this.spawnxAttribute.loadOrCreate(entity, location.getX());
        double spawnz = this.spawnxAttribute.loadOrCreate(entity, location.getX());
        location = this.spawnPoint = new Location(location.getWorld(), spawnx, spawny, spawnz);
        final double configuredDamage = plugin.getDamageManager().getEntityDamage(entity.getType());
        this.damage = plugin.getDamageManager().getModifiedEntityDamage(this, location, configuredDamage, fromSpawner);
        this.baseDamage = this.damageAttribute.loadOrCreate(entity, configuredDamage);

    }

    public Location getSpawnLocation() {
        return spawnPoint;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

    public double getModifiedDamage() {
        return this.damage;
    }

    public void setModifiedDamage(double damage) {
        this.damage = damage > 0 ? damage : 1;
    }


}
