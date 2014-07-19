package com.afterkraft.kraftrpg.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.bukkit.entity.EntityType;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.IEntity;

@RunWith(PowerMockRunner.class)
public class TestRPGIEntity {
    private RPGPlugin plugin;
    private RPGEntityCreator creator;

    @Before
    public void setUp() {
        creator = new RPGEntityCreator();
        assertTrue(creator.setupEntity());
        plugin = creator.getMockPlugin();
    }

    @After
    public void cleanup() {
        creator.cleanup();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPlugin() {
        new RPGEntity(null, creator.getMockEntity(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEntity() {
        new RPGEntity(plugin, null, null);
    }

    @Test
    public void testCorrectEntity() {
        new RPGEntity(plugin, creator.getMockEntity(), null);
    }

    @Test
    public void testNamedEntity() {
        new RPGEntity(plugin, creator.getMockEntity(), "TestNamedEntity");
    }

    @Test
    public void testValidEntity() {
        IEntity entity = new RPGEntity(plugin, creator.getMockEntity(), "TestNamedEntity");
        assertTrue(entity.isEntityValid());
        assertTrue(entity.isValid());
    }

    @Test
    public void testGetEntityType() {
        IEntity entity = new RPGEntity(plugin, creator.getMockEntity(), "TestNamedEntity");
        assertThat(entity.getEntityType(), is(EntityType.PIG));
    }
}
