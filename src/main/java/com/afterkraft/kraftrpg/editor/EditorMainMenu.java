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
package com.afterkraft.kraftrpg.editor;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

public class EditorMainMenu extends EditorPrompt {

    @Override
    public String getName(ConversationContext context) {
        return "main";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        if (command.equals("?")) {
            EditorState.setBanner(context, true);
            return this;
        } else if (command.equals("save")) {
            sendMessage(context, "<unimplemented>");
            return this;
        } else if (command.equals("exit") || command.equals("quit") || command.equals("stop")) {
            sendMessage(context, ChatColor.DARK_PURPLE + "Exiting KraftRPG configuration editor.");
            return END_CONVERSATION;
        } else if (command.equals("1") || command.equals("class") || command.equals("roles")) {
            return callPrompt(context, new EditorRoleMenu());
        } else if (command.equals("2") || command.equals("skills")) {
            // return callPrompt(context, new EditorSkillsMenu());
            return null;
        } else if (command.equals("3") || command.equals("settings")) {
            // return callPrompt(context, new EditorSettingsMenu());
            return null;
        } else {
            sendMessage(context, ChatColor.RED + "Unrecognized editor command. Say 'exit' to exit.");
            return null;
        }
    }

    @Override
    public void printBanner(ConversationContext context) {
        sendMessage(context, ChatColor.DARK_GREEN + "KraftRPG Configuration Editor: " + ChatColor.BLUE + "Main Menu");
        sendMessage(context, ChatColor.AQUA + "[1]" + ChatColor.DARK_GREEN + " Classes, Professions, and Roles");
        sendMessage(context, ChatColor.AQUA + "[2]" + ChatColor.DARK_GREEN + " Skills");
        sendMessage(context, ChatColor.AQUA + "[3]" + ChatColor.DARK_GREEN + " Global Settings");
        if (EditorState.isDirty(context)) {
            sendMessage(context, ChatColor.GOLD + "* You have un" + ChatColor.AQUA + "save" + ChatColor.GOLD + "d changes.");
        }
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return getPathString(context) + "roles skills settings save exit";
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of("roles", "class", "skills", "settings", "save", "exit", "quit");
    }
}
