package com.afterkraft.kraftrpg.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.bukkit.entity.EntityType;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CraftBukkitHandler.class})
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

    @Test
    public void testCorrectEntity() {
        Monster monster = new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), null);
        assertThat(monster.getEntity(), is(this.creator.getMockLivingEntity()));
    }

    @Test
    public void testNamedEntity() {
        new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
    }

    @Test
    public void testValidEntity() {
        Monster entity = new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
        assertTrue(entity.isEntityValid());
        assertTrue(entity.isValid());
    }

    @Test
    public void testGetEntityType() {
        Monster entity = new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getEntityType(), is(EntityType.ZOMBIE));
    }

    @Test
    public void testGetEntityMaxHealth() {
        Monster entity = new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getMaxHealth(), is(this.creator.getMockLivingEntity().getMaxHealth()));
    }

    @Test
    public void testGetEntityHealth() {
        Monster entity = new RPGMonster(this.plugin, this.creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getHealth(), is(this.creator.getMockLivingEntity().getHealth()));
    }
}
