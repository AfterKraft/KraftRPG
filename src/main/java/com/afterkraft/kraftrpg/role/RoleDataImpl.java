package com.afterkraft.kraftrpg.role;

import com.afterkraft.kraftrpg.api.RpgKeys;
import com.afterkraft.kraftrpg.api.role.Role;
import com.afterkraft.kraftrpg.common.data.manipulator.immutable.ImmutableRoleData;
import com.afterkraft.kraftrpg.common.data.manipulator.mutable.RoleData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class RoleDataImpl extends AbstractData<RoleData, ImmutableRoleData> implements RoleData {

    private Role primary;
    private Role secondary;
    private List<Role> additional;

    public RoleDataImpl() {

    }

    public RoleDataImpl(Role primary, Role secondary) {
        this(primary, secondary, Collections.EMPTY_LIST);
    }

    public RoleDataImpl(Role primary, Role secondary, List<Role> additional) {
        this.primary = checkNotNull(primary, "primary");
        this.secondary = checkNotNull(secondary, "secondary");
        this .additional = new ArrayList<>(additional);
        registerGettersAndSetters();
    }


    @Override
    public Value<Role> primary() {
        return Sponge.getRegistry().getValueFactory().createValue(RpgKeys.PRIMARY_ROLE, this
                .primary);
    }

    @Override
    public Value<Role> secondary() {
        return Sponge.getRegistry().getValueFactory().createValue(RpgKeys.SECONDARY_ROLE,
                this.secondary);
    }
    @Override
    public ListValue<Role> additionals() {
        return Sponge.getRegistry().getValueFactory().createListValue(RpgKeys.ADDITIONAL_ROLES,
                this.additional);
    }
    Role getPrimary() {
        return this.primary;
    }

    void setPrimary(Role primary) {
        this.primary = primary;
    }

    Role getSecondary() {
        return this.secondary;
    }

    void setSecondary(Role secondary) {
        this.secondary = secondary;
    }

    List<Role> getAdditional() {
        return Collections.unmodifiableList(this.additional);
    }


    void setAdditional(List<Role> additional) {
        this.additional = ImmutableList.copyOf(additional);
    }

    @Override
    public void registerGettersAndSetters() {
        registerFieldGetter(RpgKeys.PRIMARY_ROLE, this::getPrimary);
        registerFieldSetter(RpgKeys.PRIMARY_ROLE, this::setPrimary);
        registerKeyValue(RpgKeys.PRIMARY_ROLE, this::primary);

        registerFieldGetter(RpgKeys.SECONDARY_ROLE, this::getSecondary);
        registerFieldSetter(RpgKeys.SECONDARY_ROLE, this::setSecondary);
        registerKeyValue(RpgKeys.SECONDARY_ROLE, this::secondary);

        registerFieldGetter(RpgKeys.ADDITIONAL_ROLES, this::getAdditional);
        registerFieldSetter(RpgKeys.ADDITIONAL_ROLES, this::setAdditional);
        registerKeyValue(RpgKeys.ADDITIONAL_ROLES, this::additionals);

    }

    @Override
    public Optional fill(DataHolder dataHolder, MergeFunction overlap) {
        final Optional<RoleData> roleData = dataHolder.get(RoleData.class);
        if (roleData.isPresent()) {
            final RoleData holderOne = roleData.get();
            final RoleData merged = overlap.merge(this, holderOne);
            setPrimary(checkNotNull(merged.primary().get()));
        }
        return Optional.empty();
    }

    @Override
    public Optional from(DataContainer container) {
        return Optional.empty();
    }


    @Override
    public RoleData copy() {
        final RoleDataImpl roleData = new RoleDataImpl();
        roleData.primary = this.primary;
        roleData.secondary = this.secondary;
        roleData.additional = ImmutableList.copyOf(this.additional);
        return roleData;
    }

    @Override
    public ImmutableRoleData asImmutable() {
        return null;
    }


    @Override
    public int getContentVersion() {
        return 1;
    }

}
