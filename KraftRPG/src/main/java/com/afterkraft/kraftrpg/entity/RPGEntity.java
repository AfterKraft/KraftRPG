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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.IEntity;

/**
 * Default implementation of IEntity
 */
public class RPGEntity implements IEntity {

    protected final RPGPlugin plugin;
    protected final String name;
    protected final UUID uuid;
    protected WeakReference<Entity> weakEntity;

    public RPGEntity(RPGPlugin plugin, Entity weakEntity, String name) {
        Validate.notNull(plugin, "Cannot create an RPGEntity with a null plugin!");
        Validate.notNull(weakEntity, "Cannot create an RPGEntity with a null Entity!");
        this.plugin = plugin;
        this.weakEntity = new WeakReference<>(weakEntity);
        this.name = name != null ? name : weakEntity.getType().name();
        Validate.notNull(this.name, "Failed to create a name for this entity!");
        this.uuid = weakEntity.getUniqueId();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.getEntity() instanceof LivingEntity
                ? ((LivingEntity) this.getEntity()).getCustomName() : this.name;
    }

    @Override
    public final boolean isValid() {
        return this.weakEntity.get() != null;
    }

    @Override
    public final boolean isEntityValid() {
        return this.weakEntity.get() != null && this.weakEntity.get().isValid();
    }

    /**
     * Returns the entity associated with this character if the entity has not been garbage
     * collected already (in which case, this RPGEntity will need to be removed from the system.
     *
     * @return the associated LivingEntity for this RPGEntity or null if the LivingEntity no longer
     * exists
     */
    @Override
    public Entity getEntity() {
        checkState(!isEntityValid(), "This RPGEntity is proxying a null entity!");
        return this.weakEntity.get();
    }

    @Override
    public EntityType getEntityType() {
        return isEntityValid() ? this.getEntity().getType() : EntityType.UNKNOWN;
    }

    @Override
    public boolean setEntity(Entity entity) {
        Validate.notNull(entity, "Cannot set a null Entity!");
        if (!this.uuid.equals(entity.getUniqueId())) {
            return false;
        }
        this.weakEntity = new WeakReference<>(entity);
        return true;
    }

    @Override
    public UUID getUniqueID() {
        return isEntityValid() ? this.getEntity().getUniqueId() : this.uuid;
    }

    @Override
    public Location getLocation() {
        check();
        return isEntityValid() ? this.getEntity().getLocation() : null;
    }

    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        check();
        return isEntityValid() ? this.getEntity().getNearbyEntities(x, y, z) :
                new ArrayList<Entity>();
    }

    @Override
    public World getWorld() {
        check();
        return isEntityValid() ? this.getEntity().getLocation().getWorld() : null;
    }

    @Override
    public boolean isOnGround() {
        check();
        return isEntityValid() && this.getEntity().isOnGround();
    }

    private void check() {
        checkState(!isValid(), "The linked entity is no longer available!");
    }

    @Override
    public int hashCode() {
        return this.isEntityValid() ? this.uuid.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RPGEntity) && this.isEntityValid()
                && ((RPGEntity) obj).isEntityValid()
                && ((RPGEntity) obj).getEntity().getUniqueId()
                .equals(this.getEntity().getUniqueId());
    }

}
