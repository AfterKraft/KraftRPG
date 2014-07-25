package com.afterkraft.kraftrpg.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

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
        return this.mockEntity;
    }

    public LivingEntity getMockLivingEntity() {
        return this.mockLivingEntity;
    }

    public Player getMockPlayer() {
        return this.mockPlayer;
    }

    public RPGPlugin getMockPlugin() {
        return this.creator.getMockPlugin();
    }

    public EntityManager getMockEntityManager() {
        return this.mockEntityManager;
    }

    public boolean setupEntity() {
        try {
            this.creator = new RPGTestCreator();
            assertThat(this.creator.setup(), is(true));
            RPGPlugin mockPlugin = this.creator.getMockPlugin();

            this.mockEntityManager = spy(new RPGEntityManager(mockPlugin));
            doReturn(this.mockEntityManager).when(mockPlugin).getEntityManager();


            // Set up mockEntity
            this.mockEntity = createNiceMock(Entity.class);
            expect(this.mockEntity.getType()).andReturn(EntityType.PIG).anyTimes();
            expect(this.mockEntity.getUniqueId()).andReturn(new UUID(10, 4)).atLeastOnce();
            expect(this.mockEntity.isValid()).andReturn(true).atLeastOnce();
            replay(this.mockEntity);

            assertThat(mockPlugin.getEntityManager(), is(this.mockEntityManager));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setupLivingEntity() {
        try {
            this.creator = new RPGTestCreator();
            assertThat(this.creator.setup(), is(true));
            RPGPlugin mockPlugin = this.creator.getMockPlugin();

            this.mockEntityManager = spy(new RPGEntityManager(mockPlugin));
            doReturn(this.mockEntityManager).when(mockPlugin).getEntityManager();

            this.mockEntityManager.initialize();

            RPGPluginProperties mockProperties = new RPGPluginProperties();
            RPGPluginProperties.isMobDamageDistanceModified = false;
            RPGPluginProperties.isMobExpDistanceModified = false;
            RPGPluginProperties.isMobHealthDistanceModified = false;
            doReturn(mockProperties).when(mockPlugin).getProperties();

            this.mockDamageManager = spy(new RPGDamageManager(mockPlugin));
            doReturn(10D).when(this.mockDamageManager).getEntityDamage(any(EntityType.class));

            doReturn(this.mockDamageManager).when(mockPlugin).getDamageManager();


            // Set up mockEntity
            this.mockLivingEntity = createNiceMock(Zombie.class);
            expect(this.mockLivingEntity.getType()).andReturn(EntityType.ZOMBIE).anyTimes();
            expect(this.mockLivingEntity.getUniqueId()).andReturn(new UUID(10, 4)).atLeastOnce();
            expect(this.mockLivingEntity.isValid()).andReturn(true).atLeastOnce();
            expect(this.mockLivingEntity.getHealth()).andReturn(100D).atLeastOnce();
            expect(this.mockLivingEntity.getMaxHealth()).andReturn(100D).atLeastOnce();
            expect(this.mockLivingEntity.getMetadata(anyString())).andReturn(new ArrayList<MetadataValue>());
            replay(this.mockLivingEntity);

            assertThat(mockPlugin.getEntityManager(), is(this.mockEntityManager));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setupPlayer() {
        try {
            this.creator = new RPGTestCreator();
            assertThat(this.creator.setup(), is(true));
            RPGPlugin mockPlugin = this.creator.getMockPlugin();

            this.mockEntityManager = spy(new RPGEntityManager(mockPlugin));
            doReturn(this.mockEntityManager).when(mockPlugin).getEntityManager();

            this.mockEntityManager.initialize();

            RPGPluginProperties mockProperties = new RPGPluginProperties();
            RPGPluginProperties.isMobDamageDistanceModified = false;
            RPGPluginProperties.isMobExpDistanceModified = false;
            RPGPluginProperties.isMobHealthDistanceModified = false;
            doReturn(mockProperties).when(mockPlugin).getProperties();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cleanup() {
        this.creator.cleanUp();
        return true;
    }
}
