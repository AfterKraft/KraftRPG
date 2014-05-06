/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.kraftrpg.entity.roles;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.entity.roles.Role;
import com.afterkraft.kraftrpg.api.entity.roles.RoleManager;
import com.afterkraft.kraftrpg.api.entity.roles.RoleType;


public class RPGRoleManager implements RoleManager {

    private final KraftRPGPlugin plugin;
    private final Map<String, Role> roleMap;
    private Role defaultPrimaryRole;
    private Role defaultSecondaryRole;

    public RPGRoleManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
        this.roleMap = new HashMap<String, Role>();

    }

    @Override
    public Role getDefaultPrimaryRole() {
        return this.defaultPrimaryRole;
    }

    @Override
    public boolean setDefaultPrimaryRole(Role role) {
        if (role == null || role.getType() != RoleType.PRIMARY) {
            return false;
        }
        this.defaultPrimaryRole = role;
        return true;
    }

    @Override
    public Role getDefaultSecondaryRole() {
        return this.defaultSecondaryRole;
    }

    @Override
    public boolean setDefaultSecondaryRole(Role role) {
        if (role == null || role.getType() != RoleType.SECONDARY) {
            return false;
        }
        this.defaultSecondaryRole = role;
        return true;
    }

    @Override
    public Role getRole(String roleName) {
        return roleMap.get(roleName);
    }

    @Override
    public boolean addRole(Role role) {
        if (role == null || !this.roleMap.containsKey(role.getName())) {
            return false;
        }
        this.roleMap.put(role.getName(), role);
        return true;
    }

    @Override
    public boolean removeRole(Role role) {
        if (role == null || !this.roleMap.containsKey(role.getName())) {
            return true;
        }
        this.roleMap.remove(role.getName());
        return false;
    }

    @Override
    public Map<String, Role> getRoles() {
        return Collections.unmodifiableMap(this.roleMap);
    }

    @Override
    public Map<String, Role> getRolesByType(RoleType type) {
        Map<String, Role> roleTypeMap = new HashMap<String, Role>(this.roleMap.size());
        for (Map.Entry<String, Role> entry : this.roleMap.entrySet()) {
            if (entry.getValue().getType().equals(type)) {
                roleTypeMap.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(roleTypeMap);
    }

    @Override
    public void queueRoleRefresh(Role role, RoleRefreshReason reason) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {

    }
}
