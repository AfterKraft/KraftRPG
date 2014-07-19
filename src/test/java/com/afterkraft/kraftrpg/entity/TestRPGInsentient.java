package com.afterkraft.kraftrpg.entity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.easymock.EasyMock.anyDouble;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.Monster;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CraftBukkitHandler.class})
public class TestRPGInsentient {
    private RPGPlugin plugin;
    private RPGEntityCreator creator;
    private CraftBukkitHandler mockHandler;

    @Before
    public void setUp() {
        creator = new RPGEntityCreator();
        assertTrue(creator.setupLivingEntity());
        plugin = creator.getMockPlugin();
    }

    @After
    public void cleanup() {
        creator.cleanup();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPlugin() {
        new RPGMonster(null, creator.getMockLivingEntity(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEntity() {
        new RPGMonster(plugin, null, null);
    }

    @Test
    public void testCorrectEntity() {
        Monster monster = new RPGMonster(plugin, creator.getMockLivingEntity(), null);
        assertThat(monster.getEntity(), is(creator.getMockLivingEntity()));
    }

    @Test
    public void testNamedEntity() {
        new RPGMonster(plugin, creator.getMockLivingEntity(), "TestNamedEntity");
    }

    @Test
    public void testValidEntity() {
        Monster entity = new RPGMonster(plugin, creator.getMockLivingEntity(), "TestNamedEntity");
        assertTrue(entity.isEntityValid());
        assertTrue(entity.isValid());
    }

    @Test
    public void testGetEntityType() {
        Monster entity = new RPGMonster(plugin, creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getEntityType(), is(EntityType.ZOMBIE));
    }

    @Test
    public void testGetEntityMaxHealth() {
        Monster entity = new RPGMonster(plugin, creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getMaxHealth(), is(creator.getMockLivingEntity().getMaxHealth()));
    }

    @Test
    public void testGetEntityHealth() {
        Monster entity = new RPGMonster(plugin, creator.getMockLivingEntity(), "TestNamedEntity");
        assertThat(entity.getHealth(), is(creator.getMockLivingEntity().getHealth()));
    }
}
