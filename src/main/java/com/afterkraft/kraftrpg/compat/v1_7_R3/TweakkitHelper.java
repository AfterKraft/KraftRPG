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
package com.afterkraft.kraftrpg.compat.v1_7_R3;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

import com.afterkraft.metadata.PersistentMetadataValue;

import com.afterkraft.kraftrpg.api.RPGPlugin;

public final class TweakkitHelper {

    public static double getEntityData(RPGPlugin plugin, Entity entity, String key, double def) {
        if (!entity.hasMetadata("kraftrpg" + key)) {
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            entity.setMetadata("kraftrpg" + key, value);
            return value.asDouble();
        } else {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            for (MetadataValue data : compound) {
                if (data instanceof PersistentMetadataValue) {
                    return data.asDouble();
                }
            }
            return value.asDouble();
        }
    }

    public static int getEntityData(RPGPlugin plugin, Entity entity, String key, int def) {
        if (!entity.hasMetadata("kraftrpg" + key)) {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            entity.setMetadata("kraftrpg" + key, value);
            return value.asInt();
        } else {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            for (MetadataValue data : compound) {
                if (data instanceof PersistentMetadataValue) {
                    return data.asInt();
                }
            }
            return value.asInt();
        }
    }

    public static String getEntityData(RPGPlugin plugin, Entity entity, String key, String def) {
        if (!entity.hasMetadata("kraftrpg" + key)) {
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            entity.setMetadata("kraftrpg" + key, value);
            return value.asString();
        } else {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            for (MetadataValue data : compound) {
                if (data instanceof PersistentMetadataValue) {
                    return data.asString();
                }
            }
            return value.asString();
        }
    }

    public static long getEntityData(RPGPlugin plugin, Entity entity, String key, long def) {
        if (!entity.hasMetadata("kraftrpg" + key)) {
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            entity.setMetadata("kraftrpg" + key, value);
            return value.asLong();
        } else {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            for (MetadataValue data : compound) {
                if (data instanceof PersistentMetadataValue) {
                    return data.asLong();
                }
            }
            return value.asLong();
        }
    }

    public static byte getEntityData(RPGPlugin plugin, Entity entity, String key, byte def) {
        if (!entity.hasMetadata("kraftrpg" + key)) {
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            entity.setMetadata("kraftrpg" + key, value);
            return value.asByte();
        } else {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            for (MetadataValue data : compound) {
                if (data instanceof PersistentMetadataValue) {
                    return data.asByte();
                }
            }
            return value.asByte();
        }
    }

    public static float getEntityData(RPGPlugin plugin, Entity entity, String key, float def) {
        if (!entity.hasMetadata("kraftrpg" + key)) {
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            entity.setMetadata("kraftrpg" + key, value);
            return value.asByte();
        } else {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            for (MetadataValue data : compound) {
                if (data instanceof PersistentMetadataValue) {
                    return data.asFloat();
                }
            }
            return value.asFloat();
        }
    }

    public static short getEntityData(RPGPlugin plugin, Entity entity, String key, short def) {
        if (!entity.hasMetadata("kraftrpg" + key)) {
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            entity.setMetadata("kraftrpg" + key, value);
            return value.asByte();
        } else {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            for (MetadataValue data : compound) {
                if (data instanceof PersistentMetadataValue) {
                    return data.asShort();
                }
            }
            return value.asShort();
        }
    }

    public static List<Object> getEntityData(RPGPlugin plugin, Entity entity, String key, List<Object> def) {
        if (!entity.hasMetadata("kraftrpg" + key)) {
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            entity.setMetadata("kraftrpg" + key, value);
            return value.asList();
        } else {
            List<MetadataValue> compound = entity.getMetadata("kraftrpg" + key);
            PersistentMetadataValue value = new PersistentMetadataValue(plugin, def);
            for (MetadataValue data : compound) {
                if (data instanceof PersistentMetadataValue) {
                    return ((PersistentMetadataValue) data).asList();
                }
            }
            return value.asList();
        }
    }

}
