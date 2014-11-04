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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.conversations.ConversationContext;

import com.google.common.collect.ImmutableList;

import com.afterkraft.kraftrpg.api.roles.Role;
import com.afterkraft.kraftrpg.api.roles.Role.RoleType;

/**
 * TODO Add documentation
 */
public class EditorRoleFocusType extends EditorPrompt {
    private static final Map<String, Role.RoleType> roleNameMap;

    static {
        roleNameMap = new HashMap<>();
        for (Role.RoleType elem : RoleType.values()) {
            roleNameMap.put(elem.toString().toLowerCase(), elem);
        }
        roleNameMap.put("class", RoleType.PRIMARY);
        roleNameMap.put("profession", RoleType.SECONDARY);
        roleNameMap.put("extra", RoleType.ADDITIONAL);
    }

    @Override
    public String getName(ConversationContext context) {
        return "type";
    }

    @Override
    public EditorPrompt performCommand(ConversationContext context, String command) {
        if (command.equals("!cancel") || command.equals("stop") || command.equals("quit")) {
            sendMessage(context, "Input cancelled.");
            return returnPrompt(context);
        } else {
            Role.RoleType type = roleNameMap.get(command);
            if (type == null) {
                return null;
            }
            Role role = EditorState.getSelectedRole(context);

            boolean chooseNewDefault = false;
            switch (role.getType()) {
                case PRIMARY:
                    chooseNewDefault = plugin.getRoleManager().getDefaultPrimaryRole() == role;
                    break;
                case SECONDARY:
                    chooseNewDefault = plugin.getRoleManager().getDefaultSecondaryRole() == role;
                    break;
                default:
                    break;
            }

            if (chooseNewDefault) {
                // TODO pick a new default here, instead of error message
                // using PromptGetRole
                sendMessage(context, "You cannot change the type of the currently default role!");
                return null;
            }

            EditorState.setSelectedRole(context, Role.copyOf(role).setType(type).build());
            return returnPrompt(context);
        }
    }

    @Override
    public void printBanner(ConversationContext context) {
    }

    @Override
    public String getPrompt(ConversationContext context) {
        return "Choose the new type for this role (primary, secondary, extra).";
    }

    @Override
    public List<String> getCompletions(ConversationContext context) {
        return ImmutableList.copyOf(roleNameMap.keySet());
    }
}
