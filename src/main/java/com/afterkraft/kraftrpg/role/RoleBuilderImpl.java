package com.afterkraft.kraftrpg.role;

import com.afterkraft.kraftrpg.api.role.Role;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

public class RoleBuilderImpl extends AbstractDataBuilder<Role> implements Role.Builder {


    public String name;
    public int maxLevel;
    public Text description;
    public Role.RoleType type;
    public Set<Role> parent;
    public Set<Role> children;
    public boolean choosable;
    public int advancementLevel;
    public String id;
    public Role addParent;
    public Role addChildren;

    public RoleBuilderImpl() {
        super(Role.class, 1);
    }

    @Override
    public Role.Builder type(Role.RoleType type) {
        return this;
    }

    @Override
    public Role.Builder description(Text description) {
        return this;
    }

    @Override
    public RoleBuilderImpl reset() {
        return this;
    }

    @Override
    public RoleBuilderImpl from(Role value) {
        return null;
    }

    @Override
    public Role.Builder choosable(boolean choosable) {
        return this;
    }

    @Override
    public Role.Builder maxLevel(int maxLevel) {
        return this;
    }

    @Override
    public Role.Builder advancementLevel(int advancementLevel) {
        return this;
    }

    @Override
    public Role.Builder name(String name) {
        return this;
    }

    @Override
    public Role.Builder id(String id) {
        return this;
    }

    @Override
    public <E> Role.Builder aspect(Key<? extends BaseValue<E>> aspect, E value) {
        return this;
    }

    @Override
    public Role.Builder child(Role child) {
        return this;
    }

    @Override
    public Role.Builder parent(Role parent) {
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
