package com.afterkraft.kraftrpg.entity;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import com.afterkraft.kraftrpg.RPGTestCreator;
import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.entity.EntityManager;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;
import com.afterkraft.kraftrpg.util.FileUtils;
import com.afterkraft.kraftrpg.util.Util;

public class RPGEntityCreator {

    public static final File pluginDirectory = new File("bin/test/server/plugins/rpgtest");
    public static final File serverDirectory = new File("bin/test/server");

    private Entity mockEntity;
    private LivingEntity mockLivingEntity;
    private Player mockPlayer;
    private EntityManager mockEntityManager;

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

    public boolean cleanup() {
        creator.cleanUp();
        return true;
    }
}
