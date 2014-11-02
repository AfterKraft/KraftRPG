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
package com.afterkraft.kraftrpg.entity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.bukkit.entity.EntityType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.IEntity;

/**
 * Testing all RPGEntity stuffs
 */
@RunWith(PowerMockRunner.class)
public class TestRPGIEntity {
    private RPGPlugin plugin;
    private RPGEntityCreator creator;

    @Before
    public void setUp() {
        this.creator = new RPGEntityCreator();
        assertTrue(this.creator.setupEntity());
        this.plugin = this.creator.getMockPlugin();
    }

    @After
    public void cleanup() {
        this.creator.cleanup();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPlugin() {
        new RPGEntity(null, this.creator.getMockEntity(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEntity() {
        new RPGEntity(this.plugin, null, null);
    }

//    @Test
//    public void testCorrectEntity() {
//        new RPGEntity(this.plugin, this.creator.getMockEntity(), null);
//    }
//
//    @Test
//    public void testNamedEntity() {
//        new RPGEntity(this.plugin, this.creator.getMockEntity(), "TestNamedEntity");
//    }
//
//    @Test
//    public void testValidEntity() {
//        IEntity entity =
//                new RPGEntity(this.plugin, this.creator.getMockEntity(), "TestNamedEntity");
//        assertTrue(entity.isEntityValid());
//        assertTrue(entity.isValid());
//    }

    @Test(expected = IllegalStateException.class)
    public void testGetEntityType() {
        IEntity entity =
                new RPGEntity(this.plugin, this.creator.getMockEntity(), "TestNamedEntity");
        assertThat(entity.getEntityType(), is(EntityType.PIG));
    }
}
