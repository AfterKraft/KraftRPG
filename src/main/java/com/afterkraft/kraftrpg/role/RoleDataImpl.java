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

import com.afterkraft.kraftrpg.api.RpgKeys;
import com.afterkraft.kraftrpg.api.role.Role;
import com.afterkraft.kraftrpg.common.data.manipulator.immutable.ImmutableRoleData;
import com.afterkraft.kraftrpg.common.data.manipulator.mutable.RoleData;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.world.Location;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class RoleDataImpl extends AbstractData<RoleData, ImmutableRoleData> implements RoleData {

    private Role primary;
    @Nullable private Role secondary;
    private List<Role> additional;

    RoleDataImpl() {
    this(RoleRegistry.getInstance().getDefaultPrimaryRole(), RoleRegistry.getInstance().getDefaultSecondaryRole().orElse(null));
    }

    @SuppressWarnings("unchecked")
    public RoleDataImpl(Role primary, @Nullable Role secondary) {
        this(primary, secondary, Collections.emptyList());
    }

    RoleDataImpl(Role primary, @Nullable Role secondary, List<Role> additional) {
        this.primary = checkNotNull(primary, "primary");
        this.secondary = secondary;
        this .additional = new ArrayList<>(additional);
        registerGettersAndSetters();
    }


    @Override
    public Value<Role> primary() {
        return Sponge.getRegistry().getValueFactory().createValue(RpgKeys.PRIMARY_ROLE, this
                .primary, RoleRegistry.getInstance().getDefaultPrimaryRole());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public OptionalValue<Role> secondary() {
        return Sponge.getRegistry().getValueFactory().createOptionalValue(RpgKeys.SECONDARY_ROLE,
                this.secondary, RoleRegistry.getInstance().getDefaultSecondaryRole().orElse(null));
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

    @Nullable
    private Optional<Role> getSecondary() {
        return Optional.ofNullable(this.secondary);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void setSecondary(Optional<Role> secondary) {
        this.secondary = secondary.orElse(null);
    }

    private List<Role> getAdditional() {
        return Collections.unmodifiableList(this.additional);
    }


    private void setAdditional(List<Role> additional) {
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
    public Optional<RoleData> fill(DataHolder dataHolder, MergeFunction overlap) {
        if (dataHolder instanceof Location) {
            return Optional.empty();
        }
        if (dataHolder instanceof FluidStack) {
            return Optional.empty();
        }
        final Optional<RoleData> roleData = dataHolder.get(RoleData.class);
        if (roleData.isPresent()) {
            final RoleData holderOne = roleData.get();
            final RoleData merged = overlap.merge(this, holderOne);
            setPrimary(checkNotNull(merged.primary().get()));
        }
        return Optional.of(this);
    }

    @Override
    public Optional<RoleData> from(DataContainer container) {
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
        return new ImmutableRoleDataImpl(this.primary, this.secondary, this.additional);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer()
                .set(RpgKeys.PRIMARY_ROLE.getQuery(), this.primary.getId());
        if (this.secondary != null) {
            container.set(RpgKeys.SECONDARY_ROLE.getQuery(), this.secondary.getId());
        }
        if (!this.additional.isEmpty()) {
            container.set(RpgKeys.ADDITIONAL_ROLES.getQuery(), this.additional.stream().map(CatalogType::getId).collect(Collectors.toList()));
        }
        return container;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

}
