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

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.roles.Role;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

import java.util.ArrayList;
import java.util.List;

public abstract class PromptGetRole extends EditorPrompt {
    private final String prompt;

    public PromptGetRole(String prompt) {
        this.prompt = prompt;
    }

    public List<String> getCompletions(ConversationContext context) {
        return new ArrayList<String>(KraftRPGPlugin.getInstance().getRoleManager().getRoles().keySet());
    }

    public String getName(ConversationContext context) {
        return "role";
    }

    public String getPrompt(ConversationContext context) {
        return prompt;
    }

    public EditorPrompt performCommand(ConversationContext context, String command) {
        if (command.equals("!cancel") || command.equals("exit") || command.equals("quit") || command.equals("stop")) {
            sendMessage(context, "Input cancelled.");
            return returnPrompt(context);
        } else {
            Role r = plugin.getRoleManager().getRole(command);
            if (r == null) {
                sendMessage(context, ChatColor.RED + "Please choose the name of a role.");
                return null;
            } else {
                if (apply(r)) {
                    return returnPrompt(context);
                } else {
                    return null;
                }
            }
        }
    }

    public void printBanner(ConversationContext context) {
        sendMessage(context, "Available roles:");
        StringBuilder sb = new StringBuilder();
        for (String name : plugin.getRoleManager().getRoles().keySet()) {
            sb.append(name).append(" ");
        }
        sendMessage(context, sb.toString());
        sendMessage(context, "To cancel, type '!cancel'.");
    }

    public abstract boolean apply(Role r);
}
