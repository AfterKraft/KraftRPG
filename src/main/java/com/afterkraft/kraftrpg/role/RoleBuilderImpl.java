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

import com.afterkraft.kraftrpg.api.role.Role;

import com.google.common.collect.Sets;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class RoleBuilderImpl extends AbstractDataBuilder<Role> implements Role.Builder {


    public String name;
    public int maxLevel;
    public Text description;
    public Role.RoleType type;
    @Nullable public Set<Role> parent;
    @Nullable public Set<Role> children;
    public boolean choosable;
    public int advancementLevel;
    public String id;
    public RoleBuilderImpl() {
        super(Role.class, 1);
    }

    @Override
    public Role.Builder type(Role.RoleType type) {
        this.type = checkNotNull(type, "RoleType cannot be null");
        return this;
    }

    @Override
    public Role.Builder description(Text description) {
        this.description = checkNotNull(description, "Description cannot be null");
        return this;
    }

    @Override
    public RoleBuilderImpl reset() {
        return this;
    }

    @Override
    public RoleBuilderImpl from(Role value) {
        this.name = value.getName();
        this.maxLevel = value.getMaxLevel();
        this.description = value.getDescription();
        this.type = value.getType();
        this.parent = Sets.newHashSet(value.getParents());
        this.children = Sets.newHashSet(value.getChildren());
        this.choosable = value.isChoosable();
        this.advancementLevel = value.getAdvancementLevel();
        this.id = value.getId();
        return this;
    }

    @Override
    public Role.Builder choosable(boolean choosable) {
        this.choosable = choosable;
        return this;
    }

    @Override
    public Role.Builder maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    @Override
    public Role.Builder advancementLevel(int advancementLevel) {
        this.advancementLevel = advancementLevel;
        return this;
    }

    @Override
    public Role.Builder name(String name) {
        checkArgument(name != null, "Name cannot be null");
        checkArgument(!name.isEmpty(), "Name cannot be empty!");
        this.name = name;
        return this;
    }

    @Override
    public Role.Builder id(String id) {
        checkArgument(id != null, "Id cannot be null");
        checkArgument(!id.isEmpty(), "ID cannot be empty");
        this.id = id;
        return this;
    }

    @Override
    public <E> Role.Builder aspect(Key<? extends BaseValue<E>> aspect, E value) {
        return this;
    }

    @Override
    public Role.Builder child(Role child) {
        if (this.children == null) {
            this.children = new HashSet<>();
        }
        this.children.add(child);
        return this;
    }

    @Override
    public Role.Builder parent(Role parent) {
        if (this.parent == null) {
            this.parent = new HashSet<>();
        }
        this.parent.add(parent);
        return this;
    }

    @Override
    public Role build() {

        checkState(this.name != null, "The name must be set!");
        checkState(this.id != null, "An id must be set!");
        checkState(this.description != null,  "A description must be set!");
        checkState(this.type != null, "A type must be set!");
        checkState(this.advancementLevel != 0, "An advancement level must be set!");
        checkState(this.maxLevel != 0, "A maximum level must be set");

     return new RoleImpl(this);
    }

    @Override
    protected Optional<Role> buildContent(DataView container) throws InvalidDataException {
        return Optional.empty();
    }
}
