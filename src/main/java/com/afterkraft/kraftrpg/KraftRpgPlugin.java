package com.afterkraft.kraftrpg;

import com.afterkraft.kraftrpg.api.RpgPlugin;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "kraftrpg",
        name = "KraftRPG",
        version = "${version}",
        description = "Base plugin implementing the KraftRPG API to provide a robust skills and classes framework for SpongeAPI",
        authors = "gabizou"
)
public class KraftRpgPlugin implements RpgPlugin {

    @Override
    public void cancelEnable() {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
