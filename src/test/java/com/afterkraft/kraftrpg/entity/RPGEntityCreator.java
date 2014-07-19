package com.afterkraft.kraftrpg.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.hamcrest.core.Is;
import org.mockito.Matchers;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import net.minecraft.server.v1_7_R3.EntityZombie;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.MetadataValue;

import com.afterkraft.kraftrpg.RPGTestCreator;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.util.DamageManager;
import com.afterkraft.kraftrpg.listeners.EntityListener;
import com.afterkraft.kraftrpg.util.RPGDamageManager;
import com.afterkraft.kraftrpg.util.RPGPluginProperties;

public class RPGEntityCreator {

    public static final File pluginDirectory = new File("bin/test/server/plugins/rpgtest");
    public static final File serverDirectory = new File("bin/test/server");

    private Entity mockEntity;
    private LivingEntity mockLivingEntity;
    private Player mockPlayer;
    private EntityManager mockEntityManager;
    private DamageManager mockDamageManager;

    private RPGTestCreator creator;


    public Entity getMockEntity() {
        return mockEntity;
    }

    public LivingEntity getMockLivingEntity() {
        return mockLivingEntity;
    }

    public Player getMockPlayer() {
        return mockPlayer;
    }

    public RPGPlugin getMockPlugin() {
        return creator.getMockPlugin();
    }

    public EntityManager getMockEntityManager() {
        return mockEntityManager;
    }

    public boolean setupEntity() {
        try {
            creator = new RPGTestCreator();
            assertThat(creator.setup(), is(true));
            RPGPlugin mockPlugin = creator.getMockPlugin();

            mockEntityManager = spy(new RPGEntityManager(mockPlugin));
            doReturn(mockEntityManager).when(mockPlugin).getEntityManager();


            // Set up mockEntity
            mockEntity = createNiceMock(Entity.class);
            expect(mockEntity.getType()).andReturn(EntityType.PIG).anyTimes();
            expect(mockEntity.getUniqueId()).andReturn(new UUID(10, 4)).atLeastOnce();
            expect(mockEntity.isValid()).andReturn(true).atLeastOnce();
            replay(mockEntity);

            assertThat(mockPlugin.getEntityManager(), is(mockEntityManager));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setupLivingEntity() {
        try {
            creator = new RPGTestCreator();
            assertThat(creator.setup(), is(true));
            RPGPlugin mockPlugin = creator.getMockPlugin();

            mockEntityManager = spy(new RPGEntityManager(mockPlugin));
            doReturn(mockEntityManager).when(mockPlugin).getEntityManager();

            RPGPluginProperties mockProperties = new RPGPluginProperties();
            RPGPluginProperties.isMobDamageDistanceModified = false;
            RPGPluginProperties.isMobExpDistanceModified = false;
            RPGPluginProperties.isMobHealthDistanceModified = false;
            doReturn(mockProperties).when(mockPlugin).getProperties();

            mockDamageManager = spy(new RPGDamageManager(mockPlugin));
            doReturn(new Double(10D)).when(mockDamageManager).getEntityDamage(any(EntityType.class));

            doReturn(mockDamageManager).when(mockPlugin).getDamageManager();


            // Set up mockEntity
            mockLivingEntity = createNiceMock(Zombie.class);
            expect(mockLivingEntity.getType()).andReturn(EntityType.ZOMBIE).anyTimes();
            expect(mockLivingEntity.getUniqueId()).andReturn(new UUID(10, 4)).atLeastOnce();
            expect(mockLivingEntity.isValid()).andReturn(true).atLeastOnce();
            expect(mockLivingEntity.getHealth()).andReturn(100D).atLeastOnce();
            expect(mockLivingEntity.getMaxHealth()).andReturn(100D).atLeastOnce();
            expect(mockLivingEntity.getMetadata(anyString())).andReturn(new ArrayList<MetadataValue>());
            replay(mockLivingEntity);

            assertThat(mockPlugin.getEntityManager(), is(mockEntityManager));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cleanup() {
        creator.cleanUp();
        return true;
    }
}
