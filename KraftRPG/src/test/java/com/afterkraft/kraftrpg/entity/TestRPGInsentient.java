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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.handler.ServerInternals;

/**
 * Tests all Insentient methods
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerInternals.class})
public class TestRPGInsentient {
    private RPGPlugin plugin;
    private RPGEntityCreator creator;

    @Before
    public void setUp() {
        this.creator = new RPGEntityCreator();
        assertTrue(this.creator.setupLivingEntity());
        this.plugin = this.creator.getMockPlugin();
    }

    @After
    public void cleanup() {
        this.creator.cleanup();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPlugin() {
        new RPGMonster(null, this.creator.getMockLivingEntity(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEntity() {
        new RPGMonster(this.plugin, null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testCorrectEntity() {
        Monster monster = new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), null);
        assertThat(monster.getEntity(), is(this.creator.getMockLivingEntity()));
    }

    @Test(expected = IllegalStateException.class)
    public void testNamedEntity() {
        new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
    }

    @Test(expected = IllegalStateException.class)
    public void testValidEntity() {
        Monster entity =
                new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
        assertTrue(entity.isEntityValid());
        assertTrue(entity.isValid());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetEntityType() {
        Monster entity =
                new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getEntityType(), is(EntityType.ZOMBIE));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetEntityMaxHealth() {
        Monster entity =
                new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getMaxHealth(), is(this.creator.getMockLivingEntity().getMaxHealth()));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetEntityHealth() {
        Monster entity =
                new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getHealth(), is(this.creator.getMockLivingEntity().getHealth()));
    }
}
