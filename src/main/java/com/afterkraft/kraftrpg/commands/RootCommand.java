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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.*;

public abstract class RootCommand implements TabExecutor {
    protected RPGPlugin plugin;
    private Map<String, String> aliasMap;
    private Map<String, Subcommand> subcommandMap;
    private List<String> helpList = null;

    public RootCommand(RPGPlugin plugin) {
        this.plugin = plugin;
        aliasMap = new HashMap<String, String>();
        subcommandMap = new HashMap<String, Subcommand>();
    }

    protected void addSubcommand(String name, Subcommand sub) {
        assert helpList == null;
        name = name.toLowerCase();

        subcommandMap.put(name, sub);
    }

    protected void addSubcommand(String name, Subcommand sub, String... aliases) {
        assert helpList == null;
        name = name.toLowerCase();

        subcommandMap.put(name, sub);
        for (String alias : aliases) {
            subcommandMap.put(alias, sub);
            aliasMap.put(alias, name);
        }
    }

    protected static final int COMMANDS_PER_PAGE = 7;

    private void buildSortedList() {
        helpList = new ArrayList<String>(subcommandMap.size() - aliasMap.size());

        for (String s : subcommandMap.keySet()) {
            if (!aliasMap.containsKey(s)) {
                helpList.add(s);
            }
        }

        Collections.sort(helpList);
    }


    /*
    -------- [ Help for /rpg (page: 1/7) ] --------
     /rpg choices - View your choices for advancement
     /rpg about <class> - Show information about a class
     /rpg choose <class> - Pick your next class
     /rpg status - Check your status, including HP, Mana, and effects
     /rpg skills - Check on the skills you currently have
     /rpg skill <skill> - Cast a skill
    More: /rpg help 2, /rpg help choices
     */
    public void doHelp(CommandSender sender, String label, int page) {
        if (helpList == null) buildSortedList();

        if (page < 1) page = 1;
        int startCount = (page - 1) * COMMANDS_PER_PAGE;
        int startIndex = -1, lastPageIndex = 0;
        int totalCount = 0;

        for (int i = 0; i < helpList.size(); i++) {
            Subcommand cmd = subcommandMap.get(helpList.get(i));
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
                "§e-------- [ §1Help for §f/%s§1 (page: §d%d§1/§d%d§1)§e ] --------",
                label, page, pageCount));

        for (int i = startIndex; i < helpList.size() && i < startIndex + COMMANDS_PER_PAGE; i++) {
            String key = helpList.get(i);
            Subcommand cmd = subcommandMap.get(key);
            sender.sendMessage(String.format(
                    " §a/%s %s§r - §2%s",
                    label, key, cmd.getShortDescription()));
        }

        sender.sendMessage(String.format(
                "§7More: §a/%s help <page>§7, §a/rpg help status§7", label));
    }

    /*
    ##### Help for /rpg adminedit (Alias for /rpg edit)
      Interactive config editor
      Usage: /rpg edit
    Edit your KraftRPG configuration interactively.
     */
    public void doHelp(CommandSender sender, String label, String subLabel) {
        Subcommand subcommand = subcommandMap.get(subLabel);

        if (aliasMap.containsKey(subLabel)) {
            sender.sendMessage(String.format(
                    "§e##### §1Help for §b/%s %s §7§i(Alias for /%s %s)",
                    label, subLabel, label, aliasMap.get(subLabel)));
        } else {
            sender.sendMessage(String.format(
                    "§e##### §1Help for §b/%s %s",
                    label, subLabel));
        }

        if (!sender.hasPermission(subcommand.getPermission())) {
            sender.sendMessage("§cThis command is restricted.");
            return;
        }

        sender.sendMessage(String.format("§9  %s", subcommand.getShortDescription()));
        sender.sendMessage(String.format("§6  Usage: §b%s", subcommand.getUsage()));
        sender.sendMessage(subcommand.getLongDescription().split("\n"));
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
                if (subcommandMap.containsKey(sub)) {
                    doHelp(sender, label, sub);
                } else {
                    try {
                        int page = Integer.parseInt(sub);
                        doHelp(sender, label, page);
                    } catch (NumberFormatException ignored) {
                        sender.sendMessage("§cPlease enter a page number or valid subcommand.");
                    }
                }
            }
            return true;
        }

        Subcommand subcommand = subcommandMap.get(sub);
        if (subcommand == null) {
            sender.sendMessage("§cNo such command: §a/rpg " + sub);
            return true;
        }
        if (!sender.hasPermission(subcommand.getPermission())) {
            sender.sendMessage("§cPermission denied");
            return true;
        }

        subcommand.onCommand(sender, args, 1);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> matches = new ArrayList<String>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], subcommandMap.keySet(), matches);

            // Remove items sender doesn't have permission for
            ListIterator<String> iter = matches.listIterator();
            while (iter.hasNext()) {
                String s = iter.next();
                Subcommand subcommand = subcommandMap.get(s);
                if (!sender.hasPermission(subcommand.getPermission())) {
                    iter.remove();
                }
            }

            return matches;
        } else {
            Subcommand subcommand = subcommandMap.get(args[0]);
            if (subcommand == null) return null;

            return subcommand.onTabComplete(sender, args, 1);
        }
    }
}
