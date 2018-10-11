/*
 * The MIT License (MIT)
 *
 * Copyright (c) Gabriel Harris-Rouquette
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
package com.afterkraft.kraftrpg.role;

import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.role.Role;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.stream.Collectors;


public class RoleImpl implements Role {

    private final String name;

    private final int maxLevel;
    private final Text description;
    private final RoleType type;
    private ImmutableSet<Role> parent;
    private ImmutableSet<Role> children;
    private final boolean choosable;
    private final int advancementLevel;
    private final String id;
    private Map<Key<?>, Object> actualValues = new HashMap<>();

    RoleImpl(RoleBuilderImpl builder) {
        this.name = builder.name;
        this.maxLevel = builder.maxLevel;
        this.description = builder.description;
        this.type = builder.type;
        this.parent = builder.parent == null ? ImmutableSet.of() : ImmutableSet.copyOf(builder.parent);
        this.children = builder.children == null ? ImmutableSet.of() : ImmutableSet.copyOf(builder.children);
        this.choosable = builder.choosable;
        this.advancementLevel = builder.advancementLevel;
        this.id = builder.id;
    }


    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public RoleType getType() {
        return this.type;
    }

    @Override
    public ImmutableSet<Role> getParents() {
        return this.parent;
    }

    @Override
    public ImmutableSet<Role> getChildren() {
        return this.children;
    }

    @Override
    public Role addParent(Role role) {
        Preconditions.checkArgument(!this.children.contains(role), "Role is already a child. This role %s is parented to %s", this, role);
        Preconditions.checkArgument(!this.parent.contains(role), "This role, %s,  already contains a parent relationship with %s", this, role);
        Preconditions.checkArgument(!role.getChildren().contains(this), "Target role, %s, already contains a parent relationship with this role, %s.", role, this);
        this.parent = ImmutableSet.<Role>builder().addAll(this.parent).add(role).build();
        if (role instanceof RoleImpl) {
            ((RoleImpl) role).children = ImmutableSet.<Role>builder().addAll(((RoleImpl) role).children).add(this).build();
        }
        return this;
    }

    @Override
    public Role addChildren(Role role) {
        Preconditions.checkArgument(!this.children.contains(role), "Role is already a child. This role %s is parented to %s", this, role);
        Preconditions.checkArgument(!this.parent.contains(role), "This role, %s,  already contains a parent relationship with %s", this, role);
        Preconditions.checkArgument(!role.getParents().contains(this), "Target role, %s, already contains a child relationship with this role, %s.", role, this);
        this.children = ImmutableSet.<Role>builder().addAll(this.children).add(role).build();
        if (role instanceof RoleImpl) {
            ((RoleImpl) role).parent = ImmutableSet.<Role>builder().addAll(((RoleImpl) role).parent).add(this).build();
        }
        return this;
    }

    @Override
    public boolean isChoosable() {
        return this.choosable;
    }

    @Override
    public int getAdvancementLevel() {
        return this.advancementLevel;
    }

    @Override
    public Text getDescription() {
        return this.description;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }


    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(DataQuery.of("roleid"), this.id)
                .set(DataQuery.of("rolename"), this.name)
                ;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        Object supplier = this.actualValues.get(key);
        return Optional.ofNullable(supplier != null ? (E) supplier : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        Object o = this.actualValues.get(key);
        if (o == null) {
            return Optional.empty();
        }
        return Optional.of((V) Sponge.getRegistry().getValueFactory().createValue((Key) key, o));
    }

    @Override
    public boolean supports(Key<?> key) {
        return RpgCommon.getRoleManager().supportsKey(key);
    }

    @Override
    public Role copy() {
        return new RoleBuilderImpl().from(this).build();
    }

    @Override
    public Set<Key<?>> getKeys() {
        return Collections.unmodifiableSet(this.actualValues.keySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<ImmutableValue<?>> getValues() {
        Set<ImmutableValue<?>> values = new HashSet<>();
        for (Map.Entry<Key<?>, Object> entry : this.actualValues.entrySet()) {
            values.add(Sponge.getRegistry().getValueFactory().createValue((Key) entry.getKey(), entry.getValue()).asImmutable());
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Role offer(Key<? extends BaseValue<E>> key, E value) {
        // check if the key is supported
        this.actualValues.put(key, value);
        return this;
    }

    @Override
    public Role remove(Key<?> key) {
        // check if key is supported.
        this.actualValues.remove(key);
        return this;
    }

    public boolean isDefault() {
        if (this.getType() == RoleType.PRIMARY) {
            return this == RoleRegistry.getInstance().getDefaultPrimaryRole();
        } else if (this.getType() == RoleType.SECONDARY){
            return this == RoleRegistry.getInstance().getDefaultSecondaryRole().orElse(null);
        }
        return false;

    }


}
