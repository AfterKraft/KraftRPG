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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import com.afterkraft.kraftrpg.api.RPGPlugin;

/**
 * TODO Add documentation
 */
public abstract class RootCommand implements TabExecutor {
    protected static final int COMMANDS_PER_PAGE = 7;
    protected RPGPlugin plugin;
    private Map<String, String> aliasMap;
    private Map<String, Subcommand> subcommandMap;
    private List<String> helpList = null;

    protected RootCommand(RPGPlugin plugin) {
        this.plugin = plugin;
        this.aliasMap = new HashMap<>();
        this.subcommandMap = new HashMap<>();
    }

    protected void addSubcommand(String name, Subcommand sub) {
        name = name.toLowerCase();

        this.subcommandMap.put(name, sub);
        
        if (this.helpList != null) {
            buildSortedList();
        }
    }

    protected void addSubcommand(String name, Subcommand sub, String... aliases) {
        name = name.toLowerCase();

        this.subcommandMap.put(name, sub);
        for (String alias : aliases) {
            this.subcommandMap.put(alias, sub);
            this.aliasMap.put(alias, name);
        }
        
        if (this.HelpList != null) {
            buildSortedList();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            doHelp(sender, label, 1);
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("?") || sub.equals("help")) {
            if (args.length == 1) {
                doHelp(sender, label, 1);
            } else {
                sub = args[1];
                if (this.subcommandMap.containsKey(sub)) {
                    doHelp(sender, label, sub);
                } else {
                    try {
                        int page = Integer.parseInt(sub);
                        doHelp(sender, label, page);
                    } catch (NumberFormatException ignored) {
                        sender.sendMessage(
                                ChatColor.RED + "Please enter a page number or valid subcommand.");
                    }
                }
            }
            return true;
        }

        Subcommand subcommand = this.subcommandMap.get(sub);
        if (subcommand == null) {
            sender.sendMessage(
                    ChatColor.RED + "No such command: " + ChatColor.GREEN + "/rpg " + sub);
            return true;
        }
        if (!sender.hasPermission(subcommand.getPermission())) {
            sender.sendMessage(ChatColor.RED + "Permission denied");
            return true;
        }

        subcommand.onCommand(sender, args, 1);
        return true;
    }

    /*
     * -------- [ Help for /rpg (page: 1/7) ] -------- /rpg choices - View
     * your choices for advancement /rpg about <class> - Show information
     * about a class /rpg choose <class> - Pick your next class /rpg status -
     * Check your status, including HP, Mana, and effects /rpg skills - Check
     * on the skills you currently have /rpg skill <skill> - Cast a skill
     * More: /rpg help 2, /rpg help choices
     */
    public void doHelp(CommandSender sender, String label, int page) {
        if (this.helpList == null) {
            buildSortedList();
        }

        if (page < 1) {
            page = 1;
        }
        int startCount = (page - 1) * COMMANDS_PER_PAGE;
        int startIndex = -1;
        int lastPageIndex = 0;
        int totalCount = 0;

        for (int i = 0; i < this.helpList.size(); i++) {
            Subcommand cmd = this.subcommandMap.get(this.helpList.get(i));
            if (sender.hasPermission(cmd.getPermission())) {
                if (totalCount == startCount) {
                    startIndex = i;
                }
                if (totalCount % COMMANDS_PER_PAGE == 0) {
                    lastPageIndex = i;
                }
                totalCount++;
            }
        }
        int pageCount = (int) Math.ceil(((double) totalCount) / COMMANDS_PER_PAGE);
        if (page > pageCount || startIndex == -1) {
            page = pageCount;
            startIndex = lastPageIndex;
        }

        sender.sendMessage(String.format(
                ChatColor.YELLOW + "-------- [ " + ChatColor.DARK_BLUE + "Help for "
                        + ChatColor.WHITE + "/%s" + ChatColor.DARK_BLUE + " (page: "
                        + ChatColor.LIGHT_PURPLE + "%d" + ChatColor.DARK_BLUE + "/"
                        + ChatColor.LIGHT_PURPLE + "%d" + ChatColor.DARK_BLUE + ")"
                        + ChatColor.YELLOW + " ] --------",
                label, page, pageCount));

        for (int i = startIndex; i < this.helpList.size() && i < startIndex + COMMANDS_PER_PAGE;
             i++) {
            String key = this.helpList.get(i);
            Subcommand cmd = this.subcommandMap.get(key);
            if (sender.hasPermission(cmd.getPermission())) {
                sender.sendMessage(String.format(
                        " " + ChatColor.GREEN + "/%s %s" + ChatColor.RESET + " - "
                                + ChatColor.DARK_GREEN + "%s",
                        label, key, cmd.getShortDescription()));
            }
        }

        sender.sendMessage(String.format(
                ChatColor.GRAY + "More: " + ChatColor.GREEN + "/%s help <page>" + ChatColor.GRAY
                        + ", " + ChatColor.GREEN + "/rpg help status" + ChatColor.GRAY + "",
                label));
    }

    /*
     * ##### Help for /rpg adminedit (Alias for /rpg edit) Interactive config
     * editor Usage: /rpg edit Edit your KraftRPG configuration interactively.
     */
    public void doHelp(CommandSender sender, String label, String subLabel) {
        Subcommand subcommand = this.subcommandMap.get(subLabel);

        if (this.aliasMap.containsKey(subLabel)) {
            sender.sendMessage(String.format(
                    "" + ChatColor.YELLOW + "##### " + ChatColor.DARK_BLUE + "Help for "
                            + ChatColor.AQUA + "/%s %s " + ChatColor.GRAY + "" + ChatColor.ITALIC
                            .toString() + "(Alias for /%s %s)",
                    label, subLabel, label, this.aliasMap.get(subLabel)));
        } else {
            sender.sendMessage(String.format(
                    "" + ChatColor.YELLOW + "##### " + ChatColor.DARK_BLUE + "Help for "
                            + ChatColor.AQUA + "/%s %s",
                    label, subLabel));
        }

        if (!sender.hasPermission(subcommand.getPermission())) {
            sender.sendMessage("" + ChatColor.RED + "This command is restricted.");
            return;
        }

        sender.sendMessage(
                String.format("" + ChatColor.BLUE + "  %s", subcommand.getShortDescription()));
        sender.sendMessage(String.format("" + ChatColor.GOLD + "  Usage: " + ChatColor.AQUA + "%s",
                                         subcommand.getUsage()));
        sender.sendMessage(subcommand.getLongDescription().split("\n"));
    }

    private void buildSortedList() {
        this.helpList = new ArrayList<>(this.subcommandMap.size() - this.aliasMap.size());

        for (String s : this.subcommandMap.keySet()) {
            if (!this.aliasMap.containsKey(s)) {
                this.helpList.add(s);
            }
        }

        Collections.sort(this.helpList);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label,
                                      String[] args) {
        List<String> matches = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], this.subcommandMap.keySet(), matches);

            // Remove items sender doesn't have permission for
            ListIterator<String> iter = matches.listIterator();
            while (iter.hasNext()) {
                String s = iter.next();
                Subcommand subcommand = this.subcommandMap.get(s);
                if (!sender.hasPermission(subcommand.getPermission())) {
                    iter.remove();
                }
            }

            return matches;
        } else {
            Subcommand subcommand = this.subcommandMap.get(args[0]);
            if (subcommand == null) {
                return null;
            }

            return subcommand.onTabComplete(sender, args, 1);
        }
    }
}
