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
package com.afterkraft.kraftrpg.util.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import com.afterkraft.kraftrpg.RPGTestCreator;
import com.afterkraft.kraftrpg.api.util.SerializationUtil;

/**
 * Tests for Serialization
 */
public class SerializationTest {

    @BeforeClass
    public static void initCraftBukkit() {
        RPGTestCreator creator = new RPGTestCreator();
        assertTrue(creator.setup());
    }

    @Test
    public void testItemSerialization() {
        // Prep item
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.FUCHSIA);
        item.setItemMeta(meta);

        Map<String, Object> serialized = SerializationUtil.fullySerialize(item);
        assertTrue(serialized.get("meta") instanceof Map);
        System.out.println(serialized);

        ConfigurationSerializable out = SerializationUtil.fullyDeserialize(serialized);
        assertTrue(out instanceof ItemStack);
        LeatherArmorMeta outMeta = (LeatherArmorMeta) item.getItemMeta();
        assertEquals(Color.FUCHSIA, outMeta.getColor());
    }

    @Test
    public void testCustomSerialization() {
        ConfigurationSerialization.registerClass(TestObject.class);

        TestObject root = new TestObject();
        root.someList = new ArrayList<TestObject>();
        root.someList.add(new TestObject(4.5));
        root.someList.add(new TestObject(78));
        TestObject child = new TestObject(-3);
        child.someList = ImmutableList.of(new TestObject(123456));
        root.someList.add(child);
        root.someList.add(new TestObject(4.5));

        Map<String, Object> serialized = SerializationUtil.fullySerialize(root);

        TestObject out = (TestObject) SerializationUtil.fullyDeserialize(serialized);
        assertEquals(root, out);
    }

    /**
     * Test Serialization Object
     */
    public static class TestObject implements ConfigurationSerializable {
        public double number;
        public List<TestObject> someList;

        public TestObject() {
            this.number = 0;
        }

        public TestObject(double num) {
            this.number = num;
        }

        @SuppressWarnings({"unchecked"})
        public TestObject(Map<String, Object> data) {
            this.number = (Double) data.get("number");
            this.someList = (List<TestObject>) data.get("list");
        }

        @Override
        public Map<String, Object> serialize() {
            HashMap<String, Object> ret = new HashMap<String, Object>();
            ret.put("number", this.number);
            ret.put("list", this.someList);

            return ret;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(this.number);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((this.someList == null) ? 0 : this.someList.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TestObject other = (TestObject) obj;
            if (Double.doubleToLongBits(this.number) != Double.doubleToLongBits(other.number)) {
                return false;
            }
            if (this.someList == null) {
                if (other.someList != null) {
                    return false;
                }
            } else if (!this.someList.equals(other.someList)) {
                return false;
            }
            return true;
        }
    }
}
