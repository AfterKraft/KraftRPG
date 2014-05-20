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
import com.afterkraft.kraftrpg.editor.EditorMainMenu;
import com.afterkraft.kraftrpg.editor.EditorState;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;

import java.util.Arrays;
import java.util.List;

public class RPGEditorCommand extends BasicSubcommand {
    private ConversationFactory factory;

    public RPGEditorCommand(RPGPlugin plugin) {
        super(plugin);
        setShortDescription("Interactive config editor");
        setLongDescription("" +
                "Edit your KraftRPG configuration interactively.\n" +
                // passive-aggressive note to FIXME BUKKIT-5611
                ChatColor.RED + "Warning: On some old servers, this may not work unless used from the console.");

        setPermission("kraftrpg.admin.edit");

        factory = new ConversationFactory(plugin)
                .withFirstPrompt(new EditorMainMenu())
                .withModality(false)
                .withLocalEcho(false)
                .withInitialSessionData(EditorState.getStableDefaultState());
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, int depth) {
        if (sender instanceof Conversable) {
            Conversation conv = factory.buildConversation((Conversable) sender);
            EditorState.applyUnstableDefaultState(conv.getContext());
            if (args.length > depth) {
                String input = StringUtils.join(Arrays.copyOfRange(args, depth, args.length), " ");
                EditorState.queueCommands(conv.getContext(), input);
            }
            conv.begin();
        } else {
            sender.sendMessage("");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args, int depth) {
        return ImmutableList.of();
    }
}
