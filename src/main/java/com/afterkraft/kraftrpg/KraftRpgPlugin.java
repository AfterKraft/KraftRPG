package com.afterkraft.kraftrpg;

import com.afterkraft.kraftrpg.api.RpgKeys;
import com.afterkraft.kraftrpg.api.RpgPlugin;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "kraftrpg",
        name = "KraftRPG",
        version = "${version}",
        description = "Base plugin implementing the KraftRPG API to provide a robust skills and classes framework for SpongeAPI",
        authors = "gabizou"
)
public class KraftRpgPlugin implements RpgPlugin {

    @Listener
    public void keyRegister(GameRegistryEvent.Register<Key<?>> event) {
        event.register(RpgKeys.RPG_EFFECTS);
        event.register(RpgKeys.ADDITIONAL_ROLES);
    }

}
