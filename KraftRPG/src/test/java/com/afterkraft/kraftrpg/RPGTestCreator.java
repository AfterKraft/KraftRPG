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
package com.afterkraft.kraftrpg;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.api.RpgCommon;
import com.afterkraft.kraftrpg.api.handler.ServerInternals;
import com.afterkraft.kraftrpg.api.skills.ISkill;
import com.afterkraft.kraftrpg.api.storage.StorageFrontend;
import com.afterkraft.kraftrpg.util.util.FileUtils;
import com.afterkraft.kraftrpg.util.util.Util;

/**
 * Default test creator. Creates necessary server mocks and common utilities for general KraftRPG
 * like behaviors. For more specific behaviors, one should create a new TestCreator for that
 * specific purpose.
 */
public class RPGTestCreator {


    public static final File pluginDirectory = new File("bin/test/server/plugins/rpgtest");
    public static final File serverDirectory = new File("bin/test/server");
    private RPGPlugin mockPlugin;
    private ServerInternals mockHandler;
    private ISkill mockSkill;

    public RPGPlugin getMockPlugin() {
        return this.mockPlugin;
    }

    public ServerInternals getMockHandler() {
        return this.mockHandler;
    }

    public ISkill getMockSkill() {
        return this.mockSkill;
    }

    public boolean setup() {
        try {
            pluginDirectory.mkdirs();

            assertTrue(pluginDirectory.exists());

            this.mockPlugin = PowerMockito.mock(RPGPlugin.class);

            doReturn(pluginDirectory).when(this.mockPlugin).getDataFolder();

            PluginDescriptionFile descriptionFile =
                    new PluginDescriptionFile("TestRPG", "1.0.0", "RPGTestCreator");
            doReturn(descriptionFile).when(this.mockPlugin).getDescription();
            doReturn(true).when(this.mockPlugin).isEnabled();
            doReturn(Util.logger).when(this.mockPlugin).getLogger();

            // Add Core to the list of loaded plugins
            Plugin[] plugins = new RPGPlugin[]{this.mockPlugin};

            // Mock the Plugin Manager
            PluginManager mockPluginManager = PowerMockito.mock(PluginManager.class);
            Mockito.when(mockPluginManager.getPlugins()).thenReturn(plugins);
            Mockito.when(mockPluginManager.getPlugin("KraftRPG")).thenReturn(this.mockPlugin);
            Mockito.when(mockPluginManager.getPermission(anyString())).thenReturn(null);

            // Set up mockServer prep
            Server mockServer = createNiceMock(Server.class);
            expect(mockServer.getName()).andStubReturn("MockServer");
            expect(mockServer.getVersion()).andStubReturn("git-bukkit-1.7.9");
            expect(mockServer.getBukkitVersion()).andStubReturn("1.7.9");
            expect(mockServer.getLogger()).andStubReturn(Util.logger);
            expect(mockServer.getPluginManager()).andStubReturn(mockPluginManager);

            doReturn(mockServer).when(this.mockPlugin).getServer();

            // Set up Scheduler
            BukkitScheduler mockScheduler = mock(BukkitScheduler.class);
            when(mockScheduler.runTaskTimer(any(Plugin.class), any(Runnable.class), anyLong(),
                                            anyLong()))
                    .thenAnswer(new Answer<BukkitTask>() {
                        @Override
                        public BukkitTask answer(InvocationOnMock invocationOnMock) throws
                                Throwable {
                            Runnable arg;
                            try {
                                arg = (Runnable) invocationOnMock.getArguments()[1];
                            } catch (Exception e) {
                                return null;
                            }
                            arg.run();
                            BukkitTask mockTask = createNiceMock(BukkitTask.class);
                            expect(mockTask.getTaskId()).andReturn(arg.hashCode()).anyTimes();
                            replay(mockTask);
                            return mockTask;
                        }
                    });
            expect(mockServer.getScheduler()).andReturn(mockScheduler).anyTimes();

            // CraftItemFactory, just because we need to for serialization tests
            expect(mockServer.getItemFactory()).andStubReturn(CraftItemFactory.instance());

            replay(mockServer);

            // Set up mockStorage
            StorageFrontend mockStorage = createNiceMock(StorageFrontend.class);
            replay(mockStorage);

            doReturn(mockStorage).when(this.mockPlugin).getStorage();
            assertThat(this.mockPlugin.getServer(), is(mockServer));

            Bukkit.setServer(mockServer);

            // We need to mock the CraftBukkitHandler. We shouldn't test NMS implementation
            this.mockHandler = mock(ServerInternals.class);

            mockStatic(ServerInternals.class, new Answer<ServerInternals>() {
                @Override
                public ServerInternals answer(InvocationOnMock invocationOnMock) throws Throwable {
                    if (invocationOnMock.getMethod().getName().equalsIgnoreCase("getInterface")) {
                        return RPGTestCreator.this.mockHandler;
                    } else {
                        return null;
                    }
                }
            });
            when(this.mockHandler.getSpawnReason(any(LivingEntity.class), any(SpawnReason.class)))
                    .thenReturn(SpawnReason.NATURAL);
            when(this.mockHandler.getSpawnLocation(any(LivingEntity.class)))
                    .thenReturn(mock(Location.class));
            when(this.mockHandler.getEntityDamage(any(LivingEntity.class), anyDouble()))
                    .thenReturn(10D);
            RpgCommon.setHandler(RPGTestCreator.this.mockHandler);
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
            Util.log(Level.SEVERE,
                     "Error while trying to unregister the server. "
                             + "Has the Bukkit implementation changed?");
            e.printStackTrace();
            fail(e.getMessage());
            return false;
        }

        FileUtils.deleteFolder(serverDirectory);
        return true;
    }

}
