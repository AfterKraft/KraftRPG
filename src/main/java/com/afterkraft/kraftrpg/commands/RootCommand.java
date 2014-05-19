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
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import java.util.*;

public abstract class RootCommand implements TabExecutor {
    protected RPGPlugin plugin;
    private Map<String, String> aliasMap;
    private Map<String, Subcommand> subcommandMap;
    private List<List<Subcommand>> helpPages = null;

    public RootCommand(RPGPlugin plugin) {
        this.plugin = plugin;
        aliasMap = new HashMap<String, String>();
        subcommandMap = new HashMap<String, Subcommand>();
    }

    protected void addSubcommand(String name, Subcommand sub) {
        assert helpPages == null;

        subcommandMap.put(name, sub);
    }

    protected void addSubcommand(String name, Subcommand sub, String... aliases) {
        assert helpPages == null;

        subcommandMap.put(name, sub);
        for (String alias : aliases) {
            subcommandMap.put(alias, sub);
            aliasMap.put(alias, name);
        }
    }

    protected static final int COMMANDS_PER_PAGE = 7;

    private void buildHelpPages() {
        // Sort the commands by their non-aliased name
        TreeMap<String, Subcommand> map = new TreeMap<String, Subcommand>();
        for (Map.Entry<String, Subcommand> entry : subcommandMap.entrySet()) {
            if (aliasMap.containsKey(entry.getKey())) continue;
            map.put(entry.getKey(), entry.getValue());
        }

        // Build the list from the sorted map

        ImmutableList.Builder<List<Subcommand>> outerBuilder = ImmutableList.builder();
        ImmutableList.Builder<Subcommand> innerBuilder = ImmutableList.builder();
        int currentCount = 1;

        for (Subcommand subcommand : map.values()) {
            innerBuilder.add(subcommand);
            if (currentCount++ == COMMANDS_PER_PAGE) {
                outerBuilder.add(innerBuilder.build());
                innerBuilder = ImmutableList.builder();
                currentCount = 1;
            }
        }

        if (currentCount != 1) {
            outerBuilder.add(innerBuilder.build());
        } else {
            assert innerBuilder.build().size() == 0;
        }

        helpPages = outerBuilder.build();
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
        if (helpPages == null) buildHelpPages();

        sender.sendMessage("-------- [ Help for /rpg (page: 1/7) ] --------\n" +
                " /rpg choices - View your choices for advancement\n" +
                " /rpg about <class> - Show information about a class\n" +
                " /rpg choose <class> - Pick your next class\n" +
                " /rpg status - Check your status, including HP, Mana, and effects\n" +
                " /rpg skills - Check on the skills you currently have\n" +
                " /rpg skill <skill> - Cast a skill\n" +
                "More: /rpg help 2, /rpg help choices");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            doHelp(sender, label, 1);
        }

        return false;
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
