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
package com.afterkraft.kraftrpg.editor;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;

import java.util.List;

public class EditorMainMenu extends EditorPrompt {

    public String getName(ConversationContext context) {
        return "main";
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
        return getPathString(context) + "classes skills settings save exit";
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
        } else if (command.equals("1") || command.equals("class") || command.equals("classes")) {
            return callPrompt(context, new EditorClassMenu());
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
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.of("class", "skills", "settings", "save", "exit", "quit");
    }
}
