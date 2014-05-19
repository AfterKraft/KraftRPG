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

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Subcommand {

    public String getShortDescription();

    public void setShortDescription(String shortDescription);

    public String getLongDescription();

    public void setLongDescription(String longDescription);

    public String getUsage();

    public void setUsage(String usage);

    public String getPermission();

    public void setPermission(String permission);

    /**
     * Run this subcommand.
     *
     * @param sender Sender of the command
     * @param args Argument array
     * @param depth Number of arguments consumed by parent commands
     */
    public void onCommand(CommandSender sender, String[] args, int depth);

    /**
     * Perform tabcompletion.
     *
     * @param sender Sender of the command
     * @param args Argument array, tabcomplete last member
     * @param depth Number of arguments consumed by parent commands
     * @return Completion suggestions
     */
    public List<String> onTabComplete(CommandSender sender, String[] args, int depth);
}