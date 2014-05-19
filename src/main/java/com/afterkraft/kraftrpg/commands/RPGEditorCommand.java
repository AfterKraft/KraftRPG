/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.kraftrpg.commands;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RPGEditorCommand extends BasicSubcommand {

    public RPGEditorCommand(RPGPlugin plugin) {
        super(plugin);
        setShortDescription("Interactive config editor");
        setLongDescription("" +
                "Edit your KraftRPG configuration interactively.\n" +
                // passive-aggressive note to FIXME BUKKIT-5611
                "§cWarning: On some old servers, this may not work unless used from the console.");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, int depth) {


    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args, int depth) {
        return null;
    }
}
