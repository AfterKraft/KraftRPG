package com.afterkraft.kraftrpg.util;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemFactory;
import org.easymock.EasyMock;

public class ServerInitializer {
    public static boolean ran;

    static {
        Server mockServer = EasyMock.createNiceMock(Server.class);
        EasyMock.expect(mockServer.getName()).andStubReturn("MockServer");
        EasyMock.expect(mockServer.getVersion()).andStubReturn("1");
        EasyMock.expect(mockServer.getBukkitVersion()).andStubReturn("1.7.9");
        EasyMock.expect(mockServer.getLogger()).andStubReturn(Logger.getLogger("MockServer"));

        EasyMock.expect(mockServer.getItemFactory()).andStubReturn(CraftItemFactory.instance());

        // Finished! Mark as ready.
        EasyMock.replay(mockServer);

        Bukkit.setServer(mockServer);
    }

    public static void init() {
        // delegated to static {} block
    }
}
