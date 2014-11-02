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
package com.afterkraft.kraftrpg.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.command.CommandExecutor;

import com.afterkraft.kraftrpg.KraftRPGPlugin;

/**
 * TODO Add documentation
 */
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
        Iterator<Map.Entry<String, CommandExecutor>> iterator =
                this.commandMap.entrySet().iterator();
        while (iterator.hasNext()) {
            String commandName = iterator.next().getKey();
            this.plugin.getCommand(commandName).setExecutor(null);
            iterator.remove();
        }
    }
}
