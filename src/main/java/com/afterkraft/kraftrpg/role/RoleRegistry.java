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
import com.afterkraft.kraftrpg.api.role.RoleManager;
import com.afterkraft.kraftrpg.api.util.FixedPoint;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class RoleRegistry implements AdditionalCatalogRegistryModule<Role>, RoleManager {

    @Nullable private static Role DEFAULT_PRIMARY = null;
    @Nullable private static Role DEFAULT_SECONDARY = null;
    private HashMap<String, Role> catalog = new HashMap<>();
    private Set<Key<?>> supportedKeys = new ConcurrentSkipListSet<>();

    private RoleRegistry() { }

    @Override
    @DelayedRegistration(RegistrationPhase.INIT)
    public void registerDefaults() {
        Role defaultRole = Role.builder()
                .id("kraftrpg:default")
                .name("DefaultRole")
                .description(Text.of(TextColors.DARK_AQUA, "KraftRPG Default Role For Testing"))
                .advancementLevel(10)
                .maxLevel(100)
                .choosable(false)
                .type(Role.RoleType.PRIMARY)
                .build();
        Role defaultSecondary = Role.builder()
                .id("kraftrpg:default_secondary")
                .name("DefaultSecondaryRole")
                .description(Text.of(TextColors.DARK_AQUA, "KraftRPG Default Role For Testing"))
                .advancementLevel(10)
                .maxLevel(100)
                .choosable(false)
                .type(Role.RoleType.SECONDARY)
                .build();
        registerAdditionalCatalog(defaultRole);
        setDefaultPrimaryRole(defaultRole);
        registerAdditionalCatalog(defaultSecondary);
        setDefaultSecondaryRole(defaultSecondary);
    }

    @Override
    public void registerAdditionalCatalog(Role extraCatalog) {
        checkNotNull(extraCatalog, "Key cannot be null!");
        final String id = extraCatalog.getId().toLowerCase(Locale.ENGLISH);
        this.catalog.put(id, extraCatalog);
    }

    @Override
    public Optional<Role> getById(String id) {
        return Optional.ofNullable(this.catalog.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<Role> getAll() {
        return ImmutableSet.copyOf(this.catalog.values());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Role getDefaultPrimaryRole() {
        if (DEFAULT_PRIMARY == null) {
            throw new IllegalStateException("KraftRPG does not have a set Default PRimary role! This is needed for all plugin functionality to work, please check for other errors from the plugin in the startup logs.");
        }
        return DEFAULT_PRIMARY;
    }

    @Override
    public boolean setDefaultPrimaryRole(Role role) {
        checkArgument(role != null, "Primary role cannot be set to null!");
        if (DEFAULT_PRIMARY == role) {
            return false;
        }
        DEFAULT_PRIMARY = role;
        return true;
    }

    @Override
    public Optional<Role> getDefaultSecondaryRole() {

        return Optional.ofNullable(DEFAULT_SECONDARY);
    }

    @Override
    public void setDefaultSecondaryRole(@Nullable Role role) {
        if (role == DEFAULT_SECONDARY) {
            return;
        }
        DEFAULT_SECONDARY = role;
    }


    @Override
    public List<Role> getRolesByType(Role.RoleType type) {
        return Collections.unmodifiableList(this.catalog.values().stream().filter(role -> role.getType() == type).collect(Collectors.toList()));
    }

    @Override
    public FixedPoint getRoleLevelExperience(Role role, int level) {
        return FixedPoint.ZERO;
    }

    @Override
    public void registerKeySupport(Key<?> key) {
        this.supportedKeys.add(key);
    }

    @Override
    public boolean supportsKey(Key<?> key) {
        return this.supportedKeys.contains(key);
    }

    static final class Holder {
        static final RoleRegistry INSTANCE = new RoleRegistry();
    }

    public static RoleRegistry getInstance() {
        return Holder.INSTANCE;
    }

}
