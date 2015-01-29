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

import java.util.List;

import org.bukkit.command.CommandSender;

/**
 * TODO Add documentation
 */
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
     * @param args   Argument array
     * @param depth  Number of arguments consumed by parent commands
     */
    public void onCommand(CommandSender sender, String[] args, int depth);

    /**
     * Perform tabcompletion.
     *
     * @param sender Sender of the command
     * @param args   Argument array, tabcomplete last member
     * @param depth  Number of arguments consumed by parent commands
     *
     * @return Completion suggestions
     */
    public List<String> onTabComplete(CommandSender sender, String[] args, int depth);
}
