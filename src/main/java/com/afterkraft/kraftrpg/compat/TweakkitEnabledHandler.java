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
package com.afterkraft.kraftrpg.compat;

import org.bukkit.entity.Entity;

import com.afterkraft.configuration.CustomDataCompound;

import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;

public abstract class TweakkitEnabledHandler extends CraftBukkitHandler {

    protected TweakkitEnabledHandler(ServerType type) {
        super(type);
    }

    protected double getEntityData(Entity entity, String key, double def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setDouble(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getDouble(key) != 0.0D ? compound.getDouble(key) : def;
        }

    }

    protected int getEntityData(Entity entity, String key, int def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setInt(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getInt(key) != 0 ? compound.getInt(key) : def;
        }
    }

    protected String getEntityData(Entity entity, String key, String def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setString(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getString(key) != null ? compound.getString(key) : def;
        }
    }

    protected long getEntityData(Entity entity, String key, long def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setLong(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getLong(key) != 0 ? compound.getLong(key) : def;
        }
    }

    protected byte getEntityData(Entity entity, String key, byte def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setByte(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getByte(key) != 0 ? compound.getByte(key) : def;
        }
    }

    protected byte[] getEntityData(Entity entity, String key, byte[] def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setByteArray(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getByteArray(key) != null ? compound.getByteArray(key) : def;
        }
    }

    protected float getEntityData(Entity entity, String key, float def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setFloat(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getFloat(key) != 0 ? compound.getFloat(key) : def;
        }
    }

    protected int[] getEntityData(Entity entity, String key, int[] def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setIntArray(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getIntArray(key) != null ? compound.getIntArray(key) : def;
        }
    }

    protected short getEntityData(Entity entity, String key, short def) {
        if (!entity.getCustomData().hasKey("kraftrpg")) {
            CustomDataCompound compound = entity.getCustomData();
            compound.set("kraftrpg", new CustomDataCompound());
            compound = compound.getCompound("kraftrpg");
            compound.setShort(key, def);
            return def;
        } else {
            CustomDataCompound compound = entity.getCustomData().getCompound("kraftrpg");
            return compound.getShort(key) != 0 ? compound.getShort(key) : def;
        }
    }

}
