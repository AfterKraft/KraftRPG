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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;

import com.afterkraft.kraftrpg.KraftRPGPlugin;
import com.afterkraft.kraftrpg.api.roles.Role;

/**
 * TODO Add documentation
 */
public abstract class PromptGetRole extends EditorPrompt {
    private final String prompt;

    public PromptGetRole(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String getName(ConversationContext context) {
        return "role";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        if (command.equals("!cancel") || command.equals("exit") || command.equals("quit") || command
                .equals("stop")) {
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

    @Override
    public void printBanner(ConversationContext context) {
        sendMessage(context, "Available roles:");
        StringBuilder sb = new StringBuilder();
        for (String name : plugin.getRoleManager().getRoles().keySet()) {
            sb.append(name).append(" ");
        }
        sendMessage(context, sb.toString());
        sendMessage(context, "To cancel, type '!cancel'.");
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return prompt;
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return new ArrayList<String>(
                KraftRPGPlugin.getInstance().getRoleManager().getRoles().keySet());
    }

    public abstract boolean apply(Role r);
}
