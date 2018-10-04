package com.afterkraft.kraftrpg.role;

import com.afterkraft.kraftrpg.api.role.Role;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;



public class RoleImpl implements Role {

    RoleImpl(RoleBuilderImpl builder) {
        this.name = builder.name;
        this.maxLevel = builder.maxLevel;
        this.description = builder.description;
        this.type = builder.type;
        this.parent = ImmutableSet.copyOf(builder.parent);
        this.children = ImmutableSet.copyOf(builder.children);
        this.choosable = builder.choosable;
        this.advancementLevel = builder.advancementLevel;
        this.id = builder.id;
        this.addParent = builder.addParent;
        this.addChildren = builder.addChildren;
    }

    private final String name;
    private final int maxLevel;
    private final Text description;
    private final RoleType type;
    private final ImmutableSet<Role> parent;
    private final ImmutableSet<Role> children;
    private final boolean choosable;
    private final int advancementLevel;
    private final String id;
    private final Role addParent;
    private final Role addChildren;

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
        return this.addParent;
    }

    @Override
    public Role addChildren(Role role) {
        return this.addChildren;
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
        return null;
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public Role copy() {
        return null;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return null;
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return null;
    }
}
