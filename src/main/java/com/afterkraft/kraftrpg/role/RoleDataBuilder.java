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
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.extra.fluid.FluidStack;
import org.spongepowered.api.world.Location;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RoleDataBuilder extends AbstractDataBuilder<RoleData> implements DataManipulatorBuilder<RoleData, ImmutableRoleData> {


    public RoleDataBuilder() {
        super(RoleData.class, 1);
    }

    @Override
    protected Optional<RoleData> buildContent(DataView container) throws InvalidDataException {
        Optional<Role> primary = container.getCatalogType(RpgKeys.PRIMARY_ROLE.getQuery(), Role.class);
        if (!primary.isPresent()) {
            return Optional.empty();
        }
        Optional<Role> secondary = container.getCatalogType(RpgKeys.SECONDARY_ROLE.getQuery(), Role.class);
        Optional<List<Role>> catalogTypeList = container.getCatalogTypeList(RpgKeys.ADDITIONAL_ROLES.getQuery(), Role.class);
        RoleDataImpl data = new RoleDataImpl(primary.get(), secondary.orElse(RoleRegistry.getInstance().getDefaultSecondaryRole().orElse(null)), catalogTypeList.orElse(Collections.emptyList()));
        return Optional.of(data);
    }

    @Override
    public RoleData create() {
        return new RoleDataImpl();
    }

    @Override
    public Optional<RoleData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof FluidStack) {
            return Optional.empty();
        }
        if (dataHolder instanceof Location<?>) {
            return Optional.empty();
        }
        return create().fill(dataHolder);
    }
}
