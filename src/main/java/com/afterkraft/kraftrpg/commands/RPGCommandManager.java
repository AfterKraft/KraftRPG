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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.command.CommandExecutor;

import com.afterkraft.kraftrpg.KraftRPGPlugin;

public class RPGCommandManager implements CommandManager {
    private KraftRPGPlugin plugin;
    private Map<String, CommandExecutor> commandMap = new HashMap<String, CommandExecutor>();

    public RPGCommandManager(KraftRPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerCommand(String commandName, CommandExecutor command) {
        if (commandName == null || command == null) {
            return;
        }
        if (!this.commandMap.containsKey(commandName)) {
            this.plugin.getCommand(commandName).setExecutor(command);
            this.commandMap.put(commandName, command);
        }
    }

    @Override
    public void unregisterCommand(String commandName) {
        if (commandName == null) {
            return;
        }
        CommandExecutor command = this.commandMap.get(commandName);
        if (command != null) {
            this.plugin.getCommand(commandName).setExecutor(null);
        }
        this.commandMap.remove(commandName);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {
        Iterator<Map.Entry<String, CommandExecutor>> iterator = this.commandMap.entrySet().iterator();
        while (iterator.hasNext()) {
            String commandName = iterator.next().getKey();
            this.plugin.getCommand(commandName).setExecutor(null);
            iterator.remove();
        }
    }
}
