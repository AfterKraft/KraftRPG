package com.afterkraft.kraftrpg.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.afterkraft.kraftrpg.api.util.SerializationUtil;

public class SerializationTest {

    @BeforeClass
    public static void initCraftBukkit() {
        ServerInitializer.init();
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

    public static class TestObject implements ConfigurationSerializable {
        public double number;
        public List<TestObject> someList;

        public TestObject() {
            number = 0;
        }

        public TestObject(double num) {
            number = num;
        }

        @SuppressWarnings({ "unchecked" })
        public TestObject(Map<String, Object> data) {
            number = (Double) data.get("number");
            someList = (List<TestObject>) data.get("list");
        }

        @Override
        public Map<String, Object> serialize() {
            HashMap<String, Object> ret = new HashMap<String, Object>();
            ret.put("number", number);
            ret.put("list", someList);

            return ret;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(number);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((someList == null) ? 0 : someList.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            TestObject other = (TestObject) obj;
            if (Double.doubleToLongBits(number) != Double.doubleToLongBits(other.number)) return false;
            if (someList == null) {
                if (other.someList != null) return false;
            } else if (!someList.equals(other.someList)) return false;
            return true;
        }
    }
}
