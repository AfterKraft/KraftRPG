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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableRoleDataImpl extends AbstractImmutableData<ImmutableRoleData, RoleData>  implements ImmutableRoleData  {

    private Role primary;
    private Role secondary;
    private List<Role> additional;
    // Lazy loading caching
    @Nullable private ImmutableValue<Role> immutablePrimaryValue;
    @Nullable private ImmutableOptionalValue<Role> roleImmutableValue;
    @Nullable private ImmutableListValue<Role> roleImmutableListValue;

    ImmutableRoleDataImpl(Role primary, @Nullable Role secondary, List<Role> additional) {
        this.primary = checkNotNull(primary, "primary");
        this.secondary = checkNotNull(secondary, "secondary");
        this .additional = ImmutableList.copyOf(additional);
        registerGetters();
    }
    Role getPrimary() {
        return this.primary;
    }

    private Role getSecondary() {
        return this.secondary;
    }

    private ImmutableList<Role> getAdditional() {
        return (ImmutableList<Role>) Collections.unmodifiableList(this.additional);
    }

    @Override
    public RoleData asMutable() {
        return new RoleDataImpl(this.primary, this.secondary, this.additional);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public ImmutableValue<Role> primary() {
        if (this.immutablePrimaryValue == null) {
            immutablePrimaryValue = Sponge.getRegistry().getValueFactory().createValue(RpgKeys.PRIMARY_ROLE, this
                    .primary, RoleRegistry.getInstance().getDefaultPrimaryRole()).asImmutable();
        }
        return immutablePrimaryValue;
}

    @SuppressWarnings("ConstantConditions")
    @Override
    public ImmutableOptionalValue<Role> secondary() {
        if (this.roleImmutableValue == null) {
            roleImmutableValue = (ImmutableOptionalValue<Role>) Sponge.getRegistry().getValueFactory().createOptionalValue(RpgKeys.SECONDARY_ROLE,
                    this.secondary, RoleRegistry.getInstance().getDefaultSecondaryRole().orElse(null)).asImmutable();
        }
        return roleImmutableValue;
    }

    @Override
    public ImmutableListValue<Role> additionalRoles() {
        if (roleImmutableListValue == null) {
            roleImmutableListValue = Sponge.getRegistry().getValueFactory().createListValue(RpgKeys.ADDITIONAL_ROLES,
                    this.additional).asImmutable();
        }
        return roleImmutableListValue;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(RpgKeys.PRIMARY_ROLE, this::getPrimary);
        registerKeyValue(RpgKeys.PRIMARY_ROLE, this::primary);

        registerFieldGetter(RpgKeys.SECONDARY_ROLE, this::getSecondary);
        registerKeyValue(RpgKeys.SECONDARY_ROLE, this::secondary);

        registerFieldGetter(RpgKeys.ADDITIONAL_ROLES, this::getAdditional);
        registerKeyValue(RpgKeys.ADDITIONAL_ROLES, this::additionalRoles);

    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(RpgKeys.PRIMARY_ROLE.getQuery(), this.primary)
                .set(RpgKeys.SECONDARY_ROLE.getQuery(), this.secondary)
                .set(RpgKeys.ADDITIONAL_ROLES.getQuery(), this.additional);
    }
}