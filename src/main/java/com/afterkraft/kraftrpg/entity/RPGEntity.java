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

import java.lang.ref.WeakReference;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.IEntity;

public class RPGEntity implements IEntity {

    protected final RPGPlugin plugin;
    protected final String name;
    protected final UUID uuid;
    protected WeakReference<Entity> lEntity;

    public RPGEntity(RPGPlugin plugin, Entity lEntity, String name) {
        this.plugin = plugin;
        this.lEntity = new WeakReference<Entity>(lEntity);
        this.name = name != null ? name : lEntity.getType().name();
        this.uuid = lEntity.getUniqueId();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public final boolean isValid() {
        return lEntity.get() != null;
    }

    @Override
    public final boolean isEntityValid() {
        return this.lEntity.get() != null && this.lEntity.get().isValid();
    }

    /**
     * Returns the entity associated with this character if the entity has not
     * been garbage collected already (in which case, this RPGEntity will need
     * to be removed from the system.
     * 
     * @return the associated LivingEntity for this RPGEntity or null if the
     *         LivingEntity no longer exists
     */
    @Override
    public Entity getEntity() {
        if (!isEntityValid()) {
            return null;
        }
        return lEntity.get();
    }

    @Override
    public boolean setEntity(Entity entity) {
        if (!this.uuid.equals(entity.getUniqueId())) {
            return false;
        }
        this.lEntity = new WeakReference<Entity>(entity);
        return true;
    }

    @Override
    public UUID getUniqueID() {
        return isEntityValid() ? this.getEntity().getUniqueId() : null;
    }

    @Override
    public Location getLocation() {
        return isEntityValid() ? this.getEntity().getLocation() : null;
    }

    @Override
    public World getWorld() {
        return isEntityValid() ? this.getEntity().getLocation().getWorld() : null;
    }

    @Override
    public boolean isOnGround() {
        return isEntityValid() && this.getEntity().isOnGround();
    }

    @Override
    public int hashCode() {
        return this.isEntityValid() ? this.uuid.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RPGEntity) && this.isEntityValid() &&
                ((RPGEntity) obj).isEntityValid() &&
                ((RPGEntity) obj).getEntity().getUniqueId().equals(this.getEntity().getUniqueId());
    }

}
