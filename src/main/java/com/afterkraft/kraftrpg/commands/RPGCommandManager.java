package com.afterkraft.kraftrpg.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
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
        if (!commandMap.containsKey(commandName)) {
            plugin.getCommand(commandName).setExecutor(command);
            commandMap.put(commandName, command);
        }
    }

    @Override
    public void unregisterCommand(String commandName) {
        if (commandName == null) {
            return;
        }
        CommandExecutor command = commandMap.get(commandName);
        if (command != null) {
            plugin.getCommand(commandName).setExecutor(null);
        }
        commandMap.remove(commandName);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {
        Iterator<Map.Entry<String, CommandExecutor>> iterator = commandMap.entrySet().iterator();
        while (iterator.hasNext()) {
            String commandName = iterator.next().getKey();
            plugin.getCommand(commandName).setExecutor(null);
            iterator.remove();
        }
    }
}
