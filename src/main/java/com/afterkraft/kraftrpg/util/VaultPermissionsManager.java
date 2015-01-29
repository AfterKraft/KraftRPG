/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Gabriel Harris-Rouquette
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
package com.afterkraft.kraftrpg.util;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import net.milkbowl.vault.permission.Permission;

import com.afterkraft.kraftrpg.api.entity.IEntity;
import com.afterkraft.kraftrpg.api.util.PermissionsManager;

/**
 * Default implementation of PermissionsManager for Vault specifically. Other permissions can be
 * added as necessary.
 */
public final class VaultPermissionsManager implements PermissionsManager {

    private Permission permission;

    public VaultPermissionsManager(Permission permission) {
        this.permission = permission;
    }

    @Override public boolean isOp(IEntity entity) {
        return entity.isValid() && entity.getEntity() instanceof Permissible
                && ((Permissible) entity.getEntity()).isOp();
    }

    @Override public boolean hasPermission(IEntity entity, String permission) {
        return entity.isValid() && entity.getEntity() instanceof Permissible
                && ((Permissible) entity.getEntity()).hasPermission(permission);
    }

    @Override public boolean hasWorldPermission(IEntity entity, World world, String permission) {
        return entity.isValid() && entity.getEntity() instanceof Player
                && ((Player) entity.getEntity()).hasPermission(permission);
    }

    @Override public boolean hasWorldPermission(IEntity entity, String worldName,
                                                String permission) {
        return false;
    }

    @Override public void addGlobalPermission(IEntity entity, String permission) {

    }

    @Override public void addWorldPermission(IEntity entity, World world, String permission) {

    }

    @Override public void addWorldPermission(IEntity entity, String worldName, String permission) {

    }

    @Override public void addTransientGlobalPermission(IEntity entity, String permission) {

    }

    @Override public void addTransientWorldPermission(IEntity entity, World world,
                                                      String permission) {

    }

    @Override public void addTransientWorldPermission(IEntity entity, String worldName,
                                                      String permission) {

    }

    @Override public void removeGlobalPermission(IEntity entity, String permission) {

    }

    @Override public void removeWorldPermission(IEntity entity, World world, String permission) {

    }

    @Override public void removeWorldPermission(IEntity entity, String worldName,
                                                String permission) {

    }

    @Override public void removeTransientGlobalPermission(IEntity entity, String permission) {

    }

    @Override public void removeTransientWorldPermission(IEntity entity, World world,
                                                         String permission) {

    }

    @Override public void removeTransientWorldPermission(IEntity entity, String worldName,
                                                         String permission) {

    }

    @Override public void initialize() {

    }

    @Override public void shutdown() {

    }
}
