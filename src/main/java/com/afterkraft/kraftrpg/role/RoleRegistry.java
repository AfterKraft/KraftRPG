package com.afterkraft.kraftrpg.role;

import com.afterkraft.kraftrpg.api.role.Role;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class RoleRegistry implements AdditionalCatalogRegistryModule<Role> {

    private HashMap<String, Role> catalog = new HashMap<>();

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

    static final class Holder {
        static final RoleRegistry INSTANCE = new RoleRegistry();
    }

    public static RoleRegistry getInstance() {
        return Holder.INSTANCE;
    }



}
