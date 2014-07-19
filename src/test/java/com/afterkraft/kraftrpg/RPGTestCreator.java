package com.afterkraft.kraftrpg;

import java.io.File;
import java.lang.reflect.Field;
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
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemFactory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.handler.CraftBukkitHandler;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;
import com.afterkraft.kraftrpg.util.FileUtils;
import com.afterkraft.kraftrpg.util.Util;

public class RPGTestCreator {


    private RPGPlugin mockPlugin;
    private CraftBukkitHandler mockHandler;

    private ISkill mockSkill;

    public static final File pluginDirectory = new File("bin/test/server/plugins/rpgtest");

    public static final File serverDirectory = new File("bin/test/server");

    public RPGPlugin getMockPlugin() {
        return mockPlugin;
    }

    public CraftBukkitHandler getMockHandler() {
        return mockHandler;
    }

    public ISkill getMockSkill() {
        return mockSkill;
    }

    public boolean setup() {
        try {
            pluginDirectory.mkdirs();

            assertTrue(pluginDirectory.exists());

            mockPlugin = PowerMockito.mock(RPGPlugin.class);

            doReturn(pluginDirectory).when(mockPlugin).getDataFolder();

            PluginDescriptionFile descriptionFile = new PluginDescriptionFile("TestRPG", "1.0.0", "com.afterkraft.kraftrpg.RPGTestCreator");
            doReturn(descriptionFile).when(mockPlugin).getDescription();
            doReturn(true).when(mockPlugin).isEnabled();
            doReturn(Util.logger).when(mockPlugin).getLogger();

            // Add Core to the list of loaded plugins
            Plugin[] plugins = new RPGPlugin[]{mockPlugin};

            // Mock the Plugin Manager
            PluginManager mockPluginManager = PowerMockito.mock(PluginManager.class);
            Mockito.when(mockPluginManager.getPlugins()).thenReturn(plugins);
            Mockito.when(mockPluginManager.getPlugin("KraftRPG")).thenReturn(mockPlugin);
            Mockito.when(mockPluginManager.getPermission(anyString())).thenReturn(null);

            // Set up mockServer prep
            Server mockServer = createNiceMock(Server.class);
            expect(mockServer.getName()).andStubReturn("MockServer");
            expect(mockServer.getVersion()).andStubReturn("git-bukkit-1.7.9");
            expect(mockServer.getBukkitVersion()).andStubReturn("1.7.9");
            expect(mockServer.getLogger()).andStubReturn(Util.logger);
            expect(mockServer.getPluginManager()).andStubReturn(mockPluginManager);

            doReturn(mockServer).when(mockPlugin).getServer();

            // Set up Scheduler
            BukkitScheduler mockScheduler = mock(BukkitScheduler.class);
            when(mockScheduler.runTaskTimer(any(Plugin.class), any(Runnable.class), anyLong(), anyLong()))
                    .thenAnswer(new Answer<Integer>() {
                        @Override
                        public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                            Runnable arg;
                            try {
                                arg = (Runnable) invocationOnMock.getArguments()[1];
                            } catch (Exception e) {
                                return null;
                            }
                            arg.run();
                            return null;
                        }
                    });
            expect(mockServer.getScheduler()).andReturn(mockScheduler).anyTimes();

            // CraftItemFactory, just because we need to for serialization tests
            expect(mockServer.getItemFactory()).andStubReturn(CraftItemFactory.instance());

            replay(mockServer);

            // Set up mockStorage
            StorageFrontend mockStorage = createNiceMock(StorageFrontend.class);
            replay(mockStorage);

            doReturn(mockStorage).when(mockPlugin).getStorage();
            assertThat(mockPlugin.getServer(), is(mockServer));

            Bukkit.setServer(mockServer);

            mockHandler = mock(CraftBukkitHandler.class);

            mockStatic(CraftBukkitHandler.class, new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    if (invocationOnMock.getMethod().getName().equalsIgnoreCase("getInterface")) {
                        return mockHandler;
                    } else {
                        return 1;
                    }
                }
            });
            when(mockHandler.getSpawnReason(any(LivingEntity.class), any(SpawnReason.class))).thenReturn(SpawnReason.NATURAL);
            when(mockHandler.getSpawnLocation(any(LivingEntity.class))).thenReturn(mock(Location.class));
            when(mockHandler.getEntityDamage(any(LivingEntity.class), anyDouble())).thenReturn(10D);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cleanUp() {
        try {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(Class.forName("org.bukkit.Bukkit"), null);
        } catch (Exception e) {
            Util.log(Level.SEVERE, "Error while trying to unregister the server. Has the Bukkit implementation changed?");
            e.printStackTrace();
            fail(e.getMessage());
            return false;
        }

        FileUtils.deleteFolder(serverDirectory);
        return true;
    }

}
