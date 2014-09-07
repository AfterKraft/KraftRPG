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

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;

import com.afterkraft.kraftrpg.api.RPGPlugin;
import com.afterkraft.kraftrpg.editor.EditorMainMenu;
import com.afterkraft.kraftrpg.editor.EditorState;

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

        this.factory = new ConversationFactory(plugin)
                .withFirstPrompt(new EditorMainMenu())
                .withModality(false)
                .withLocalEcho(false)
                .withInitialSessionData(EditorState.getStableDefaultState());
    }

    @Override
    public void onCommand(CommandSender sender, String[] args, int depth) {
        if (sender instanceof Conversable) {
            Conversation conv = this.factory.buildConversation((Conversable) sender);
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
