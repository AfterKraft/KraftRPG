package com.afterkraft.kraftrpg.entity.roles;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.entity.roles.RoleManager;

/**
 * @author gabizou
 */
public class RPGRoleManager implements RoleManager {

    private Role defaultPrimaryRole;
    private Role defaultSecondaryRole;
    private final KraftRPGPlugin plugin;

    public RPGRoleManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Role getDefaultPrimaryRole() {
        return this.defaultPrimaryRole;
    }

    @Override
    public Role getDefaultSecondaryRole() {
        return this.defaultSecondaryRole;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}
