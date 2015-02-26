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
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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
        checkNotNull(plugin, "Cannot create an RPGEntity with a null plugin!");
        checkNotNull(weakEntity,
                "Cannot create an RPGEntity with a null Entity!");
        checkNotNull(name);
        this.plugin = plugin;
        this.weakEntity = new WeakReference<>(weakEntity);
        this.name = name;
        checkNotNull(this.name, "Failed to create a name for this entity!");
        this.uuid = weakEntity.getUniqueId();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.getEntity() instanceof Living
                ? ((Living) this.getEntity()).getCustomName() : this.name;
    }

    @Override
    public final boolean isValid() {
        return this.weakEntity.get() != null;
    }

    @Override
    public final boolean isEntityValid() {
        return this.weakEntity.get() != null && getUnsafeEntity().isLoaded();
    }

    /**
     * Returns the entity associated with this character if the entity has not
     * been garbage collected already (in which case, this RPGEntity will need
     * to be removed from the system.
     *
     * @return the associated Living for this RPGEntity or null if the Living no
     * longer exists
     */
    @Override
    public Optional<? extends Entity> getEntity() {
        checkState(!isEntityValid(),
                "This RPGEntity is proxying a null entity!");
        return Optional.of(this.getUnsafeEntity());
    }

    @Override
    public Optional<EntityType> getEntityType() {
        return isEntityValid()
                ? Optional.of(this.getEntity().get().getType())
                : Optional.<EntityType>absent();
    }

    @Override
    public boolean setEntity(Entity entity) {
        checkNotNull(entity, "Cannot set a null Entity!");
        if (!this.uuid.equals(entity.getUniqueId())) {
            return false;
        }
        this.weakEntity = new WeakReference<>(entity);
        return true;
    }

    @Override
    public UUID getUniqueID() {
        return this.uuid;
    }

    @Override
    public Location getLocation() {
        check();
        return this.getUnsafeEntity().getLocation();
    }

    @Override
    public void teleport(Location location) {
        check();
        this.getUnsafeEntity().setLocation(location);
    }

    @Override
    public void teleport(Location location, boolean keepYawAndPitch) {

    }

    @Override
    public List<Entity> getNearbyEntities(final double x, final double y,
                                          final double z) {
        check();
        if (isEntityValid()) {
            return ImmutableList.<Entity>builder()
                    .addAll(this.getWorld()
                            .getEntities(new Predicate<Entity>() {
                                @Override
                                public boolean apply(
                                        @Nullable
                                        Entity input) {
                                    if (input == null) {
                                        return false;
                                    }
                                    Location entityLocation =
                                            getLocation();
                                    Location difference =
                                            input.getLocation();
                                    return entityLocation.getPosition()
                                            .distance(difference
                                                    .getPosition())
                                            <= Math.sqrt(x * x + y * y
                                            + z
                                            * z);
                                }
                            })).build();
        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public World getWorld() {
        check();
        return this.getUnsafeEntity().getWorld();
    }

    @Override
    public boolean isOnGround() {
        check();
        return isEntityValid() && this.getUnsafeEntity().isOnGround();
    }

    private void check() {
        checkState(!isValid(), "The linked entity is no longer available!");
    }

    Entity getUnsafeEntity() {
        check();
        return this.weakEntity.get();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name,
                this.uuid,
                this.weakEntity);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RPGEntity other = (RPGEntity) obj;
        return Objects.equal(this.plugin, other.plugin)
                && Objects.equal(this.name, other.name)
                && Objects.equal(this.uuid, other.uuid)
                && Objects.equal(this.weakEntity, other.weakEntity);
    }
}
