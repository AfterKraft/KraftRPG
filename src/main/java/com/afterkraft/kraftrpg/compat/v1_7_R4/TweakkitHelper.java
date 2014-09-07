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
package com.afterkraft.kraftrpg.compat.v1_7_R4;

import java.util.List;

import com.afterkraft.metadata.PersistentMetadataValue;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

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
