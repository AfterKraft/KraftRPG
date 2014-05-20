package com.afterkraft.kraftrpg.commands;

import org.bukkit.command.CommandExecutor;

import com.afterkraft.kraftrpg.api.Manager;

public interface CommandManager extends Manager {

    public void registerCommand(String commandName, CommandExecutor command);

    public void unregisterCommand(String commandName);
}
